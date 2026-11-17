package com.guiaheartopia.app.domain

import java.security.MessageDigest
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Paquete publicado en el sitio de la guía.
 * `content/manifest.json` más los archivos que lista, con tamaño y SHA-256.
 */
@Serializable
data class ContentFileEntry(
    val path: String,
    val size: Long,
    val sha256: String,
    /** Si existe, solo la descarga la app con versionCode >= este valor. */
    val minAppVersionCode: Int? = null,
)

@Serializable
data class ContentManifest(
    val contentVersion: Int,
    val minAppVersionCode: Int,
    val publishedOn: String,
    val files: List<ContentFileEntry> = emptyList(),
)

/** Avance de la descarga de los archivos pendientes de un paquete de contenido. */
data class ContentDownloadProgress(
    val downloadedFiles: Int,
    val totalFiles: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
)

interface ContentLibrary {
    fun bundledManifest(): ContentManifest
    fun installedManifest(): ContentManifest?
    fun effectiveBytes(path: String): ByteArray?
    fun overlayBytes(path: String): ByteArray?
    fun publish(manifest: ContentManifest, overlay: Map<String, ByteArray>)
    fun needsNewerApp(): Boolean
    fun setNeedsNewerApp(value: Boolean)
}

object ContentPackage {
    private const val DOWNLOAD_THREADS = 5
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val shaPattern = Regex("[0-9a-fA-F]{64}")
    private val progressLock = Any()

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun isSafePath(path: String): Boolean {
        if (path.isBlank() || path.startsWith("/") || path.contains('\\') || path.contains("..")) return false
        return path.split('/').all { it.isNotEmpty() && it != "." }
    }

    fun parseManifest(raw: String): ContentManifest? = try {
        val parsed = json.decodeFromString(ContentManifest.serializer(), raw)
        if (parsed.contentVersion < 1 || parsed.minAppVersionCode < 0 || parsed.publishedOn.isBlank()) {
            null
        } else if (parsed.files.any { file ->
                !isSafePath(file.path) || file.size < 0 || !shaPattern.matches(file.sha256) ||
                    (file.minAppVersionCode != null && file.minAppVersionCode < 0)
            }
        ) {
            null
        } else if (parsed.files.map { it.path }.distinct().size != parsed.files.size) {
            null
        } else {
            parsed.copy(files = parsed.files.map { it.copy(sha256 = it.sha256.lowercase()) })
        }
    } catch (_: Exception) {
        null
    }

    fun encodeManifest(manifest: ContentManifest): String =
        json.encodeToString(ContentManifest.serializer(), manifest)

    fun manifestUrl(baseUrl: String): String = fileUrl(baseUrl, "manifest.json")

    fun fileUrl(baseUrl: String, relative: String): String {
        val root = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return root + "content/" + relative.trimStart('/')
    }

    fun currentVersion(library: ContentLibrary): Int =
        library.installedManifest()?.contentVersion ?: library.bundledManifest().contentVersion

    fun currentPublishedOn(library: ContentLibrary): String =
        library.installedManifest()?.publishedOn ?: library.bundledManifest().publishedOn

    /**
     * Descarga el manifiesto y solo los archivos cuyo SHA-256 cambió.
     * Si algo falla, no publica nada.
     */
    fun sync(
        library: ContentLibrary,
        fetch: (String) -> ByteArray?,
        appVersionCode: Int,
        baseUrl: String,
        onProgress: ((ContentDownloadProgress) -> Unit)? = null,
    ): ContentSyncReport {
        val current = currentVersion(library)
        val raw = fetch(manifestUrl(baseUrl)) ?: return ContentSyncReport(ContentSyncResult.NETWORK_ERROR, current, null)
        val remote = parseManifest(raw.decodeToString())
            ?: return ContentSyncReport(ContentSyncResult.VERIFY_FAILED, current, null)
        val remoteVersion = remote.contentVersion
        if (remote.contentVersion < current) {
            return ContentSyncReport(ContentSyncResult.OLDER, current, remoteVersion)
        }
        if (remote.minAppVersionCode > appVersionCode) {
            library.setNeedsNewerApp(true)
            return ContentSyncReport(ContentSyncResult.NEEDS_NEWER_APP, current, remoteVersion)
        }
        val missing = pendingDownloads(remote, library, appVersionCode)
        if (remote.contentVersion == current && missing.isEmpty()) {
            if (remote.minAppVersionCode <= appVersionCode) library.setNeedsNewerApp(false)
            return ContentSyncReport(ContentSyncResult.UNCHANGED, current, remoteVersion)
        }
        return applyOverlay(library, fetch, baseUrl, remote, appVersionCode, current, remoteVersion, onProgress)
    }

    internal fun fileEligible(file: ContentFileEntry, appVersionCode: Int): Boolean =
        file.minAppVersionCode == null || file.minAppVersionCode <= appVersionCode

    internal fun overlayMatches(file: ContentFileEntry, library: ContentLibrary): Boolean {
        val have = library.overlayBytes(file.path) ?: return false
        return have.size.toLong() == file.size && sha256(have) == file.sha256
    }

    internal fun pendingDownloads(
        remote: ContentManifest,
        library: ContentLibrary,
        appVersionCode: Int,
    ): List<ContentFileEntry> = remote.files.filter { file ->
        fileEligible(file, appVersionCode) && !overlayMatches(file, library)
    }

    private fun applyOverlay(
        library: ContentLibrary,
        fetch: (String) -> ByteArray?,
        baseUrl: String,
        remote: ContentManifest,
        appVersionCode: Int,
        current: Int,
        remoteVersion: Int,
        onProgress: ((ContentDownloadProgress) -> Unit)? = null,
    ): ContentSyncReport {
        // Calculamos primero qué hay que bajar y cuánto pesa para poder informar del progreso.
        val pending = pendingDownloads(remote, library, appVersionCode)
        val totalBytes = pending.sumOf { it.size }
        if (pending.isNotEmpty()) {
            onProgress?.invoke(ContentDownloadProgress(0, pending.size, 0L, totalBytes))
        }

        // Los archivos que ya tienen el SHA correcto se reutilizan sin volver a descargarlos.
        val pendingPaths = pending.map { it.path }.toSet()
        val reusable = LinkedHashMap<String, ByteArray>()
        remote.files.forEach { file ->
            if (fileEligible(file, appVersionCode) && file.path !in pendingPaths) {
                library.overlayBytes(file.path)?.let { reusable[file.path] = it }
            }
        }

        val downloaded = arrayOfNulls<ByteArray>(pending.size)
        val doneFiles = AtomicInteger(0)
        val doneBytes = AtomicLong(0L)
        val failure = AtomicReference<ContentSyncResult?>(null)

        val executor = Executors.newFixedThreadPool(DOWNLOAD_THREADS)
        try {
            val tasks: List<Callable<Unit>> = pending.mapIndexed { index, file -> Callable<Unit> {
                if (failure.get() == null) {
                    val bytes = fetch(fileUrl(baseUrl, file.path))
                    when {
                        bytes == null -> failure.compareAndSet(null, ContentSyncResult.NETWORK_ERROR)
                        bytes.size.toLong() != file.size || sha256(bytes) != file.sha256 ->
                            failure.compareAndSet(null, ContentSyncResult.VERIFY_FAILED)

                        else -> {
                            downloaded[index] = bytes
                            synchronized(progressLock) {
                                val files = doneFiles.incrementAndGet()
                                val bytesDone = doneBytes.addAndGet(bytes.size.toLong())
                                onProgress?.invoke(
                                    ContentDownloadProgress(files, pending.size, bytesDone, totalBytes),
                                )
                            }
                        }
                    }
                }
                Unit
            } }
            executor.invokeAll(tasks)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            failure.compareAndSet(null, ContentSyncResult.NETWORK_ERROR)
        } finally {
            executor.shutdownNow()
        }

        failure.get()?.let { return ContentSyncReport(it, current, remoteVersion) }

        // Reconstruimos el mapa en el orden del manifiesto, aunque la descarga haya terminado desordenada.
        val next = LinkedHashMap<String, ByteArray>()
        val pendingIndexByPath = pending.mapIndexed { index, file -> file.path to index }.toMap()
        remote.files.forEach { file ->
            if (!fileEligible(file, appVersionCode)) return@forEach
            val index = pendingIndexByPath[file.path]
            if (index == null) {
                reusable[file.path]?.let { next[file.path] = it }
            } else {
                val bytes = downloaded[index]
                    ?: return ContentSyncReport(ContentSyncResult.VERIFY_FAILED, current, remoteVersion)
                next[file.path] = bytes
            }
        }

        return try {
            library.publish(remote, next)
            library.setNeedsNewerApp(false)
            if (pending.isNotEmpty()) {
                onProgress?.invoke(ContentDownloadProgress(pending.size, pending.size, totalBytes, totalBytes))
            }
            ContentSyncReport(
                ContentSyncResult.APPLIED,
                currentVersion(library),
                remoteVersion,
            )
        } catch (_: Exception) {
            ContentSyncReport(ContentSyncResult.VERIFY_FAILED, current, remoteVersion)
        }
    }
}
