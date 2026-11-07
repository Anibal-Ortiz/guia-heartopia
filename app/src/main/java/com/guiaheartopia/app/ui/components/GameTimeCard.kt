package com.guiaheartopia.app.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Muted
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun GameTimeCard(zoneId: String) {
    val zone = DexSchedule.zoneOf(zoneId)
    val instant = Instant.now()
    val now = instant.atZone(zone)
    val current = DexSchedule.currentWindow(zone, instant)
    val windows = DexSchedule.windowsAround(zone, instant)
    val clock = DateTimeFormatter.ofPattern("HH:mm")
    PixelCard {
        SlotLines(
            titleColor = Green,
            bodyColor = Color.Unspecified,
            muted = Muted,
            headline = "${DexSchedule.titled(current.slot)} · ${DexSchedule.untilLabel(current, now)}",
            clockLine = "Hora local ${now.format(clock)} · ${Countries.zoneLabel(zoneId)}",
            lines = windows.map { window ->
                val mark = if (window.slot.key == current.slot.key) " · ahora" else ""
                "${DexSchedule.titled(window.slot)}  ${DexSchedule.hoursLabel(window)}$mark"
            },
        )
    }
}

@Composable
private fun ColumnScope.SlotLines(
    titleColor: Color,
    bodyColor: Color,
    muted: Color,
    headline: String,
    clockLine: String,
    lines: List<String>,
) {
    Text("Franja del juego", style = MaterialTheme.typography.titleSmall, color = titleColor)
    Text(headline, style = MaterialTheme.typography.titleMedium, color = titleColor)
    Text(clockLine, style = MaterialTheme.typography.bodySmall, color = muted)
    lines.forEach { line ->
        Text(line, style = MaterialTheme.typography.bodyMedium, color = bodyColor)
    }
    Text(
        "Horas del país elegido, con horario de verano. El servidor va en UTC fijo.",
        style = MaterialTheme.typography.bodySmall,
        color = muted,
    )
}
