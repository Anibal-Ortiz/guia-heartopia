package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.R

/** Mismo diseño que el ícono adaptativo del launcher. */
@Composable
fun WikiFanAppIcon(modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape)) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun CozySettingsIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val outerR = w * 0.36f
        val innerR = w * 0.28f
        val strokeWidth = 1.8.dp.toPx()

        for (i in 0 until 6) {
            val angle = i * Math.PI / 3.0
            val x1 = (cx + innerR * Math.cos(angle)).toFloat()
            val y1 = (cy + innerR * Math.sin(angle)).toFloat()
            val x2 = (cx + (outerR + 2.dp.toPx()) * Math.cos(angle)).toFloat()
            val y2 = (cy + (outerR + 2.dp.toPx()) * Math.sin(angle)).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2.8.dp.toPx(), cap = StrokeCap.Round)
        }

        drawCircle(tint, outerR * 0.85f, Offset(cx, cy), style = Stroke(strokeWidth))
        drawCircle(tint, outerR * 0.32f, Offset(cx, cy), style = Stroke(strokeWidth))
    }
}

@Composable
fun CozyHomeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 2.dp.toPx()
        val style = if (filled) Fill else Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Chimney
        val chimney = Path().apply {
            moveTo(w * 0.72f, h * 0.38f)
            lineTo(w * 0.72f, h * 0.20f)
            lineTo(w * 0.84f, h * 0.20f)
            lineTo(w * 0.84f, h * 0.48f)
        }
        drawPath(chimney, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Roof
        val roof = Path().apply {
            moveTo(w * 0.12f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.88f, h * 0.46f)
        }
        drawPath(roof, tint, style = Stroke(strokeWidth * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // House walls
        val walls = Path().apply {
            moveTo(w * 0.22f, h * 0.42f)
            lineTo(w * 0.22f, h * 0.84f)
            lineTo(w * 0.78f, h * 0.84f)
            lineTo(w * 0.78f, h * 0.42f)
        }
        drawPath(walls, tint, style = style)

        // Door
        val door = Path().apply {
            moveTo(w * 0.40f, h * 0.84f)
            lineTo(w * 0.40f, h * 0.58f)
            lineTo(w * 0.60f, h * 0.58f)
            lineTo(w * 0.60f, h * 0.84f)
        }
        drawPath(door, if (filled) Color.White.copy(alpha = 0.85f) else tint, style = Stroke(strokeWidth * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CozyRoutineIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 2.dp.toPx()

        // Board outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.20f, h * 0.20f),
            size = Size(w * 0.60f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = if (filled) Fill else Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Clip on top
        drawRoundRect(
            color = if (filled) Color.White else tint,
            topLeft = Offset(w * 0.36f, h * 0.12f),
            size = Size(w * 0.28f, h * 0.14f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
            style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        val checkColor = if (filled) Color.White else tint

        // Check line 1
        val check1 = Path().apply {
            moveTo(w * 0.32f, h * 0.42f)
            lineTo(w * 0.40f, h * 0.49f)
            lineTo(w * 0.50f, h * 0.38f)
        }
        drawPath(check1, checkColor, style = Stroke(strokeWidth * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(checkColor, Offset(w * 0.56f, h * 0.44f), Offset(w * 0.68f, h * 0.44f), strokeWidth = strokeWidth * 0.9f, cap = StrokeCap.Round)

        // Check line 2
        val check2 = Path().apply {
            moveTo(w * 0.32f, h * 0.60f)
            lineTo(w * 0.40f, h * 0.67f)
            lineTo(w * 0.50f, h * 0.56f)
        }
        drawPath(check2, checkColor, style = Stroke(strokeWidth * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(checkColor, Offset(w * 0.56f, h * 0.62f), Offset(w * 0.68f, h * 0.62f), strokeWidth = strokeWidth * 0.9f, cap = StrokeCap.Round)
    }
}

@Composable
fun CozyGuideIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 2.dp.toPx()

        val leftPage = Path().apply {
            moveTo(w * 0.50f, h * 0.78f)
            cubicTo(w * 0.36f, h * 0.74f, w * 0.24f, h * 0.76f, w * 0.16f, h * 0.80f)
            lineTo(w * 0.16f, h * 0.26f)
            cubicTo(w * 0.26f, h * 0.22f, w * 0.38f, h * 0.20f, w * 0.50f, h * 0.25f)
            close()
        }

        val rightPage = Path().apply {
            moveTo(w * 0.50f, h * 0.78f)
            cubicTo(w * 0.64f, h * 0.74f, w * 0.76f, h * 0.76f, w * 0.84f, h * 0.80f)
            lineTo(w * 0.84f, h * 0.26f)
            cubicTo(w * 0.74f, h * 0.22f, w * 0.62f, h * 0.20f, w * 0.50f, h * 0.25f)
            close()
        }

        if (filled) {
            drawPath(leftPage, tint, style = Fill)
            drawPath(rightPage, tint, style = Fill)
            val ribbon = Path().apply {
                moveTo(w * 0.47f, h * 0.25f)
                lineTo(w * 0.53f, h * 0.25f)
                lineTo(w * 0.53f, h * 0.56f)
                lineTo(w * 0.50f, h * 0.50f)
                lineTo(w * 0.47f, h * 0.56f)
                close()
            }
            drawPath(ribbon, Color.White, style = Fill)
        } else {
            drawPath(leftPage, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(rightPage, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawLine(tint, Offset(w * 0.50f, h * 0.25f), Offset(w * 0.50f, h * 0.78f), strokeWidth = strokeWidth * 0.9f)
        }
    }
}

@Composable
fun CozyPixelIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 2.dp.toPx()

        val palette = Path().apply {
            moveTo(w * 0.48f, h * 0.16f)
            cubicTo(w * 0.78f, h * 0.16f, w * 0.88f, h * 0.38f, w * 0.86f, h * 0.58f)
            cubicTo(w * 0.84f, h * 0.76f, w * 0.72f, h * 0.84f, w * 0.56f, h * 0.84f)
            cubicTo(w * 0.44f, h * 0.84f, w * 0.40f, h * 0.74f, w * 0.32f, h * 0.74f)
            cubicTo(w * 0.24f, h * 0.74f, w * 0.22f, h * 0.82f, w * 0.16f, h * 0.78f)
            cubicTo(w * 0.10f, h * 0.72f, w * 0.10f, h * 0.42f, w * 0.24f, h * 0.24f)
            cubicTo(w * 0.30f, h * 0.18f, w * 0.40f, h * 0.16f, w * 0.48f, h * 0.16f)
            close()
        }

        if (filled) {
            drawPath(palette, tint, style = Fill)
            val spotColor = Color.White
            drawCircle(spotColor, w * 0.055f, Offset(w * 0.42f, h * 0.32f))
            drawCircle(spotColor, w * 0.055f, Offset(w * 0.64f, h * 0.34f))
            drawCircle(spotColor, w * 0.055f, Offset(w * 0.72f, h * 0.54f))
            drawCircle(Color.White, w * 0.065f, Offset(w * 0.30f, h * 0.60f))
        } else {
            drawPath(palette, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(tint, w * 0.045f, Offset(w * 0.42f, h * 0.32f))
            drawCircle(tint, w * 0.045f, Offset(w * 0.64f, h * 0.34f))
            drawCircle(tint, w * 0.045f, Offset(w * 0.72f, h * 0.54f))
            drawCircle(tint, w * 0.055f, Offset(w * 0.30f, h * 0.60f), style = Stroke(strokeWidth * 0.8f))
        }
    }
}

@Composable
fun CozyHobbiesIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 2.dp.toPx()

        val star = Path().apply {
            val cx = w * 0.50f
            val cy = h * 0.48f
            val outerR = w * 0.36f
            val innerR = w * 0.17f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = (Math.PI / 5.0 * i - Math.PI / 2.0)
                val x = cx + (r * Math.cos(angle)).toFloat()
                val y = cy + (r * Math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

        if (filled) {
            drawPath(star, tint, style = Fill)
            drawCircle(Color.White, w * 0.08f, Offset(w * 0.50f, h * 0.48f))
        } else {
            drawPath(star, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(tint, w * 0.07f, Offset(w * 0.50f, h * 0.48f))
        }
    }
}

@Composable
fun CozyCrystalIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF2C8255),
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val gem = Path().apply {
            moveTo(w * 0.50f, h * 0.10f)
            lineTo(w * 0.84f, h * 0.38f)
            lineTo(w * 0.50f, h * 0.90f)
            lineTo(w * 0.16f, h * 0.38f)
            close()
        }
        drawPath(gem, tint.copy(alpha = 0.22f), style = Fill)
        drawPath(gem, tint, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.16f, h * 0.38f), Offset(w * 0.84f, h * 0.38f), strokeWidth = 1.4.dp.toPx())
        drawLine(tint, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.90f), strokeWidth = 1.4.dp.toPx())
    }
}

@Composable
fun CozyTreeIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFE76235),
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val crown = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.46f)
            lineTo(w * 0.68f, h * 0.46f)
            lineTo(w * 0.86f, h * 0.72f)
            lineTo(w * 0.14f, h * 0.72f)
            lineTo(w * 0.32f, h * 0.46f)
            lineTo(w * 0.18f, h * 0.46f)
            close()
        }
        drawPath(crown, tint.copy(alpha = 0.25f), style = Fill)
        drawPath(crown, tint, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.50f, h * 0.72f), Offset(w * 0.50f, h * 0.92f), strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun CozyCalendarIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        val style = if (filled) Fill else Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.18f),
            size = Size(w * 0.76f, h * 0.70f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = style,
        )
        drawLine(tint, Offset(w * 0.12f, h * 0.34f), Offset(w * 0.88f, h * 0.34f), stroke)
        drawLine(tint, Offset(w * 0.30f, h * 0.10f), Offset(w * 0.30f, h * 0.26f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.70f, h * 0.10f), Offset(w * 0.70f, h * 0.26f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun CozySunIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFF39C12),
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val r = w * 0.24f
        drawCircle(tint.copy(alpha = 0.25f), r, center)
        drawCircle(tint, r, center, style = Stroke(1.8.dp.toPx()))

        val rayLen = w * 0.12f
        val startR = r + 2.dp.toPx()
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            val x1 = (center.x + startR * Math.cos(angle)).toFloat()
            val y1 = (center.y + startR * Math.sin(angle)).toFloat()
            val x2 = (center.x + (startR + rayLen) * Math.cos(angle)).toFloat()
            val y2 = (center.y + (startR + rayLen) * Math.sin(angle)).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
fun CozyMoonIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF7E57C2),
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val moon = Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.20f, w * 0.20f, h * 0.75f, w * 0.62f, h * 0.85f)
            cubicTo(w * 0.40f, h * 0.70f, w * 0.42f, h * 0.35f, w * 0.65f, h * 0.15f)
            close()
        }
        drawPath(moon, tint.copy(alpha = 0.22f), style = Fill)
        drawPath(moon, tint, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CaptureNetIcon(
    on: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    val ink = if (on) Color(0xFF2C554F) else Color(0xFFB7A48C)
    val fill = if (on) Color(0xFFD7E6E3) else Color.Transparent
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        val center = Offset(w * 0.40f, h * 0.38f)
        val radius = w * 0.24f
        drawCircle(fill, radius, center)
        drawCircle(ink, radius, center, style = Stroke(stroke))
        val mesh = radius * 0.55f
        drawLine(ink, Offset(center.x - mesh, center.y), Offset(center.x + mesh, center.y), stroke * 0.7f)
        drawLine(ink, Offset(center.x, center.y - mesh), Offset(center.x, center.y + mesh), stroke * 0.7f)
        drawLine(
            ink,
            Offset(center.x + radius * 0.7f, center.y + radius * 0.7f),
            Offset(w * 0.82f, h * 0.86f),
            stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun NonCertifiableIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    val ink = Color(0xFF9A9A9A)
    Canvas(modifier.size(size).semantics { contentDescription = "No certificable" }) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        val center = Offset(w * 0.50f, h * 0.40f)
        val radius = w * 0.24f
        drawCircle(Color.Transparent, radius, center, style = Stroke(stroke))
        drawCircle(ink, radius, center, style = Stroke(stroke))
        drawLine(
            ink,
            Offset(center.x - radius * 0.75f, center.y + radius * 0.65f),
            Offset(center.x + radius * 0.75f, center.y - radius * 0.65f),
            stroke,
            cap = StrokeCap.Round,
        )
        val ribbon = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.30f, h * 0.90f)
            lineTo(w * 0.50f, h * 0.78f)
            lineTo(w * 0.70f, h * 0.90f)
            lineTo(w * 0.64f, h * 0.50f)
            close()
        }
        drawPath(ribbon, Color.Transparent, style = Stroke(stroke * 0.75f, cap = StrokeCap.Round))
    }
}

@Composable
fun CertificateMedalIcon(
    on: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    val ink = if (on) Color(0xFF8C6A2F) else Color(0xFFB7A48C)
    val fill = if (on) Color(0xFFE7C56A) else Color.Transparent
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.8.dp.toPx()
        val ribbon = Path().apply {
            moveTo(w * 0.36f, h * 0.46f)
            lineTo(w * 0.30f, h * 0.92f)
            lineTo(w * 0.50f, h * 0.76f)
            lineTo(w * 0.70f, h * 0.92f)
            lineTo(w * 0.64f, h * 0.46f)
            close()
        }
        drawPath(ribbon, if (on) Color(0xFFC4A15A) else Color.Transparent, style = Fill)
        drawPath(ribbon, ink, style = Stroke(stroke * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val center = Offset(w * 0.50f, h * 0.36f)
        val radius = w * 0.24f
        drawCircle(fill, radius, center)
        drawCircle(ink, radius, center, style = Stroke(stroke))
        drawCircle(ink, radius * 0.45f, center, style = Stroke(stroke * 0.7f))
    }
}

/** Sol de cuaderno para el clima despejado. No es el sol de la franja Día. */
@Composable
fun ClearSkyIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFFE39B2B),
    size: Dp = 16.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val center = Offset(w * 0.5f, w * 0.5f)
        val radius = w * 0.22f
        drawCircle(tint.copy(alpha = 0.35f), radius, center)
        drawCircle(tint, radius, center, style = Stroke(1.4.dp.toPx()))
        val inner = radius + 1.5.dp.toPx()
        val outer = radius + w * 0.16f
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            drawLine(
                tint,
                Offset((center.x + inner * Math.cos(angle)).toFloat(), (center.y + inner * Math.sin(angle)).toFloat()),
                Offset((center.x + outer * Math.cos(angle)).toFloat(), (center.y + outer * Math.sin(angle)).toFloat()),
                strokeWidth = 1.3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
fun SproutIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF2C554F),
    size: Dp = 36.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        drawLine(tint, Offset(w * 0.50f, h * 0.88f), Offset(w * 0.50f, h * 0.42f), stroke, cap = StrokeCap.Round)
        val leaf = Path().apply {
            moveTo(w * 0.50f, h * 0.48f)
            cubicTo(w * 0.78f, h * 0.28f, w * 0.86f, h * 0.18f, w * 0.70f, h * 0.14f)
            cubicTo(w * 0.52f, h * 0.18f, w * 0.48f, h * 0.32f, w * 0.50f, h * 0.48f)
            close()
        }
        drawPath(leaf, tint.copy(alpha = 0.25f), style = Fill)
        drawPath(leaf, tint, style = Stroke(stroke * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val left = Path().apply {
            moveTo(w * 0.50f, h * 0.58f)
            cubicTo(w * 0.22f, h * 0.42f, w * 0.14f, h * 0.30f, w * 0.30f, h * 0.26f)
            cubicTo(w * 0.48f, h * 0.30f, w * 0.52f, h * 0.44f, w * 0.50f, h * 0.58f)
            close()
        }
        drawPath(left, tint.copy(alpha = 0.25f), style = Fill)
        drawPath(left, tint, style = Stroke(stroke * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CookingPotIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF8C6A2F),
    size: Dp = 36.dp,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        drawRoundRect(
            color = tint.copy(alpha = 0.2f),
            topLeft = Offset(w * 0.22f, h * 0.42f),
            size = Size(w * 0.56f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Fill,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.42f),
            size = Size(w * 0.56f, h * 0.40f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = Stroke(stroke),
        )
        drawLine(tint, Offset(w * 0.12f, h * 0.48f), Offset(w * 0.22f, h * 0.48f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.78f, h * 0.48f), Offset(w * 0.88f, h * 0.48f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.38f, h * 0.42f), Offset(w * 0.34f, h * 0.22f), stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.62f, h * 0.42f), Offset(w * 0.66f, h * 0.22f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun CozyTicketIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
    filled: Boolean = false,
) {
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 2.dp.toPx()
        val ticket = Path().apply {
            moveTo(w * 0.16f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.28f)
            lineTo(w * 0.84f, h * 0.46f)
            cubicTo(w * 0.72f, h * 0.46f, w * 0.72f, h * 0.54f, w * 0.84f, h * 0.54f)
            lineTo(w * 0.84f, h * 0.74f)
            lineTo(w * 0.16f, h * 0.74f)
            lineTo(w * 0.16f, h * 0.54f)
            cubicTo(w * 0.28f, h * 0.54f, w * 0.28f, h * 0.46f, w * 0.16f, h * 0.46f)
            close()
        }
        if (filled) drawPath(ticket, tint, style = Fill)
        drawPath(ticket, tint, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.32f, h * 0.40f), Offset(w * 0.68f, h * 0.40f), stroke * 0.7f, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.32f, h * 0.52f), Offset(w * 0.56f, h * 0.52f), stroke * 0.7f, cap = StrokeCap.Round)
    }
}
