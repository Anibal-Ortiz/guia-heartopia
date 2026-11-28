package com.guiaheartopia.app.ui.community

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Guarda el PNG en `cacheDir/shared_cards` y abre el selector nativo de Android. */
fun shareBitmapAsPng(
    context: Context,
    bitmap: Bitmap,
    communityName: String,
) {
    val cacheFolder = File(context.cacheDir, "shared_cards").apply { mkdirs() }
    val imageFile = File(cacheFolder, "wikifan_heartopia_comunidad.png")

    FileOutputStream(imageFile).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )

    val shareText = if (communityName.isNotBlank()) {
        "Reporte compartido por $communityName • Guía WikiFan para Heartopia"
    } else {
        "Guía diaria de Heartopia • WikiFan para Heartopia"
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, shareText)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(sendIntent, "Compartir imagen con tu comunidad").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
