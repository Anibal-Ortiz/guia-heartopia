package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.DarkCard
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.HoneyTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Lilac
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.theme.Peach
import com.guiaheartopia.app.ui.theme.Purple
import com.guiaheartopia.app.ui.theme.Sky
import com.guiaheartopia.app.ui.theme.SkyBlue
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun GameTimeCard(zoneId: String, modifier: Modifier = Modifier) {
    val zone = DexSchedule.zoneOf(zoneId)
    val instant = Instant.now()
    val now = instant.atZone(zone)
    val current = DexSchedule.currentWindow(zone, instant)
    val windows = DexSchedule.windowsAround(zone, instant)
    val clock = DateTimeFormatter.ofPattern("HH:mm")

    val (slotEmoji, slotColor, slotBg) = when (current.slot.key.lowercase()) {
        "dawn" -> Triple("🌅", Orange, Peach)
        "day" -> Triple("☀️", Honey, HoneySoft)
        "dusk" -> Triple("🌤️", Orange, Peach)
        "night" -> Triple("🌆", Purple, Lilac)
        else -> Triple("⏰", Green, GreenSoft)
    }

    PixelCard(modifier = modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(slotBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(slotEmoji, fontSize = 18.sp)
                }
                Column {
                    Text(
                        "Franja del Juego",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    Text(
                        DexSchedule.titled(current.slot),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Ink,
                    )
                }
            }
            NowBadge(DexSchedule.untilLabel(current, now))
        }

        Text(
            "Hora local: ${now.format(clock)} · ${Countries.zoneLabel(zoneId)}",
            style = MaterialTheme.typography.bodySmall,
            color = Muted,
        )

        Spacer(Modifier.height(2.dp))

        // Grid of 4 time periods
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            windows.forEach { window ->
                val isCurrent = window.slot.key == current.slot.key
                val itemBg = if (isCurrent) GreenSoft else PaperSubtle
                val itemBorder = if (isCurrent) GreenTint else Line
                val icon = when (window.slot.key.lowercase()) {
                    "dawn" -> "🌅"
                    "day" -> "☀️"
                    "dusk" -> "🌤️"
                    "night" -> "🌆"
                    else -> "⏰"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBg)
                        .border(1.dp, itemBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(icon, fontSize = 14.sp)
                        Text(
                            DexSchedule.titled(window.slot),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isCurrent) GreenDark else Ink,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            DexSchedule.hoursLabel(window),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (isCurrent) GreenDark else Muted,
                        )
                        if (isCurrent) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Green),
                            )
                        }
                    }
                }
            }
        }

        Text(
            "Convertido a tu país con horario de verano (servidor en UTC fijo).",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Muted,
        )
    }
}
