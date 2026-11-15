package com.guiaheartopia.app.domain

/**
 * Opciones del filtro de nivel. El tope sale de los niveles del catálogo,
 * no de un número escrito en el código.
 * `null` es «Todos».
 */
object LevelFilter {
    fun maximum(levels: Iterable<Int?>): Int? = levels.mapNotNull { it }.maxOrNull()

    fun options(maximum: Int): List<Int?> {
        val top = maximum.coerceAtLeast(0)
        return listOf(null) + (0..top).toList()
    }
}
