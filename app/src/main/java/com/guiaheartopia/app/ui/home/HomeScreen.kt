package com.guiaheartopia.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.RoutineLogic
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.components.LineNavIcon
import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import java.time.Instant
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Yellow

@Composable
fun HomeScreen(ui: GuideUi, onOpen: (String) -> Unit) {
    val country = Countries.byId(ui.profile.countryId)
    val done = RoutineLogic.completedCount(ui.routine)
    val context = androidx.compose.ui.platform.LocalContext.current
    val catalog = androidx.compose.runtime.remember { com.guiaheartopia.app.data.HeartodexCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val nowInstant = Instant.now()
    val available = catalog.count { DexSchedule.availableNow(it.schedule, nowInstant) }
    val byCategory = catalog.groupBy { it.category }
    val today = java.time.LocalDate.now(zone)
    val spot = com.guiaheartopia.app.data.VerifiedCatalog.novemberSpot(today)
    val event = com.guiaheartopia.app.data.VerifiedCatalog.events.firstOrNull()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(ui.greeting, style = MaterialTheme.typography.headlineSmall)
        Text(ui.todayLabel.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodyMedium)
        Text("${country.name} · ${ui.profile.zoneId}", style = MaterialTheme.typography.bodySmall)
        Text("Guía independiente. No está afiliada a los creadores de Heartopia.", style = MaterialTheme.typography.bodySmall)
        GameTimeCard(zone.id)
        PixelCard(container = Dark) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineNavIcon(NavIcon.GUIDE, Yellow)
                Text("Tu colección", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Text("$available de ${catalog.size} disponibles ahora", color = Yellow, style = MaterialTheme.typography.bodyMedium)
            PixelProgress(
                if (catalog.isEmpty()) 0f else available.toFloat() / catalog.size,
                color = Yellow,
                track = androidx.compose.ui.graphics.Color(0xFF3A322C),
            )
            byCategory.forEach { (category, items) ->
                val found = items.count { it.id in ui.found }
                Text("$category  $found/${items.size}", color = androidx.compose.ui.graphics.Color(0xFFE7E0D6), style = MaterialTheme.typography.bodySmall)
            }
            PixelButton("Abrir la guía →", onClick = { onOpen("guide") }, container = Yellow, contentColor = Dark)
        }
        PixelCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineNavIcon(NavIcon.HOME, Green)
                Text("Hoy", style = MaterialTheme.typography.titleMedium)
            }
            if (spot == null) {
                Text("Sin parcela contrastada de flurita o Liuliu para esta fecha.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Liuliu: ${com.guiaheartopia.app.data.VerifiedCatalog.spotLabel(spot.first)}")
                Text("flurita: ${com.guiaheartopia.app.data.VerifiedCatalog.spotLabel(spot.second)}", color = Green)
            }
            if (event != null) {
                Text("${event.title}: ${event.start} → ${event.end}", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Datos de heartodex y, en noviembre, también Tidebell.", style = MaterialTheme.typography.bodySmall)
        }
        PixelCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LineNavIcon(NavIcon.ROUTINE, Green)
                Column(Modifier.weight(1f)) {
                    Text("Rutina de hoy", style = MaterialTheme.typography.titleMedium)
                    Text("Se reinicia cuando cambia la fecha local.", style = MaterialTheme.typography.bodySmall)
                }
                Text("$done de ${ui.routine.tasks.size}\ncompletadas", color = Green, style = MaterialTheme.typography.titleSmall)
            }
            ui.routine.tasks.take(3).forEach { task ->
                Text(
                    (if (task.id in ui.routine.completedIds) "✓  " else "○  ") + task.title,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            PixelButton("Ver rutina", onClick = { onOpen("routine") }, outlined = true)
        }
        listOf(
            Triple("weather", "Calendario", NavIcon.GUIDE),
            Triple("map", "Mapa ilustrado", NavIcon.HOME),
            Triple("pixel", "Pixel art", NavIcon.PIXEL),
            Triple("hobbies", "Hobbies", NavIcon.HOBBIES),
        ).forEach { (route, label, icon) ->
            PixelCard(Modifier.clickable { onOpen(route) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LineNavIcon(icon, Green)
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
