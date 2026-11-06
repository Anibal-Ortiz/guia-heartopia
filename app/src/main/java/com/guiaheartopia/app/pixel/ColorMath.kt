package com.guiaheartopia.app.pixel

import kotlin.math.cbrt
import kotlin.math.pow

/** Conversión sRGB -> CIELAB (D65) para comparar colores de forma perceptual. */
object ColorMath {
    private fun lin(c: Float): Float {
        val v = c / 255f
        return if (v <= 0.04045f) v / 12.92f else ((v + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    private fun f(t: Float): Float =
        if (t > 0.008856f) cbrt(t.toDouble()).toFloat() else (7.787f * t + 16f / 116f)

    /** Devuelve [L, a, b] a partir de r,g,b en 0..255 (pueden venir fuera de rango por el dithering). */
    fun rgbToLab(r: Float, g: Float, b: Float, out: FloatArray = FloatArray(3)): FloatArray {
        val rl = lin(r.coerceIn(0f, 255f)); val gl = lin(g.coerceIn(0f, 255f)); val bl = lin(b.coerceIn(0f, 255f))
        val x = (rl * 0.4124f + gl * 0.3576f + bl * 0.1805f) / 0.95047f
        val y = (rl * 0.2126f + gl * 0.7152f + bl * 0.0722f)
        val z = (rl * 0.0193f + gl * 0.1192f + bl * 0.9505f) / 1.08883f
        val fx = f(x); val fy = f(y); val fz = f(z)
        out[0] = 116f * fy - 16f
        out[1] = 500f * (fx - fy)
        out[2] = 200f * (fy - fz)
        return out
    }

    fun argbToLab(c: Int) = rgbToLab(((c shr 16) and 255).toFloat(), ((c shr 8) and 255).toFloat(), (c and 255).toFloat())

    /** Distancia ΔE76 al cuadrado (suficiente para buscar el más cercano). */
    fun dist2(a: FloatArray, b: FloatArray): Float {
        val dl = a[0] - b[0]; val da = a[1] - b[1]; val db = a[2] - b[2]
        return dl * dl + da * da + db * db
    }
}
