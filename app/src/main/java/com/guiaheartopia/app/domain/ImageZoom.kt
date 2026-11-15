package com.guiaheartopia.app.domain

/** Zoom del diálogo de la ficha. Sin librerías: pellizco y doble toque. */
object ImageZoom {
    const val MIN = 1f
    const val MAX = 4f
    const val DOUBLE_TAP = 2.5f

    fun clamp(scale: Float): Float = scale.coerceIn(MIN, MAX)

    fun pinch(scale: Float, zoom: Float): Float = clamp(scale * zoom)

    fun doubleTap(scale: Float): Float = if (scale > MIN + 0.05f) MIN else DOUBLE_TAP

    fun enlargedDescription(name: String): String = "Ver imagen ampliada de $name"
}
