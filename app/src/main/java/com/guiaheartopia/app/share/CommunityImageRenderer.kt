package com.guiaheartopia.app.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.climateDisplay

/**
 * Renderiza una [CommunityCard] a `android.graphics.Bitmap` PNG-ready.
 * No usa Compose-snapshot: es determinista y no requiere una vista montada.
 */
object CommunityImageRenderer {
    private const val PAPER = 0xFFF5F1E8.toInt()
    private const val PAPER_SUBTLE = 0xFFFFFDF8.toInt()
    private const val INK = 0xFF23211C.toInt()
    private const val GREEN = 0xFF1B6B3A.toInt()
    private const val ORANGE = 0xFFC96A1B.toInt()
    private const val LINE = 0xFFD8D2C4.toInt()
    private const val GREEN_SOFT = 0xFFE7F2E8.toInt()
    private const val HONEY_SOFT = 0xFFFBEFD6.toInt()
    private const val BEIGE = 0xFFEFE7D8.toInt()

    fun render(card: CommunityCard, widthPx: Int = 1080): Bitmap {
        val width = widthPx.coerceAtLeast(720)
        val height = if (card.kind == CommunityImageKind.CALENDAR) 1700 else 1560
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(PAPER)

        val margin = 48f
        val titlePaint = paint(color = INK, textSize = 44f, bold = true)
        val subtitlePaint = paint(color = INK, textSize = 28f, bold = true)
        val bodyPaint = paint(color = INK, textSize = 28f)
        val smallPaint = paint(color = INK, textSize = 22f)
        val tinyPaint = paint(color = INK, textSize = 18f)
        val mutedPaint = paint(color = 0xFF6B6458.toInt(), textSize = 20f)
        val greenPaint = paint(color = GREEN, textSize = 22f, bold = true)
        val orangePaint = paint(color = ORANGE, textSize = 22f, bold = true)
        val greenTiny = paint(color = GREEN, textSize = 17f, bold = true)
        val orangeTiny = paint(color = ORANGE, textSize = 17f, bold = true)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f; color = LINE }

        var y = margin + 48f
        canvas.drawText(card.title, margin, y, titlePaint)
        y += 40f
        canvas.drawText(card.subtitle, margin, y, subtitlePaint)
        y += 36f
        canvas.drawText(
            "${card.countryName} · ${card.zoneLabel} · hora local ${card.clockLabel}",
            margin, y, mutedPaint,
        )
        y += 34f
        canvas.drawText(card.zoneId, margin, y, mutedPaint)
        y += 34f

        when (card.kind) {
            CommunityImageKind.CALENDAR -> y = drawCalendar(
                canvas, card, y, width - 2 * margin, margin, fill, stroke,
                subtitlePaint, tinyPaint, greenTiny, orangeTiny, mutedPaint,
            )
            CommunityImageKind.WEATHER -> y = drawWeather(
                canvas, card, y, width - 2 * margin, margin, fill, stroke,
                subtitlePaint, bodyPaint, smallPaint, tinyPaint, mutedPaint,
            )
        }

        // Detalle del día + ubicaciones de Fluorita y Liuliu.
        val d = card.selectedDay
        val boxTop = y + 12f
        val boxHeight = 150f
        canvas.drawRoundRect(RectF(margin, boxTop, width - margin, boxTop + boxHeight), 16f, 16f, fill.apply { color = PAPER_SUBTLE })
        canvas.drawRoundRect(RectF(margin, boxTop, width - margin, boxTop + boxHeight), 16f, 16f, stroke)
        canvas.drawText("Día seleccionado: ${d.title}", margin + 20f, boxTop + 40f, subtitlePaint)
        canvas.drawText("Liuliu: ${d.liuliuLabel}", margin + 20f, boxTop + 78f, greenPaint)
        canvas.drawText("Fluorita: ${d.fluritaLabel}", margin + 20f, boxTop + 112f, orangePaint)
        canvas.drawText(
            if (d.hasData) d.certaintyLabel.ifBlank { "Publicado" } else "Sin datos",
            width - margin - 20f, boxTop + 40f, mutedPaint.apply { textAlign = Paint.Align.RIGHT },
        )
        mutedPaint.textAlign = Paint.Align.LEFT
        y = boxTop + boxHeight + 24f

        // Leyenda.
        canvas.drawText("Leyenda", margin, y, subtitlePaint)
        y += 30f
        card.legend.forEach { line ->
            canvas.drawText("• $line", margin + 8f, y, smallPaint)
            y += 28f
        }
        y += 8f

        // Pie honesto.
        canvas.drawLine(margin, y, width - margin, y, stroke)
        y += 32f
        canvas.drawText(card.footer, margin, y, smallPaint.apply { typeface = Typeface.DEFAULT_BOLD })
        smallPaint.typeface = Typeface.DEFAULT
        y += 28f
        if (card.sourceLabel.isNotBlank()) {
            canvas.drawText(card.sourceLabel, margin, y, tinyPaint)
        }
        return bitmap
    }

    private fun drawCalendar(
        canvas: Canvas,
        card: CommunityCard,
        startY: Float,
        gridWidth: Float,
        margin: Float,
        fill: Paint,
        stroke: Paint,
        headerPaint: Paint,
        tinyPaint: Paint,
        greenTiny: Paint,
        orangeTiny: Paint,
        mutedPaint: Paint,
    ): Float {
        var y = startY
        canvas.drawText(card.monthTitle.ifBlank { "Calendario" }, margin, y, headerPaint)
        y += 30f

        val cellW = gridWidth / 7f
        val cellH = (cellW * 0.78f).coerceAtMost(92f)
        val gap = 4f
        val gridTop = y + 30f

        // Cabecera L M X J V S D.
        card.weekLabels.forEachIndexed { i, label ->
            canvas.drawText(label, margin + i * cellW + cellW / 2f, y + 20f, mutedPaint.apply { textAlign = Paint.Align.CENTER })
        }
        mutedPaint.textAlign = Paint.Align.LEFT

        val cells = card.cells
        val rows = if (cells.isEmpty()) 0 else cells.size / 7
        val dayPaint = paint(color = INK, textSize = 22f, bold = true)
        for (row in 0 until rows) {
            for (col in 0 until 7) {
                val cell = cells[row * 7 + col]
                val left = margin + col * cellW + gap / 2f
                val top = gridTop + row * (cellH + gap)
                val rect = RectF(left, top, left + cellW - gap, top + cellH)
                if (cell.date == null) continue
                canvas.drawRoundRect(rect, 10f, 10f, fill.apply { color = if (cell.isSelected) GREEN_SOFT else if (cell.isToday) HONEY_SOFT else PAPER_SUBTLE })
                canvas.drawRoundRect(rect, 10f, 10f, stroke.apply { color = if (cell.isSelected) GREEN else if (cell.isToday) ORANGE else LINE; strokeWidth = if (cell.isSelected || cell.isToday) 3f else 1f })
                canvas.drawText("${cell.dayNumber}", left + 8f, top + 24f, dayPaint)
                cell.liuliuShort?.let { canvas.drawText(it, left + 6f, top + cellH - 8f, greenTiny) }
                cell.fluritaShort?.let { canvas.drawText(it, left + cellW / 2f, top + cellH - 8f, orangeTiny) }
                if (!cell.hasData) canvas.drawText("·", left + cellW - 14f, top + 24f, tinyPaint)
            }
        }
        stroke.color = LINE
        stroke.strokeWidth = 2f
        return gridTop + rows * (cellH + gap) + 12f
    }

    private fun drawWeather(
        canvas: Canvas,
        card: CommunityCard,
        startY: Float,
        width: Float,
        margin: Float,
        fill: Paint,
        stroke: Paint,
        headerPaint: Paint,
        bodyPaint: Paint,
        smallPaint: Paint,
        tinyPaint: Paint,
        mutedPaint: Paint,
    ): Float {
        var y = startY
        canvas.drawText("Clima por franjas del juego", margin, y, headerPaint)
        y += 34f
        if (card.weatherSummary.isNotBlank()) {
            canvas.drawText(card.weatherSummary, margin, y, bodyPaint)
            y += 34f
        }
        val frameH = 92f
        val barH = 18f
        card.frames.forEach { frame ->
            val top = y
            val rect = RectF(margin, top, margin + width, top + frameH)
            canvas.drawRoundRect(rect, 14f, 14f, fill.apply { color = PAPER_SUBTLE })
            canvas.drawRoundRect(rect, 14f, 14f, stroke)
            canvas.drawText("${frame.title} ${frame.timeRange}", margin + 16f, top + 30f, headerPaint)
            canvas.drawText("${frame.label} ${frame.emoji}", margin + 16f, top + 58f, bodyPaint)
            drawBar(canvas, margin + 16f, top + frameH - barH - 8f, width - 32f, barH, frame.segments)
            if (frame.detail.isNotBlank()) {
                canvas.drawText(frame.detail, margin + 16f, top + frameH - 8f, tinyPaint)
            } else if (!frame.hasData) {
                canvas.drawText("Sin datos", margin + width - 16f, top + 30f, mutedPaint.apply { textAlign = Paint.Align.RIGHT })
                mutedPaint.textAlign = Paint.Align.LEFT
            }
            y += frameH + 10f
        }
        y += 4f
        canvas.drawText("Hora local: ${card.zoneLabel}", margin, y, smallPaint)
        return y + 24f
    }

    private fun drawBar(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, segments: List<CommunityClimateSegment>) {
        val total = segments.sumOf { it.weight }.coerceAtLeast(1)
        val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        // fondo
        bar.color = BEIGE
        canvas.drawRoundRect(RectF(left, top, left + width, top + height), height / 2f, height / 2f, bar)
        if (segments.isEmpty()) return
        var x = left
        segments.forEach { seg ->
            val w = width * seg.weight.toFloat() / total.toFloat()
            bar.color = skyColor(seg.kind)
            canvas.drawRect(x, top, x + w, top + height, bar)
            x += w
        }
    }

    private fun skyColor(kind: SkyKind): Int = when (kind.climateDisplay()) {
        SkyKind.LLUVIA -> 0xFF7EB6D6.toInt()
        SkyKind.ARCOIRIS -> 0xFFD7B4E8.toInt()
        SkyKind.NIEVE -> 0xFFE7F2FA.toInt()
        SkyKind.METEORO -> 0xFF2C3A4A.toInt()
        SkyKind.SIN_DATOS -> BEIGE
        else -> 0xFFE9C46A.toInt()
    }

    private fun paint(color: Int, textSize: Float, bold: Boolean = false): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
            this.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            this.style = Paint.Style.FILL
        }
}
