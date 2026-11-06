package com.guiaheartopia.app.pixel

/**
 * Tamaños de cuadrícula.
 *
 * [COMMUNITY] repite conteos que circulan en herramientas de la comunidad
 * (no son una especificación oficial del lienzo de Heartopia). La app debe
 * decirlo. [EXTRA] son tamaños útiles para probar la guía manual.
 */
data class GridSize(val cols: Int, val rows: Int, val label: String) {
    val key: String get() = "${cols}x$rows"

    companion object {
        val COMMUNITY: List<GridSize> = buildList {
            val table = listOf(
                "1:1" to listOf(30 to 30, 50 to 50, 100 to 100, 150 to 150),
                "4:3" to listOf(30 to 24, 50 to 38, 100 to 76, 150 to 114),
                "3:4" to listOf(24 to 30, 38 to 50, 76 to 100, 114 to 150),
                "16:9" to listOf(30 to 18, 50 to 28, 100 to 56, 150 to 84),
                "9:16" to listOf(18 to 30, 28 to 50, 56 to 100, 84 to 150),
            )
            for ((ratio, sizes) in table) {
                sizes.forEachIndexed { i, (w, h) ->
                    add(GridSize(w, h, "Comunidad $ratio · nivel ${i + 1} ($w×$h)"))
                }
            }
        }

        val EXTRA = listOf(
            GridSize(32, 32, "32×32 (prueba)"),
            GridSize(64, 64, "64×64 (prueba)"),
        )

        val ALL = EXTRA + COMMUNITY
    }
}
