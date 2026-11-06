package com.guiaheartopia.app.pixel

import kotlin.math.floor
import kotlin.math.min

/**
 * Matemáticas del visor de la guía manual: zoom hacia una casilla y toque → celda.
 * Misma convención que el visor: pantalla = offset + (origen + celda · base) · escala.
 */
object GuideViewport {
    fun zoomTo(
        viewW: Float,
        viewH: Float,
        cols: Int,
        rows: Int,
        cx: Float,
        cy: Float,
        span: Float = 24f,
    ): Triple<Float, Float, Float> {
        val base = min(viewW / cols, viewH / rows)
        val ox = (viewW - cols * base) / 2f
        val oy = (viewH - rows * base) / 2f
        val scale = (min(viewW, viewH) / (span * base)).coerceIn(1f, 40f)
        val offsetX = viewW / 2f - (ox + cx * base) * scale
        val offsetY = viewH / 2f - (oy + cy * base) * scale
        return Triple(scale, offsetX, offsetY)
    }

    fun cellAt(
        viewW: Float,
        viewH: Float,
        cols: Int,
        rows: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        touchX: Float,
        touchY: Float,
    ): Pair<Int, Int>? {
        if (viewW <= 0f || viewH <= 0f || cols <= 0 || rows <= 0 || scale <= 0f) return null
        val base = min(viewW / cols, viewH / rows)
        val ox = (viewW - cols * base) / 2f
        val oy = (viewH - rows * base) / 2f
        val cell = base * scale
        if (cell <= 0f) return null
        val gx = floor((touchX - offsetX - ox * scale) / cell).toInt()
        val gy = floor((touchY - offsetY - oy * scale) / cell).toInt()
        if (gx !in 0 until cols || gy !in 0 until rows) return null
        return gx to gy
    }
}
