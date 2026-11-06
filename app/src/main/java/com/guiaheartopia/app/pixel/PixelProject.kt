package com.guiaheartopia.app.pixel

import kotlinx.serialization.Serializable

/**
 * Resultado de una conversión para la guía manual.
 * [cells] es el índice en [colors], fila a fila, o -1 si la casilla queda vacía.
 * No guarda progreso de pintado, verificación ni estado de un lienzo del juego.
 */
@Serializable
data class PixelProject(
    val cols: Int,
    val rows: Int,
    val colors: List<PaletteColor>,
    val cells: List<Int>,
    val label: String = "${cols}×$rows",
) {
    init {
        require(cells.size == cols * rows) { "La cuadrícula no coincide con cols×rows" }
    }

    fun index(x: Int, y: Int) = y * cols + x

    fun colorAt(x: Int, y: Int): Int = cells[index(x, y)]

    fun counts(): IntArray {
        val c = IntArray(colors.size)
        for (v in cells) if (v >= 0) c[v]++
        return c
    }

    val paintableCount: Int get() = cells.count { it >= 0 }
}

/** Imagen ARGB sin dependencias de Android, para poder probar el conversor. */
class ArgbImage(
    val width: Int,
    val height: Int,
    val pixels: IntArray,
) {
    init {
        require(width > 0 && height > 0 && pixels.size == width * height)
    }

    fun pixel(x: Int, y: Int): Int = pixels[y * width + x]
}
