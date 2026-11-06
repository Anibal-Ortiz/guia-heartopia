package com.guiaheartopia.app.pixel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

object PixelBitmaps {
    fun decode(context: Context, uri: Uri, maxSide: Int = 1600): ArgbImage {
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val scale = max(info.size.width, info.size.height).toFloat() / maxSide
                if (scale > 1f) {
                    decoder.setTargetSize(
                        (info.size.width / scale).toInt().coerceAtLeast(1),
                        (info.size.height / scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            var sample = 1
            while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opts)
            } ?: error("No se pudo leer la imagen")
        }
        val argb = if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap else bitmap.copy(Bitmap.Config.ARGB_8888, false)
        return argb.toArgbImage()
    }

    fun render(
        project: PixelProject,
        scale: Int = 1,
        grid: Boolean = false,
        skip: BooleanArray? = null,
        checker: Boolean = true,
    ): Bitmap {
        val safeScale = scale.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(project.cols * safeScale, project.rows * safeScale, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply { isFilterBitmap = false }
        for (y in 0 until project.rows) {
            for (x in 0 until project.cols) {
                val index = project.index(x, y)
                val value = project.cells[index]
                val left = x * safeScale.toFloat()
                val top = y * safeScale.toFloat()
                if (value < 0 || (skip != null && skip[index])) {
                    if (checker) {
                        paint.color = if ((x + y) % 2 == 0) 0xFFFFFFFF.toInt() else 0xFFD9D3C8.toInt()
                        canvas.drawRect(left, top, left + safeScale, top + safeScale, paint)
                    }
                    continue
                }
                paint.color = project.colors[value].argb
                canvas.drawRect(left, top, left + safeScale, top + safeScale, paint)
            }
        }
        if (grid && safeScale >= 4) {
            paint.color = 0x55000000
            for (x in 0..project.cols) {
                canvas.drawRect((x * safeScale).toFloat(), 0f, (x * safeScale + 1).toFloat(), bmp.height.toFloat(), paint)
            }
            for (y in 0..project.rows) {
                canvas.drawRect(0f, (y * safeScale).toFloat(), bmp.width.toFloat(), (y * safeScale + 1).toFloat(), paint)
            }
        }
        return bmp
    }

    /** PNG de referencia: cuadrícula, números si caben y leyenda de colores. */
    fun renderGuide(project: PixelProject, skip: BooleanArray? = null): Bitmap {
        val cell = when {
            project.cols >= 120 -> 8
            project.cols >= 80 -> 12
            else -> 18
        }
        val legendRows = project.colors.size.coerceAtLeast(1)
        val width = project.cols * cell + 48
        val height = project.rows * cell + 36 + legendRows * 28 + 24
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
        canvas.drawColor(0xFFFFF6EA.toInt())
        val art = render(project, scale = cell, grid = true, skip = skip, checker = true)
        canvas.drawBitmap(art, 32f, 24f, paint)
        paint.color = 0xFF3A271C.toInt()
        paint.textSize = 14f
        if (cell >= 16) {
            paint.textAlign = Paint.Align.CENTER
            for (x in 0 until project.cols) {
                if (x % 5 == 0) canvas.drawText("${x + 1}", 32f + (x + 0.5f) * cell, 18f, paint)
            }
            paint.textAlign = Paint.Align.RIGHT
            for (y in 0 until project.rows) {
                if (y % 5 == 0) canvas.drawText("${y + 1}", 28f, 24f + (y + 0.7f) * cell, paint)
            }
        }
        var y = 24f + art.height + 22f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 18f
        project.colors.forEachIndexed { index, color ->
            paint.color = color.argb
            canvas.drawRect(32f, y - 14f, 54f, y + 4f, paint)
            paint.color = 0xFF3A271C.toInt()
            val count = project.counts()[index]
            canvas.drawText("${index + 1}. ${color.name}  ${color.hex}  ×$count", 62f, y, paint)
            y += 28f
        }
        art.recycle()
        return bmp
    }
}

fun Bitmap.toArgbImage(): ArgbImage {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    return ArgbImage(width, height, pixels)
}

object ImageExporter {
    fun savePng(context: Context, bitmap: Bitmap, name: String): String {
        val safe = name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$safe.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GuiaHeartopia")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("No se pudo crear el archivo")
            context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                ?: error("No se pudo escribir el archivo")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)
            "Imágenes/GuiaHeartopia/$safe.png"
        } else {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "GuiaHeartopia").apply { mkdirs() }
            val file = File(dir, "$safe.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            file.absolutePath
        }
    }

    fun sharePng(context: Context, bitmap: Bitmap, name: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "${name.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Compartir guía").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

fun readableCellSize(cols: Int, rows: Int, viewPx: Float): Boolean {
    val cell = viewPx / max(cols, rows).coerceAtLeast(1)
    return cell.roundToInt() >= 22
}
