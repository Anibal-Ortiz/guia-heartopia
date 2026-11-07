package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.CreatureMark
import com.guiaheartopia.app.domain.Habitat
import com.guiaheartopia.app.ui.theme.Beige
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper

@Composable
fun CreatureGlyph(
    category: String,
    locations: List<String>,
    level: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val habitat = CreatureMark.habitat(locations, level)
    val tint = habitatColor(habitat, level)
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.16f)),
    ) {
        Canvas(Modifier.size(size)) {
            val ink = tint
            val w = this.size.minDimension
            when (category) {
                "Peces" -> fish(ink, w)
                "Pájaros" -> bird(ink, w)
                "Insectos" -> insect(ink, w)
                "Fauna" -> fauna(ink, w)
                else -> {
                    drawCircle(ink, w * 0.22f, center, style = Stroke(w * 0.06f))
                }
            }
        }
    }
}

private fun habitatColor(habitat: Habitat, level: Int?): Color = when (habitat) {
    Habitat.MAR -> Color(0xFF2C6E8A)
    Habitat.AGUA -> Color(0xFF1C7A68)
    Habitat.BOSQUE -> Green
    Habitat.RUINAS -> Color(0xFF6E5A8A)
    Habitat.PUEBLO -> Orange
    Habitat.NIVEL -> when {
        (level ?: 1) >= 8 -> Color(0xFF8A5A12)
        (level ?: 1) >= 4 -> Orange
        else -> Green
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.fish(ink: Color, w: Float) {
    drawOval(ink, Offset(w * 0.22f, w * 0.34f), Size(w * 0.48f, w * 0.30f))
    val tail = Path().apply {
        moveTo(w * 0.68f, w * 0.49f)
        lineTo(w * 0.84f, w * 0.32f)
        lineTo(w * 0.84f, w * 0.66f)
        close()
    }
    drawPath(tail, ink)
    drawCircle(Paper, w * 0.035f, Offset(w * 0.34f, w * 0.45f))
    drawCircle(Ink, w * 0.018f, Offset(w * 0.34f, w * 0.45f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.bird(ink: Color, w: Float) {
    drawCircle(ink, w * 0.16f, Offset(w * 0.42f, w * 0.48f))
    drawCircle(ink, w * 0.11f, Offset(w * 0.60f, w * 0.36f))
    val beak = Path().apply {
        moveTo(w * 0.70f, w * 0.34f)
        lineTo(w * 0.84f, w * 0.38f)
        lineTo(w * 0.70f, w * 0.42f)
        close()
    }
    drawPath(beak, Orange)
    drawCircle(Paper, w * 0.02f, Offset(w * 0.63f, w * 0.34f))
    drawArc(ink, 200f, 140f, false, Offset(w * 0.28f, w * 0.42f), Size(w * 0.28f, w * 0.22f), style = Stroke(w * 0.045f, cap = StrokeCap.Round))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.insect(ink: Color, w: Float) {
    drawOval(ink.copy(alpha = 0.85f), Offset(w * 0.18f, w * 0.28f), Size(w * 0.28f, w * 0.18f))
    drawOval(ink.copy(alpha = 0.85f), Offset(w * 0.52f, w * 0.28f), Size(w * 0.28f, w * 0.18f))
    drawOval(ink, Offset(w * 0.40f, w * 0.40f), Size(w * 0.20f, w * 0.28f))
    drawLine(ink, Offset(w * 0.44f, w * 0.40f), Offset(w * 0.34f, w * 0.24f), w * 0.035f, StrokeCap.Round)
    drawLine(ink, Offset(w * 0.56f, w * 0.40f), Offset(w * 0.66f, w * 0.24f), w * 0.035f, StrokeCap.Round)
    drawCircle(Paper, w * 0.025f, Offset(w * 0.46f, w * 0.28f))
    drawCircle(Paper, w * 0.025f, Offset(w * 0.54f, w * 0.28f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.fauna(ink: Color, w: Float) {
    drawCircle(ink, w * 0.12f, Offset(w * 0.36f, w * 0.32f))
    drawCircle(ink, w * 0.12f, Offset(w * 0.64f, w * 0.32f))
    drawCircle(ink, w * 0.20f, Offset(w * 0.50f, w * 0.52f))
    drawCircle(Paper, w * 0.035f, Offset(w * 0.44f, w * 0.50f))
    drawCircle(Paper, w * 0.035f, Offset(w * 0.56f, w * 0.50f))
    drawCircle(Ink, w * 0.016f, Offset(w * 0.44f, w * 0.50f))
    drawCircle(Ink, w * 0.016f, Offset(w * 0.56f, w * 0.50f))
}

@Composable
fun UnknownGlyph(modifier: Modifier = Modifier, size: Dp = 52.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Beige),
    )
}
