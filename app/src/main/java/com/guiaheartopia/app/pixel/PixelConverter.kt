package com.guiaheartopia.app.pixel

import java.util.ArrayDeque
import kotlin.math.max

/**
 * Convierte una imagen en una guía de pixel art con una paleta fija:
 * 1. box-filter con alfa a la cuadrícula (FIT / CROP / STRETCH o srcRect),
 * 2. color más cercano en CIELAB (ΔE76²),
 * 3. tope opcional de colores por histograma,
 * 4. contornos nítidos (por defecto) o dithering Floyd–Steinberg,
 * 5. el blanco conectado al borde puede quedar vacío.
 *
 * No pinta, no toca el juego y no comprueba un lienzo real.
 */
object PixelConverter : ImageToPixel {

    fun hasTransparency(src: ArgbImage): Boolean {
        for (c in src.pixels) if ((c ushr 24) != 0xFF) return true
        return false
    }

    /** Compone la imagen sobre un fondo opaco. Si ya es opaca, devuelve la misma. */
    fun flatten(src: ArgbImage, bgArgb: Int): ArgbImage {
        if (!hasTransparency(src)) return src
        val bgR = (bgArgb shr 16) and 255
        val bgG = (bgArgb shr 8) and 255
        val bgB = bgArgb and 255
        val out = IntArray(src.pixels.size)
        for (i in src.pixels.indices) {
            val c = src.pixels[i]
            val al = (c ushr 24) and 255
            if (al == 255) {
                out[i] = c
            } else {
                val inv = 255 - al
                val r = (((c shr 16) and 255) * al + bgR * inv) / 255
                val g = (((c shr 8) and 255) * al + bgG * inv) / 255
                val b = ((c and 255) * al + bgB * inv) / 255
                out[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return ArgbImage(src.width, src.height, out)
    }

    fun isNearWhite(lab: FloatArray): Boolean =
        lab[0] > 92f && kotlin.math.hypot(lab[1].toDouble(), lab[2].toDouble()) < 8.0

    override fun convert(src: ArgbImage, palette: List<PaletteColor>, options: ConvertOptions): PixelProject =
        convertImage(src, palette, options)

    fun convertImage(src0: ArgbImage, palette: List<PaletteColor>, o: ConvertOptions): PixelProject {
        val src = if (o.transparentAsEmpty) src0 else flatten(src0, o.borderColorArgb)
        val colors = palette.filter { it.enabled }.ifEmpty { palette }
        require(colors.isNotEmpty()) { "La paleta no tiene colores" }
        val palLab = colors.map { ColorMath.argbToLab(it.argb) }
        val channels = downscale(src, o)
        val r = channels[0]
        val g = channels[1]
        val b = channels[2]
        val a = channels[3]
        val n = o.cols * o.rows
        val empty = BooleanArray(n) { i ->
            (o.transparentAsEmpty && a[i] < 128f) || (o.mask != null && !o.mask[i])
        }
        val tmp = FloatArray(3)

        var allowed = IntArray(colors.size) { it }
        if (o.maxColors in 1 until colors.size) {
            val hist = IntArray(colors.size)
            for (i in 0 until n) {
                if (!empty[i]) {
                    hist[nearest(ColorMath.rgbToLab(r[i], g[i], b[i], tmp), palLab, allowed)]++
                }
            }
            allowed = hist.indices.filter { hist[it] > 0 }.sortedByDescending { hist[it] }
                .take(o.maxColors).ifEmpty { listOf(0) }.toIntArray()
        }

        val out = IntArray(n) { -1 }
        if (o.crispLines && !o.dithering) {
            crisp(src, o, palLab, allowed, empty, out, r, g, b)
        } else {
            val er = r.copyOf()
            val eg = g.copyOf()
            val eb = b.copyOf()
            for (y in 0 until o.rows) {
                for (x in 0 until o.cols) {
                    val i = y * o.cols + x
                    if (empty[i]) continue
                    val k = nearest(ColorMath.rgbToLab(er[i], eg[i], eb[i], tmp), palLab, allowed)
                    out[i] = k
                    if (o.dithering) {
                        val c = colors[k].argb
                        val dr = er[i] - ((c shr 16) and 255)
                        val dg = eg[i] - ((c shr 8) and 255)
                        val db = eb[i] - (c and 255)
                        fun spread(xx: Int, yy: Int, f: Float) {
                            if (xx < 0 || xx >= o.cols || yy >= o.rows) return
                            val j = yy * o.cols + xx
                            if (empty[j]) return
                            er[j] += dr * f
                            eg[j] += dg * f
                            eb[j] += db * f
                        }
                        spread(x + 1, y, 7f / 16)
                        spread(x - 1, y + 1, 3f / 16)
                        spread(x, y + 1, 5f / 16)
                        spread(x + 1, y + 1, 1f / 16)
                    }
                }
            }
        }

        if (o.skipWhiteBackground) {
            val whiteish = BooleanArray(colors.size) { isNearWhite(palLab[it]) }
            val stack = ArrayDeque<Int>()
            val seen = BooleanArray(n)
            fun push(i: Int) {
                if (!seen[i] && out[i] >= 0 && whiteish[out[i]]) {
                    seen[i] = true
                    stack.addLast(i)
                }
            }
            for (x in 0 until o.cols) {
                push(x)
                push((o.rows - 1) * o.cols + x)
            }
            for (y in 0 until o.rows) {
                push(y * o.cols)
                push(y * o.cols + o.cols - 1)
            }
            for (i in 0 until n) {
                if (out[i] < 0) {
                    val x = i % o.cols
                    val y = i / o.cols
                    if (x == 0 || y == 0 || x == o.cols - 1 || y == o.rows - 1) {
                        seen[i] = true
                        stack.addLast(i)
                    }
                }
            }
            while (stack.isNotEmpty()) {
                val i = stack.removeLast()
                val x = i % o.cols
                val y = i / o.cols
                fun visit(j: Int) {
                    if (!seen[j] && (out[j] < 0 || whiteish[out[j]])) {
                        seen[j] = true
                        stack.addLast(j)
                    }
                }
                if (x > 0) visit(i - 1)
                if (x < o.cols - 1) visit(i + 1)
                if (y > 0) visit(i - o.cols)
                if (y < o.rows - 1) visit(i + o.cols)
            }
            for (i in 0 until n) if (seen[i] && out[i] >= 0) out[i] = -1
        }

        val used = out.filter { it >= 0 }.toSortedSet().toList()
        val remap = IntArray(colors.size) { -1 }.also { m -> used.forEachIndexed { ni, oi -> m[oi] = ni } }
        val cells = List(n) { i -> if (out[i] >= 0) remap[out[i]] else -1 }
        return PixelProject(o.cols, o.rows, used.map { colors[it] }, cells, label = o.label)
    }

    private fun downscale(src: ArgbImage, o: ConvertOptions): Array<FloatArray> {
        val sw = src.width
        val sh = src.height
        val px = src.pixels
        val n = o.cols * o.rows
        val r = FloatArray(n)
        val g = FloatArray(n)
        val b = FloatArray(n)
        val a = FloatArray(n)
        val region = region(sw, sh, o)
        val sx0 = region[0]
        val sy0 = region[1]
        val cw = region[2]
        val ch = region[3]
        val br = ((o.borderColorArgb shr 16) and 255).toFloat()
        val bg = ((o.borderColorArgb shr 8) and 255).toFloat()
        val bb = (o.borderColorArgb and 255).toFloat()

        for (cy in 0 until o.rows) {
            for (cx in 0 until o.cols) {
                val x0 = sx0 + cx * cw
                val y0 = sy0 + cy * ch
                val ix0 = x0.toInt()
                val iy0 = y0.toInt()
                val ix1 = max(ix0 + 1, (x0 + cw).toInt())
                val iy1 = max(iy0 + 1, (y0 + ch).toInt())
                var sr = 0f
                var sg = 0f
                var sb = 0f
                var sa = 0f
                var cnt = 0
                for (y in iy0 until iy1) {
                    for (x in ix0 until ix1) {
                        if (x < 0 || y < 0 || x >= sw || y >= sh) {
                            val ba = ((o.borderColorArgb ushr 24) and 255).toFloat()
                            sr += br * ba
                            sg += bg * ba
                            sb += bb * ba
                            sa += ba
                        } else {
                            val c = px[y * sw + x]
                            val al = ((c ushr 24) and 255).toFloat()
                            sr += ((c shr 16) and 255) * al
                            sg += ((c shr 8) and 255) * al
                            sb += (c and 255) * al
                            sa += al
                        }
                        cnt++
                    }
                }
                val i = cy * o.cols + cx
                if (sa > 0f) {
                    r[i] = sr / sa
                    g[i] = sg / sa
                    b[i] = sb / sa
                } else {
                    r[i] = 255f
                    g[i] = 255f
                    b[i] = 255f
                }
                a[i] = if (cnt == 0) 0f else sa / cnt
            }
        }
        return arrayOf(r, g, b, a)
    }

    private fun nearest(lab: FloatArray, pal: List<FloatArray>, allowed: IntArray): Int {
        var best = allowed[0]
        var bd = Float.MAX_VALUE
        for (k in allowed) {
            val d = ColorMath.dist2(lab, pal[k])
            if (d < bd) {
                bd = d
                best = k
            }
        }
        return best
    }

    /** Región fuente: x0, y0, ancho de casilla, alto de casilla. */
    private fun region(sw: Int, sh: Int, o: ConvertOptions): FloatArray {
        o.srcRect?.let { rect ->
            return floatArrayOf(rect[0], rect[1], (rect[2] - rect[0]) / o.cols, (rect[3] - rect[1]) / o.rows)
        }
        val targetRatio = o.cols.toFloat() / o.rows
        val srcRatio = sw.toFloat() / sh
        var x0 = 0f
        var y0 = 0f
        var x1 = sw.toFloat()
        var y1 = sh.toFloat()
        when (o.fitMode) {
            FitMode.CROP -> if (srcRatio > targetRatio) {
                val w = sh * targetRatio
                x0 = (sw - w) / 2f
                x1 = x0 + w
            } else {
                val h = sw / targetRatio
                y0 = (sh - h) / 2f
                y1 = y0 + h
            }
            FitMode.FIT -> if (srcRatio > targetRatio) {
                val h = sw / targetRatio
                y0 = (sh - h) / 2f
                y1 = y0 + h
            } else {
                val w = sh * targetRatio
                x0 = (sw - w) / 2f
                x1 = x0 + w
            }
            FitMode.STRETCH -> Unit
        }
        return floatArrayOf(x0, y0, (x1 - x0) / o.cols, (y1 - y0) / o.rows)
    }

    private fun crisp(
        src: ArgbImage,
        o: ConvertOptions,
        palLab: List<FloatArray>,
        allowed: IntArray,
        empty: BooleanArray,
        out: IntArray,
        ar: FloatArray,
        ag: FloatArray,
        ab: FloatArray,
    ) {
        val sw = src.width
        val sh = src.height
        val px = src.pixels
        val region = region(sw, sh, o)
        val sx0 = region[0]
        val sy0 = region[1]
        val cw = region[2]
        val ch = region[3]
        val cache = HashMap<Int, Int>()
        val tmp = FloatArray(3)
        val borderK = nearest(ColorMath.argbToLab(o.borderColorArgb), palLab, allowed)
        fun classOf(c: Int): Int {
            val key = (c shr 2) and 0x3F3F3F
            return cache.getOrPut(key) {
                nearest(
                    ColorMath.rgbToLab(
                        ((c shr 16) and 255).toFloat(),
                        ((c shr 8) and 255).toFloat(),
                        (c and 255).toFloat(),
                        tmp,
                    ),
                    palLab,
                    allowed,
                )
            }
        }
        val cnt = IntArray(palLab.size)
        val dark = BooleanArray(out.size)
        for (cy in 0 until o.rows) {
            for (cx in 0 until o.cols) {
                val i = cy * o.cols + cx
                if (empty[i]) continue
                val x0 = sx0 + cx * cw
                val y0 = sy0 + cy * ch
                val ix0 = x0.toInt()
                val iy0 = y0.toInt()
                val ix1 = max(ix0 + 1, (x0 + cw).toInt())
                val iy1 = max(iy0 + 1, (y0 + ch).toInt())
                java.util.Arrays.fill(cnt, 0)
                var total = 0
                for (y in iy0 until iy1) {
                    for (x in ix0 until ix1) {
                        val k = if (x < 0 || y < 0 || x >= sw || y >= sh) {
                            if (((o.borderColorArgb ushr 24) and 255) < 128) continue
                            borderK
                        } else {
                            val c = px[y * sw + x]
                            if (((c ushr 24) and 255) < 128) continue
                            classOf(c)
                        }
                        cnt[k]++
                        total++
                    }
                }
                if (total == 0) {
                    out[i] = allowed[0]
                    continue
                }
                var mode = allowed[0]
                for (k in allowed) if (cnt[k] > cnt[mode]) mode = k
                var second = -1
                for (k in allowed) if (k != mode && (second < 0 || cnt[k] > cnt[second])) second = k
                if ((cnt[mode] + (if (second >= 0) cnt[second] else 0)) * 10 < total * 8) {
                    out[i] = nearest(ColorMath.rgbToLab(ar[i], ag[i], ab[i], tmp), palLab, allowed)
                    continue
                }
                var dk = mode
                for (k in allowed) if (cnt[k] * 4 >= total && palLab[k][0] < palLab[dk][0]) dk = k
                if (palLab[dk][0] < palLab[mode][0] - 20f) {
                    out[i] = dk
                    dark[i] = true
                } else {
                    out[i] = mode
                }
            }
        }
        val copy = out.copyOf()
        val nb = IntArray(8)
        for (y in 0 until o.rows) {
            for (x in 0 until o.cols) {
                val i = y * o.cols + x
                if (copy[i] < 0 || dark[i]) continue
                var m = 0
                var same = false
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val xx = x + dx
                        val yy = y + dy
                        if (xx !in 0 until o.cols || yy !in 0 until o.rows) continue
                        val v = copy[yy * o.cols + xx]
                        if (v == copy[i]) same = true
                        if (v >= 0) nb[m++] = v
                    }
                }
                if (same || m == 0) continue
                var best = -1
                var bc = 0
                for (aIdx in 0 until m) {
                    var c = 0
                    for (bIdx in 0 until m) if (nb[bIdx] == nb[aIdx]) c++
                    if (c > bc) {
                        bc = c
                        best = nb[aIdx]
                    }
                }
                if (bc >= 5) out[i] = best
            }
        }
    }
}
