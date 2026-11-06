package com.guiaheartopia.app.pixel

import kotlin.math.sqrt

/**
 * Ayuda visual de la guía manual: marca casillas de fondo claro conectadas al borde
 * para que se puedan ocultar en la referencia. No pinta el juego ni verifica el lienzo.
 * El blanco encerrado (brillos) no se marca.
 */
object BackgroundFilter {
    private fun lab(argb: Int) = ColorMath.argbToLab(argb)
    private fun dE(a: FloatArray, b: FloatArray) = sqrt(ColorMath.dist2(a, b))

    /** Blanco / casi blanco / crema (más tolerante que en la conversión: L* > 86, croma < 10). */
    fun isLightNeutral(lab: FloatArray) = lab[0] > 86f && kotlin.math.hypot(lab[1].toDouble(), lab[2].toDouble()) < 10.0

    /** true = esa casilla NO se pinta. */
    fun skipMask(p: PixelProject, skipBackground: Boolean, excludedHex: Set<String> = emptySet()): BooleanArray {
        val n = p.cols * p.rows
        val skip = BooleanArray(n)
        val excluded = BooleanArray(p.colors.size) { p.colors[it].hex.uppercase() in excludedHex.map { h -> h.uppercase() } }
        for (i in 0 until n) { val k = p.cells[i]; if (k >= 0 && excluded[k]) skip[i] = true }
        if (!skipBackground) return skip

        val labs = p.colors.map { lab(it.argb) }
        val bg = BooleanArray(p.colors.size) { isLightNeutral(labs[it]) }
        // Color dominante del borde: si es claro (L* > 80) y ocupa ≥ 40% del borde, también es fondo (± ΔE 6)
        val edge = ArrayList<Int>()
        for (x in 0 until p.cols) { edge += x; edge += (p.rows - 1) * p.cols + x }
        for (y in 1 until p.rows - 1) { edge += y * p.cols; edge += y * p.cols + p.cols - 1 }
        val counts = IntArray(p.colors.size); var filled = 0
        for (i in edge) { val k = p.cells[i]; if (k >= 0) { counts[k]++; filled++ } }
        val dom = counts.indices.maxByOrNull { counts[it] }
        if (dom != null && filled > 0 && counts[dom] >= filled * 0.4f && labs[dom][0] > 80f) {
            for (k in p.colors.indices) if (dE(labs[k], labs[dom]) <= 6f) bg[k] = true
        }
        fun isBg(i: Int) = p.cells[i] < 0 || bg[p.cells[i]]
        val seen = BooleanArray(n)
        val stack = ArrayDeque<Int>()
        for (i in edge) if (isBg(i) && !seen[i]) { seen[i] = true; stack.addLast(i) }
        while (stack.isNotEmpty()) {
            val i = stack.removeLast(); val x = i % p.cols; val y = i / p.cols
            fun visit(j: Int) { if (!seen[j] && isBg(j)) { seen[j] = true; stack.addLast(j) } }
            if (x > 0) visit(i - 1); if (x < p.cols - 1) visit(i + 1)
            if (y > 0) visit(i - p.cols); if (y < p.rows - 1) visit(i + p.cols)
        }
        for (i in 0 until n) if (seen[i] && p.cells[i] >= 0) skip[i] = true
        return skip
    }
}
