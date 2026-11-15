package com.guiaheartopia.app.domain

import java.security.MessageDigest
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
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val shaPattern = Regex("[0-9a-fA-F]{64}")

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
        if (remote.contentVersion == current && missing.isNotEmpty()) {
            return applyOverlay(library, fetch, baseUrl, remote, appVersionCode, current, remoteVersion)
        }
        return applyOverlay(library, fetch, baseUrl, remote, appVersionCode, current, remoteVersion)
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
    ): ContentSyncReport {
        val next = LinkedHashMap<String, ByteArray>()
        for (file in remote.files) {
            if (!fileEligible(file, appVersionCode)) continue
            if (overlayMatches(file, library)) {
                library.overlayBytes(file.path)?.let { next[file.path] = it }
                continue
            }
            val bytes = fetch(fileUrl(baseUrl, file.path))
                ?: return ContentSyncReport(ContentSyncResult.NETWORK_ERROR, current, remoteVersion)
            if (bytes.size.toLong() != file.size || sha256(bytes) != file.sha256) {
                return ContentSyncReport(ContentSyncResult.VERIFY_FAILED, current, remoteVersion)
            }
            next[file.path] = bytes
        }
        return try {
            library.publish(remote, next)
            library.setNeedsNewerApp(false)
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
