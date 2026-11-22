package com.guiaheartopia.app.ui.weather

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.ui.theme.DarkPalette
import com.guiaheartopia.app.ui.theme.LocalAppPalette
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Decoración animada del clima para la tarjeta de la pantalla principal.
 *
 * Todo se dibuja dentro de un [Canvas] con un progress 0..1: la animación no cambia
 * estado en cada frame, así que la home no se recomponde. Las posiciones se derivan
 * de [seed], determinista, para que las partículas no parpadeen.
 *
 * Los colores se eligen según el tema activo: en modo oscuro la base es oscura para
 * que el texto blanco del tema siga siendo legible.
 */
private const val LOOP_MS = 4200
private const val TAU = 6.2831853f

private fun seed(i: Int): Float {
    val x = sin((i + 1) * 78.233f) * 43758.5453f
    return x - floor(x)
}

private data class WeatherPalette(
    val base: Color,
    val primary: Color,
    val secondary: Color,
    val sparkle: Color,
    val rainbow: List<Color>,
)

private val LightRainbow = listOf(
    Color(0xFFE86A6A),
    Color(0xFFF0A44B),
    Color(0xFFF3D35B),
    Color(0xFF77B87C),
    Color(0xFF6FA8D6),
    Color(0xFFA78BD1),
)

private val DarkRainbow = listOf(
    Color(0xFFFF8F8F),
    Color(0xFFFFC06A),
    Color(0xFFFFE781),
    Color(0xFF9FE0A5),
    Color(0xFF93C6F0),
    Color(0xFFC9AEED),
)

private fun paletteFor(kind: SkyKind, dark: Boolean): WeatherPalette = when (kind) {
    SkyKind.DESPEJADO -> if (dark) {
        WeatherPalette(Color(0xFF3B3020), Color(0xFFFFC24D), Color(0xFFFFD98A), Color(0xFFFFE9B0), emptyList())
    } else {
        WeatherPalette(Color(0xFFFFF6DA), Color(0xFFE9A62B), Color(0xFFFFD05A), Color.White, emptyList())
    }
    SkyKind.LLUVIA -> if (dark) {
        WeatherPalette(Color(0xFF1F3441), Color(0xFF8FC4E4), Color(0xFF5E7D8F), Color(0xFFC7E4F5), emptyList())
    } else {
        WeatherPalette(Color(0xFFE8F4FB), Color(0xFF4E8FB5), Color(0xFF9DB6C6), Color.White, emptyList())
    }
    SkyKind.ARCOIRIS -> if (dark) {
        WeatherPalette(Color(0xFF30283B), Color(0xFFC9AEED), Color(0xFF93C6F0), Color(0xFFF3E4FF), DarkRainbow)
    } else {
        WeatherPalette(Color(0xFFF5EDFB), Color(0xFFA78BD1), Color(0xFF6FA8D6), Color.White, LightRainbow)
    }
    SkyKind.NIEVE -> if (dark) {
        WeatherPalette(Color(0xFF24343F), Color(0xFFCFE6F5), Color(0xFF9FC3DB), Color.White, emptyList())
    } else {
        WeatherPalette(Color(0xFFEDF7FC), Color(0xFF7FA8C4), Color(0xFF7FA8C4), Color.White, emptyList())
    }
    SkyKind.METEORO -> if (dark) {
        WeatherPalette(Color(0xFF1E2530), Color(0xFFAFC1D6), Color(0xFF7E91A8), Color(0xFFFFD76B), emptyList())
    } else {
        WeatherPalette(Color(0xFFE9EDF4), Color(0xFF5B6B7F), Color(0xFF8A9AAD), Color(0xFFFFD76B), emptyList())
    }
    else -> if (dark) {
        WeatherPalette(Color(0xFF2E2924), Color(0xFFC7B79A), Color(0xFFC7B79A), Color(0xFFE8DCC8), emptyList())
    } else {
        WeatherPalette(Color(0xFFF5F0E6), Color(0xFFB9A98C), Color(0xFFB9A98C), Color.White, emptyList())
    }
}

/** Fondo animado que ocupa toda la tarjeta del clima, sin añadir altura. */
@Composable
fun WeatherAmbientBackground(kind: SkyKind, modifier: Modifier = Modifier) {
    val dark = LocalAppPalette.current === DarkPalette
    val palette = paletteFor(kind, dark)
    val infinite = rememberInfiniteTransition(label = "weatherAmbient")
    val p by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(LOOP_MS, easing = LinearEasing), RepeatMode.Restart),
        label = "ambientProgress",
    )
    Canvas(modifier) {
        drawRect(palette.base)
        when (kind) {
            SkyKind.DESPEJADO -> drawSunny(p, palette)
            SkyKind.LLUVIA -> drawRainy(p, palette)
            SkyKind.ARCOIRIS -> drawRainbow(p, palette)
            SkyKind.NIEVE -> drawSnowy(p, palette)
            SkyKind.METEORO -> drawMeteors(p, palette)
            else -> drawCalm(p, palette)
        }
    }
}

private fun DrawScope.drawSunny(p: Float, palette: WeatherPalette) {
    val cx = size.width * 0.82f
    val cy = size.height * 0.3f
    val pulse = 1f + 0.12f * sin(p * TAU)
    val glow = size.minDimension * 0.8f * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.secondary.copy(alpha = 0.45f),
                palette.secondary.copy(alpha = 0f),
            ),
            center = Offset(cx, cy),
            radius = glow,
        ),
        radius = glow,
        center = Offset(cx, cy),
    )
    val rotation = p * TAU
    for (i in 0 until 10) {
        val a = rotation + i * TAU / 10f
        val inner = size.minDimension * 0.18f
        val outer = size.minDimension * (0.3f + 0.03f * sin(p * TAU + i))
        drawLine(
            color = palette.primary.copy(alpha = 0.26f),
            start = Offset(cx + cos(a) * inner, cy + sin(a) * inner),
            end = Offset(cx + cos(a) * outer, cy + sin(a) * outer),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
    for (i in 0 until 9) {
        val x = seed(i) * size.width
        val y = ((seed(i + 40) + p * 0.35f) % 1f) * size.height
        val alpha = 0.15f + 0.25f * (0.5f + 0.5f * sin(p * TAU * 2 + i))
        drawCircle(palette.sparkle.copy(alpha = alpha), radius = (1f + seed(i + 60)) * 1.6f, center = Offset(x, y))
    }
}

private fun DrawScope.drawRainy(p: Float, palette: WeatherPalette) {
    repeat(3) { i ->
        val cx = (((seed(i) + p * 0.22f) % 1.4f) - 0.2f) * size.width
        val cy = size.height * (0.14f + 0.08f * seed(i + 9))
        drawCloud(cx, cy, size.minDimension * 0.16f, palette.secondary.copy(alpha = 0.22f))
    }
    repeat(22) { i ->
        val s = seed(i + 100)
        val speed = 0.7f + seed(i + 150) * 0.9f
        val y = ((s + p * speed) % 1f) * size.height
        val x = seed(i + 150) * size.width
        drawLine(
            palette.primary.copy(alpha = 0.20f + 0.18f * s),
            Offset(x, y),
            Offset(x - 2.dp.toPx(), y + size.height * 0.09f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
    repeat(4) { i ->
        val x = seed(i + 200) * size.width
        val alpha = 0.12f + 0.12f * (0.5f + 0.5f * sin(p * TAU + i))
        drawOval(
            palette.primary.copy(alpha = alpha),
            topLeft = Offset(x - 8.dp.toPx(), size.height - 4.dp.toPx()),
            size = Size(16.dp.toPx(), 3.dp.toPx()),
        )
    }
}

private fun DrawScope.drawCloud(cx: Float, cy: Float, r: Float, color: Color) {
    drawCircle(color, r, Offset(cx - r * 0.7f, cy + r * 0.2f))
    drawCircle(color, r * 1.1f, Offset(cx, cy))
    drawCircle(color, r * 0.8f, Offset(cx + r * 0.8f, cy + r * 0.25f))
    drawRect(color, topLeft = Offset(cx - r, cy), size = Size(r * 2f, r))
}

private fun DrawScope.drawRainbow(p: Float, palette: WeatherPalette) {
    val shift = sin(p * TAU) * size.width * 0.03f
    palette.rainbow.forEachIndexed { i, color ->
        val r = size.minDimension * 0.28f + i * 6.dp.toPx()
        drawArc(
            color = color.copy(alpha = 0.16f + 0.10f * (0.5f + 0.5f * sin(p * TAU * 2 - i * 0.5f))),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.5f - r + shift, size.height * 0.95f - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(width = 4.dp.toPx()),
        )
    }
    repeat(12) { i ->
        val phase = (seed(i + 350) + p * 0.4f) % 1f
        val alpha = 0.15f + 0.3f * (1f - phase)
        drawCircle(
            palette.sparkle.copy(alpha = alpha),
            radius = 1.4.dp.toPx(),
            center = Offset(seed(i + 300) * size.width, size.height - phase * size.height),
        )
    }
}

private fun DrawScope.drawSnowy(p: Float, palette: WeatherPalette) {
    repeat(24) { i ->
        val speed = 0.25f + seed(i + 450) * 0.35f
        val y = ((seed(i + 400) + p * speed) % 1f) * size.height
        val x = seed(i + 450) * size.width + sin(p * TAU + i) * 6.dp.toPx()
        drawCircle(
            palette.primary.copy(alpha = 0.25f + 0.25f * seed(i + 400)),
            radius = (1.2f + seed(i + 400) * 1.8f).dp.toPx(),
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.drawMeteors(p: Float, palette: WeatherPalette) {
    repeat(5) { i ->
        val s = seed(i + 500)
        val local = (p * 2f + s) % 1f
        if (local > 0.42f) return@repeat
        val alpha = (1f - local / 0.42f) * 0.5f
        val x = seed(i + 550) * size.width
        val y = s * size.height * 0.6f
        val len = size.width * 0.18f
        drawLine(
            palette.primary.copy(alpha = alpha),
            Offset(x, y),
            Offset(x + len, y + len * 0.55f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawCircle(palette.sparkle.copy(alpha = alpha), radius = 2.dp.toPx(), center = Offset(x, y))
    }
    repeat(10) { i ->
        val alpha = 0.1f + 0.18f * (0.5f + 0.5f * sin(p * TAU * 2 + i))
        drawCircle(
            palette.secondary.copy(alpha = alpha),
            radius = 1.dp.toPx(),
            center = Offset(seed(i + 600) * size.width, seed(i + 650) * size.height),
        )
    }
}

private fun DrawScope.drawCalm(p: Float, palette: WeatherPalette) {
    repeat(8) { i ->
        val alpha = 0.08f + 0.10f * (0.5f + 0.5f * sin(p * TAU + i))
        drawCircle(
            palette.primary.copy(alpha = alpha),
            radius = 2.dp.toPx(),
            center = Offset(seed(i + 700) * size.width, seed(i + 750) * size.height),
        )
    }
}

/** Icono animado que va junto al título "Clima de hoy", dentro de la fila existente. */
@Composable
fun WeatherGlyphAnimated(kind: SkyKind, modifier: Modifier = Modifier) {
    val dark = LocalAppPalette.current === DarkPalette
    val palette = paletteFor(kind, dark)
    val infinite = rememberInfiniteTransition(label = "weatherGlyph")
    val p by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "glyphProgress",
    )
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = center
        drawCircle(palette.base, radius = r, center = c)
        drawCircle(palette.primary.copy(alpha = 0.6f), radius = r, center = c, style = Stroke(width = 1.dp.toPx()))
        when (kind) {
            SkyKind.DESPEJADO -> {
                drawCircle(palette.secondary, radius = r * 0.34f, center = c)
                for (i in 0 until 8) {
                    val a = p * TAU + i * TAU / 8f
                    drawLine(
                        palette.primary,
                        Offset(c.x + cos(a) * r * 0.5f, c.y + sin(a) * r * 0.5f),
                        Offset(c.x + cos(a) * r * 0.68f, c.y + sin(a) * r * 0.68f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            SkyKind.LLUVIA -> {
                drawCloud(c.x, c.y - r * 0.2f, r * 0.42f, palette.secondary)
                repeat(3) { i ->
                    val y = c.y + r * (0.15f + ((p + i * 0.33f) % 1f) * 0.55f)
                    drawLine(
                        palette.primary,
                        Offset(c.x - r * 0.4f + i * r * 0.4f, y),
                        Offset(c.x - r * 0.4f + i * r * 0.4f, y + r * 0.2f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            SkyKind.ARCOIRIS -> {
                palette.rainbow.take(3).forEachIndexed { i, tone ->
                    drawArc(
                        tone.copy(alpha = 0.6f + 0.4f * (0.5f + 0.5f * sin(p * TAU - i))),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(c.x - r * (0.8f - i * 0.2f), c.y - r * (0.8f - i * 0.2f)),
                        size = Size(r * (1.6f - i * 0.4f), r * (1.6f - i * 0.4f)),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
            SkyKind.NIEVE -> {
                val a = p * TAU * 0.35f
                repeat(3) { i ->
                    val angle = a + i * TAU / 3f
                    drawLine(
                        palette.primary,
                        Offset(c.x - cos(angle) * r * 0.55f, c.y - sin(angle) * r * 0.55f),
                        Offset(c.x + cos(angle) * r * 0.55f, c.y + sin(angle) * r * 0.55f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            SkyKind.METEORO -> {
                val alpha = 0.35f + 0.65f * (0.5f + 0.5f * sin(p * TAU * 2))
                drawLine(
                    palette.primary.copy(alpha = alpha),
                    Offset(c.x - r * 0.5f, c.y - r * 0.5f),
                    Offset(c.x + r * 0.45f, c.y + r * 0.45f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            else -> {
                drawCircle(
                    palette.primary.copy(alpha = 0.5f + 0.4f * (0.5f + 0.5f * sin(p * TAU))),
                    radius = r * 0.22f,
                    center = c,
                )
            }
        }
    }
}

/** Reflejo que recorre la barra de franjas sin cambiar su altura. */
@Composable
fun WeatherBarShimmer(modifier: Modifier = Modifier) {
    val dark = LocalAppPalette.current === DarkPalette
    val infinite = rememberInfiniteTransition(label = "weatherShimmer")
    val p by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    Canvas(modifier) {
        val band = size.width * 0.35f
        val x = p * (size.width + band) - band
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = if (dark) 0.18f else 0.45f),
                    Color.Transparent,
                ),
                startX = x,
                endX = x + band,
            ),
            topLeft = Offset(x, 0f),
            size = Size(band, size.height),
        )
    }
}
