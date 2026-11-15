package com.guiaheartopia.app.data

import android.graphics.Bitmap
import android.util.Base64
import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

/** La foto del avatar vive solo en el almacenamiento interno de la app. */
object AvatarStore {
    fun file(context: Context): File = File(context.filesDir, "avatar/avatar.jpg")

    fun save(context: Context, jpeg: ByteArray): String {
        val dest = file(context)
        dest.parentFile?.mkdirs()
        dest.writeBytes(jpeg)
        return Base64.encodeToString(jpeg, Base64.NO_WRAP)
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    fun restore(context: Context, base64: String?) {
        if (base64.isNullOrBlank()) {
            clear(context)
            return
        }
        val bytes = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull() ?: return
        val dest = file(context)
        dest.parentFile?.mkdirs()
        dest.writeBytes(bytes)
    }

    fun centerCropJpeg(source: Bitmap, maxSide: Int = 512): ByteArray {
        val side = min(source.width, source.height).coerceAtLeast(1)
        val x = ((source.width - side) / 2).coerceAtLeast(0)
        val y = ((source.height - side) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(source, x, y, side, side)
        val scaled = if (side > maxSide) Bitmap.createScaledBitmap(cropped, maxSide, maxSide, true) else cropped
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 86, stream)
        if (scaled !== cropped) scaled.recycle()
        if (cropped !== source) cropped.recycle()
        return stream.toByteArray()
    }
}
