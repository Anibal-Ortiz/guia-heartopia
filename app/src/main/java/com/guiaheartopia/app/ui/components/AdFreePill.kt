package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.AdRewardStore
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.PaperSubtle
import kotlinx.coroutines.delay

/**
 * Contador local del beneficio «sin anuncios». El estado vive en el propio
 * composable: cada 30 s solo se recompone el chip o la tarjeta que lo usa,
 * nunca la pantalla completa.
 */
@Composable
fun rememberAdFreeRemainingMillis(untilMillis: Long): Long {
    var nowMillis by remember(untilMillis) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(untilMillis) {
        nowMillis = System.currentTimeMillis()
        while (untilMillis > nowMillis) {
            delay(30_000L)
            nowMillis = System.currentTimeMillis()
        }
    }
    return (untilMillis - nowMillis).coerceAtLeast(0L)
}

/**
 * Píldora compacta de la barra superior.
 * - Inactiva: «Sin ads · 3h», pulsable para ver un video.
 * - Activa: contador «Sin ads · 2h 45m» y acción «+3h» si quedan 9 h o menos.
 * - Con más de 9 h restantes: «Máx. 12h» y sin acción hasta bajar del umbral.
 */
@Composable
fun AdFreeStatusChip(
    untilMillis: Long,
    onWatchAdFree: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = rememberAdFreeRemainingMillis(untilMillis)
    val active = remaining > 0L
    val canAddMore = AdRewardStore.canAddMore(remaining)
    val shape = RoundedCornerShape(999.dp)
    val background = if (active) GreenSoft else PaperSubtle
    val border = if (active) GreenTint else Line
    val textColor = if (active) GreenDark else Ink

    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, border, shape)
            .then(
                if (active) {
                    Modifier
                } else {
                    Modifier
                        .clickable(onClick = onWatchAdFree)
                        .semantics { contentDescription = "Ver un video y quitar anuncios por 3 horas" }
                },
            )
            .padding(
                start = 9.dp,
                end = if (active && canAddMore) 4.dp else 9.dp,
                top = 5.dp,
                bottom = 5.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AdFreePlayIcon(tint = textColor, iconSize = 15.dp)
        Text(
            text = if (active) {
                "Sin ads · ${AdRewardStore.formatRemaining(remaining)}"
            } else {
                "Sin ads · 3h"
            },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (active) {
            if (canAddMore) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Green)
                        .clickable(onClick = onWatchAdFree)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .semantics { contentDescription = "Ver otro video y sumar 3 horas sin anuncios" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "+3h",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                    )
                }
            } else {
                Text(
                    "Máx. 12h",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Muted,
                    maxLines = 1,
                    modifier = Modifier.semantics { contentDescription = "Máximo 12h alcanzado" },
                )
            }
        }
    }
}

/**
 * Tarjeta de Ajustes. Comparte el mismo contador y las mismas reglas que el
 * chip de la barra superior.
 */
@Composable
fun AdFreeRewardCard(
    untilMillis: Long,
    onWatchAdFree: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = rememberAdFreeRemainingMillis(untilMillis)
    val active = remaining > 0L
    val canAddMore = AdRewardStore.canAddMore(remaining)

    NotebookCard(modifier = modifier) {
        Text("Sin anuncios con videos", style = MaterialTheme.typography.titleMedium)
        Text(
            "Cada video elimina los anuncios por 3 horas (acumulable hasta 12 horas).",
            style = MaterialTheme.typography.bodySmall,
        )
        if (active) {
            Text(
                "Sin banners ni intersticiales. Quedan ${AdRewardStore.formatRemaining(remaining)}.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        when {
            !active -> PixelButton(
                "Ver video · +3h",
                onClick = onWatchAdFree,
                outlined = true,
                modifier = Modifier.fillMaxWidth(),
            )

            canAddMore -> PixelButton(
                "Ver otro video · +3h",
                onClick = onWatchAdFree,
                outlined = true,
                modifier = Modifier.fillMaxWidth(),
            )

            else -> {
                Text(
                    "Máximo 12h alcanzado",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenDark,
                )
                Text(
                    "Podrás sumar otro video cuando queden 9 h o menos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                PixelButton(
                    "Ver otro video · +3h",
                    onClick = {},
                    outlined = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AdFreePlayIcon(tint: Color, iconSize: Dp) {
    Canvas(Modifier.size(iconSize)) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.06f, h * 0.12f),
            size = Size(w * 0.88f, h * 0.76f),
            cornerRadius = CornerRadius(w * 0.22f),
            style = Stroke(1.3.dp.toPx()),
        )
        val play = Path().apply {
            moveTo(w * 0.42f, h * 0.30f)
            lineTo(w * 0.72f, h * 0.50f)
            lineTo(w * 0.42f, h * 0.70f)
            close()
        }
        drawPath(play, tint)
    }
}
