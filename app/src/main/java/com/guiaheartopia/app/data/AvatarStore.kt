package com.guiaheartopia.app.data

import android.graphics.Bitmap
import android.util.Base64
import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

/** La foto del avatar vive solo en el almacenamiento interno de la app. */
object AvatarStore {
    private const val MAX_BYTES = 2 * 1024 * 1024

    /** SOI JPEG (FF D8) y tamaño acotado. Evita escribir arbitrary bytes como avatar. */
    private fun isJpeg(bytes: ByteArray): Boolean =
        bytes.size in 4..MAX_BYTES &&
            (bytes[0].toInt() and 0xFF) == 0xFF &&
            (bytes[1].toInt() and 0xFF) == 0xD8

    fun file(context: Context): File = File(context.filesDir, "avatar/avatar.jpg")

    fun save(context: Context, jpeg: ByteArray): String {
        if (!isJpeg(jpeg)) {
            return file(context).takeIf { it.isFile }?.let { existing ->
                runCatching { Base64.encodeToString(existing.readBytes(), Base64.NO_WRAP) }.getOrDefault("")
            } ?: ""
        }
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
        if (!isJpeg(bytes)) return
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
