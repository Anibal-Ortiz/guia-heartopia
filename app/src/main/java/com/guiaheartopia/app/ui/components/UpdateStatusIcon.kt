package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.Orange
import kotlin.math.cos
import kotlin.math.sin

/** Estados que puede mostrar el hueco del logo en la cabecera. */
enum class UpdateHeaderState { CHECKING, OK, AVAILABLE, FAILED }

/**
 * Ocupa el lugar del logo de la app mientras se comprueba si hay actualización.
 * En fallo muestra un icono de reintentar (el padre se encarga del click).
 */
@Composable
fun UpdateStatusIcon(state: UpdateHeaderState, modifier: Modifier = Modifier) {
    when (state) {
        UpdateHeaderState.CHECKING -> CircularProgressIndicator(
            modifier = modifier.size(30.dp),
            color = Green,
            trackColor = GreenSoft,
            strokeWidth = 2.5.dp,
        )
        UpdateHeaderState.OK,
        UpdateHeaderState.AVAILABLE,
        UpdateHeaderState.FAILED,
        -> {
            val color = when (state) {
                UpdateHeaderState.OK -> Green
                UpdateHeaderState.AVAILABLE -> Honey
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
                    UpdateHeaderState.AVAILABLE -> {
                        drawLine(
                            Color.White,
                            Offset(c.x, c.y - r * 0.42f),
                            Offset(c.x, c.y + r * 0.26f),
                            stroke,
                            StrokeCap.Round,
                        )
                        drawLine(
                            Color.White,
                            Offset(c.x - r * 0.24f, c.y + r * 0.02f),
                            Offset(c.x, c.y + r * 0.28f),
                            stroke,
                            StrokeCap.Round,
                        )
                        drawLine(
                            Color.White,
                            Offset(c.x + r * 0.24f, c.y + r * 0.02f),
                            Offset(c.x, c.y + r * 0.28f),
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
