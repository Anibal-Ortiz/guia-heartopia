package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Moss
import com.guiaheartopia.app.ui.theme.Ochre

enum class NotebookMark { HOME, FISH, BIRD, INSECT, FAUNA, COOK, SPROUT, CALENDAR, SETTINGS }

@Composable
fun NotebookTitle(title: String, mark: NotebookMark, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        SectionMark(mark, Modifier.size(36.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Composable
fun SectionMark(mark: NotebookMark, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val ink = Ink
        val w = size.minDimension
        val stroke = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
        when (mark) {
            NotebookMark.HOME -> {
                drawCircle(Ochre.copy(alpha = 0.35f), w * 0.28f, Offset(w * 0.55f, w * 0.42f))
                drawLine(ink, Offset(w * 0.2f, w * 0.72f), Offset(w * 0.8f, w * 0.72f), stroke.width, StrokeCap.Round)
            }
            NotebookMark.FISH -> {
                drawOval(Green, Offset(w * 0.12f, w * 0.32f), androidx.compose.ui.geometry.Size(w * 0.55f, w * 0.32f))
                val tail = Path().apply {
                    moveTo(w * 0.62f, w * 0.48f)
                    lineTo(w * 0.86f, w * 0.28f)
                    lineTo(w * 0.86f, w * 0.68f)
                    close()
                }
                drawPath(tail, Green)
            }
            NotebookMark.BIRD -> {
                drawLine(ink, Offset(w * 0.2f, w * 0.62f), Offset(w * 0.7f, w * 0.3f), stroke.width, StrokeCap.Round)
                drawLine(ink, Offset(w * 0.45f, w * 0.42f), Offset(w * 0.2f, w * 0.28f), stroke.width, StrokeCap.Round)
            }
            NotebookMark.INSECT -> {
                drawOval(Moss, Offset(w * 0.28f, w * 0.22f), androidx.compose.ui.geometry.Size(w * 0.22f, w * 0.5f))
                drawOval(Moss, Offset(w * 0.5f, w * 0.22f), androidx.compose.ui.geometry.Size(w * 0.22f, w * 0.5f))
            }
            NotebookMark.FAUNA -> {
                drawCircle(Ochre, w * 0.1f, Offset(w * 0.3f, w * 0.35f))
                drawCircle(Ochre, w * 0.1f, Offset(w * 0.55f, w * 0.62f))
                drawCircle(Ochre, w * 0.08f, Offset(w * 0.72f, w * 0.4f))
            }
            NotebookMark.COOK -> {
                drawArc(ink, 200f, 140f, false, Offset(w * 0.18f, w * 0.28f), androidx.compose.ui.geometry.Size(w * 0.64f, w * 0.4f), style = stroke)
                drawLine(ink, Offset(w * 0.22f, w * 0.55f), Offset(w * 0.78f, w * 0.55f), stroke.width, StrokeCap.Round)
            }
            NotebookMark.SPROUT -> {
                drawLine(Moss, Offset(w * 0.5f, w * 0.8f), Offset(w * 0.5f, w * 0.35f), stroke.width, StrokeCap.Round)
                drawOval(Moss, Offset(w * 0.48f, w * 0.22f), androidx.compose.ui.geometry.Size(w * 0.28f, w * 0.16f))
            }
            NotebookMark.CALENDAR -> {
                drawRect(ink, Offset(w * 0.18f, w * 0.22f), androidx.compose.ui.geometry.Size(w * 0.64f, w * 0.56f), style = stroke)
                drawLine(Ochre, Offset(w * 0.18f, w * 0.4f), Offset(w * 0.82f, w * 0.4f), stroke.width)
            }
            NotebookMark.SETTINGS -> {
                drawCircle(ink, w * 0.16f, style = stroke)
                drawCircle(Ochre, w * 0.05f)
            }
        }
    }
}
