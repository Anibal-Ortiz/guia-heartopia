package com.guiaheartopia.app.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Guarda el PNG en `cacheDir/exports` y abre la hoja de compartir del sistema. */
object CommunityImageSharer {
    const val SHARE_CHOOSER_TITLE = "Enviar a comunidades"

    fun share(context: Context, bitmap: Bitmap, filename: String): Boolean {
        val uri = runCatching {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val safeName = CommunityImageFilename.sanitize(filename.removeSuffix(".png")) + ".png"
            val file = File(dir, safeName)
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull() ?: return false

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // No se incluye texto que falsee la imagen; cada tarjeta lleva su propio pie.
        }
        context.startActivity(Intent.createChooser(intent, SHARE_CHOOSER_TITLE))
        return true
    }
}
