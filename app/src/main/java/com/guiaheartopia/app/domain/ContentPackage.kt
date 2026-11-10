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
)

@Serializable
data class ContentManifest(
    val contentVersion: Int,
    val minAppVersionCode: Int,
    val publishedOn: String,
    val files: List<ContentFileEntry> = emptyList(),
)

enum class ContentSyncResult {
    APPLIED,
    UNCHANGED,
    OLDER,
    NEEDS_NEWER_APP,
    FAILED,
}

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
                !isSafePath(file.path) || file.size < 0 || !shaPattern.matches(file.sha256)
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
    ): ContentSyncResult {
        val raw = fetch(manifestUrl(baseUrl)) ?: return ContentSyncResult.FAILED
        val remote = parseManifest(raw.decodeToString()) ?: return ContentSyncResult.FAILED
        val current = currentVersion(library)
        if (remote.contentVersion < current) return ContentSyncResult.OLDER
        if (remote.contentVersion == current) {
            if (remote.minAppVersionCode <= appVersionCode) library.setNeedsNewerApp(false)
            return ContentSyncResult.UNCHANGED
        }
        if (remote.minAppVersionCode > appVersionCode) {
            library.setNeedsNewerApp(true)
            return ContentSyncResult.NEEDS_NEWER_APP
        }
        val next = LinkedHashMap<String, ByteArray>()
        for (file in remote.files) {
            val have = library.effectiveBytes(file.path)
            val same = have != null &&
                have.size.toLong() == file.size &&
                sha256(have) == file.sha256
            if (same) {
                val overlay = library.overlayBytes(file.path)
                if (overlay != null) next[file.path] = overlay
            } else {
                val bytes = fetch(fileUrl(baseUrl, file.path)) ?: return ContentSyncResult.FAILED
                if (bytes.size.toLong() != file.size || sha256(bytes) != file.sha256) {
                    return ContentSyncResult.FAILED
                }
                next[file.path] = bytes
            }
        }
        return try {
            library.publish(remote, next)
            library.setNeedsNewerApp(false)
            ContentSyncResult.APPLIED
        } catch (_: Exception) {
            ContentSyncResult.FAILED
        }
    }
}
