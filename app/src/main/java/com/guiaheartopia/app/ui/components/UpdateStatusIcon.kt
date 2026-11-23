package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Orange
import kotlin.math.cos
import kotlin.math.sin

/** Estados que puede mostrar el hueco del logo en la cabecera. */
enum class UpdateHeaderState { CHECKING, OK, FAILED }

/**
 * Ocupa el lugar del logo de la app mientras se comprueba si hay actualización.
 * En fallo muestra un icono de reintentar (el padre se encarga del click).
 * [percent] solo se usa durante la carga de contenido para mostrar el progreso.
 */
@Composable
fun UpdateStatusIcon(
    state: UpdateHeaderState,
    modifier: Modifier = Modifier,
    percent: Int? = null,
) {
    when (state) {
        UpdateHeaderState.CHECKING -> Box(
            modifier = modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (percent != null) {
                CircularProgressIndicator(
                    progress = { percent.coerceAtLeast(2) / 100f },
                    modifier = Modifier.size(32.dp),
                    color = Green,
                    trackColor = GreenSoft,
                    strokeWidth = 2.5.dp,
                )
                Text(
                    "$percent%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Green,
                    trackColor = GreenSoft,
                    strokeWidth = 2.5.dp,
                )
            }
        }
        UpdateHeaderState.OK,
        UpdateHeaderState.FAILED,
        -> {
            val color = when (state) {
                UpdateHeaderState.OK -> Green
                UpdateHeaderState.FAILED -> Orange
            }
            Canvas(modifier.size(36.dp)) {
                val r = size.minDimension / 2f
                val c = center
                drawCircle(color, radius = r, center = c)
                drawCircle(
                    Color.White.copy(alpha = 0.45f),
                    radius = r,
                    center = c,
                    style = Stroke(width = 1.dp.toPx()),
                )
                val stroke = 2.5.dp.toPx()
                when (state) {
                    UpdateHeaderState.OK -> {
                        drawLine(
                            Color.White,
                            Offset(c.x - r * 0.38f, c.y),
                            Offset(c.x - r * 0.08f, c.y + r * 0.3f),
                            stroke,
                            StrokeCap.Round,
                        )
                        drawLine(
                            Color.White,
                            Offset(c.x - r * 0.08f, c.y + r * 0.3f),
                            Offset(c.x + r * 0.42f, c.y - r * 0.32f),
                            stroke,
                            StrokeCap.Round,
                        )
                    }
                    UpdateHeaderState.FAILED -> {
                        val start = 250f
                        val sweep = 300f
                        drawArc(
                            Color.White,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(c.x - r * 0.55f, c.y - r * 0.55f),
                            size = Size(r * 1.1f, r * 1.1f),
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        val a = Math.toRadians((start + sweep).toDouble())
                        val ex = c.x + cos(a).toFloat() * r * 0.55f
                        val ey = c.y + sin(a).toFloat() * r * 0.55f
                        val head = r * 0.28f
                        drawLine(
                            Color.White,
                            Offset(ex, ey),
                            Offset(ex - cos(a - 0.55).toFloat() * head, ey - sin(a - 0.55).toFloat() * head),
                            stroke,
                            StrokeCap.Round,
                        )
                        drawLine(
                            Color.White,
                            Offset(ex, ey),
                            Offset(ex - cos(a + 0.55).toFloat() * head, ey - sin(a + 0.55).toFloat() * head),
                            stroke,
                            StrokeCap.Round,
                        )
                    }
                    UpdateHeaderState.CHECKING -> Unit
                }
            }
        }
    }
}
