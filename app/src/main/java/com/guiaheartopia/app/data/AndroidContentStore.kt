package com.guiaheartopia.app.data

import android.content.Context
import com.guiaheartopia.app.BuildConfig
import com.guiaheartopia.app.domain.ContentDownloadProgress
import com.guiaheartopia.app.domain.ContentLibrary
import com.guiaheartopia.app.domain.ContentManifest
import com.guiaheartopia.app.domain.ContentPackage
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ContentStatus(
    val contentVersion: Int,
    val publishedOn: String,
    val needsNewerApp: Boolean,
)

object AndroidContentStore {
    private const val PREFS = "content-sync"
    private const val KEY_ATTEMPT = "lastAttempt"
    private const val KEY_NEEDS_APP = "needsNewerApp"
    private const val MAX_DOWNLOAD_BYTES = 50L * 1024 * 1024
    private const val CHUNK = 16 * 1024

    fun root(context: Context): File = File(context.filesDir, "content")

    fun liveDir(context: Context): File = File(root(context), "live")

    fun overlayFile(context: Context, relative: String): File? {
        if (!ContentPackage.isSafePath(relative)) return null
        val file = File(liveDir(context), "files/$relative")
        return file.takeIf { it.isFile }
    }

    fun status(context: Context): ContentStatus {
        val library = AndroidContentLibrary(context)
        return ContentStatus(
            contentVersion = ContentPackage.currentVersion(library),
            publishedOn = ContentPackage.currentPublishedOn(library),
            needsNewerApp = library.needsNewerApp(),
        )
    }

    fun installedVersion(context: Context): Int = status(context).contentVersion

    fun lastAttemptMillis(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_ATTEMPT, 0L)

    fun markAttempt(context: Context, nowMillis: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_ATTEMPT, nowMillis)
            .apply()
    }

    fun syncNow(
        context: Context,
        onProgress: ((ContentDownloadProgress) -> Unit)? = null,
    ): com.guiaheartopia.app.domain.ContentSyncReport {
        val library = AndroidContentLibrary(context)
        return ContentPackage.sync(
            library = library,
            fetch = ::httpGet,
            appVersionCode = BuildConfig.VERSION_CODE,
            baseUrl = BuildConfig.CONTENT_BASE_URL,
            onProgress = onProgress,
        )
    }

    /** Descarga por chunks, con tope y validación de Content-Length. Nunca readBytes() sin límite. */
    fun httpGet(url: String): ByteArray? = try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            useCaches = false
            setRequestProperty("Accept", "*/*")
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                val declared = connection.contentLengthLong
                if (declared > MAX_DOWNLOAD_BYTES) return null
                connection.inputStream.use { input ->
                    val out = ByteArrayOutputStream()
                    val buffer = ByteArray(CHUNK)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_DOWNLOAD_BYTES) return null
                        out.write(buffer, 0, read)
                    }
                    out.toByteArray()
                }
            }
        } finally {
            connection.disconnect()
        }
    } catch (_: Exception) {
        null
    }
}

class AndroidContentLibrary(private val context: Context) : ContentLibrary {
    override fun bundledManifest(): ContentManifest = try {
        context.assets.open("content/manifest.json").bufferedReader().use { reader ->
            ContentPackage.parseManifest(reader.readText())
        }
    } catch (_: Exception) {
        null
    } ?: ContentManifest(contentVersion = 1, minAppVersionCode = 1, publishedOn = "2026-11-10")

    override fun installedManifest(): ContentManifest? = try {
        val file = File(AndroidContentStore.liveDir(context), "manifest.json")
        if (!file.isFile) null else ContentPackage.parseManifest(file.readText())
    } catch (_: Exception) {
        null
    }

    override fun effectiveBytes(path: String): ByteArray? {
        if (!ContentPackage.isSafePath(path)) return null
        return ContentCache.bytes(path) {
            overlayBytes(path) ?: try {
                context.assets.open(path).use { it.readBytes() }
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun overlayBytes(path: String): ByteArray? =
        runCatching { AndroidContentStore.overlayFile(context, path)?.readBytes() }.getOrNull()

    /**
     * Publicación atómica: `previous` se conserva hasta confirmar staging→live,
     * se verifica cada renameTo y nunca se deja la app sin contenido live.
     */
    override fun publish(manifest: ContentManifest, overlay: Map<String, ByteArray>) {
        val root = AndroidContentStore.root(context)
        val staging = File(root, "staging")
        val live = File(root, "live")
        val previous = File(root, "previous")

        // 1. Preparamos el staging completo antes de tocar live.
        if (staging.exists() && !staging.deleteRecursively()) error("no se pudo limpiar el staging")
        val filesDir = File(staging, "files")
        if (!filesDir.mkdirs() && !filesDir.isDirectory) error("no se pudo crear el staging")
        overlay.forEach { (path, bytes) ->
            require(ContentPackage.isSafePath(path))
            val dest = File(filesDir, path)
            dest.parentFile?.mkdirs()
            val part = File(dest.parentFile, dest.name + ".part")
            part.writeBytes(bytes)
            if (!part.renameTo(dest)) error("no se pudo guardar $path")
        }
        val manifestFile = File(staging, "manifest.json")
        val manifestPart = File(staging, "manifest.json.part")
        manifestPart.writeText(ContentPackage.encodeManifest(manifest))
        if (!manifestPart.renameTo(manifestFile)) error("no se pudo guardar el manifiesto")

        // 2. Apartamos el contenido anterior solo con el staging ya completo.
        if (previous.exists() && !previous.deleteRecursively()) error("no se pudo limpiar la copia anterior")
        var movedPrevious = false
        if (live.exists()) {
            if (!live.renameTo(previous)) error("no se pudo apartar el contenido anterior")
            movedPrevious = true
        }

        // 3. Publicamos. Si falla, restauramos el contenido anterior.
        if (!staging.renameTo(live)) {
            if (movedPrevious && !previous.renameTo(live)) {
                error("no se pudo publicar ni restaurar el contenido anterior")
            }
            error("no se pudo publicar el contenido")
        }
        // previous se conserva hasta aquí: live ya está confirmado.
        if (previous.exists()) previous.deleteRecursively()
        ContentCache.invalidate()
    }

    override fun needsNewerApp(): Boolean =
        context.getSharedPreferences("content-sync", Context.MODE_PRIVATE).getBoolean(NEEDS_APP, false)

    override fun setNeedsNewerApp(value: Boolean) {
        context.getSharedPreferences("content-sync", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(NEEDS_APP, value)
            .apply()
    }

    companion object {
        private const val NEEDS_APP = "needsNewerApp"
    }
}
