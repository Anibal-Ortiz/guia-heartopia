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
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Yellow

@Composable
fun HomeScreen(ui: GuideUi, onOpen: (String) -> Unit) {
    val country = Countries.byId(ui.profile.countryId)
    val done = RoutineLogic.completedCount(ui.routine)
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
        PixelCard(container = Dark) {
            Text("Tu colección", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                "${(ui.collectionFraction * 100).toInt()} % registrado",
                color = Yellow,
                style = MaterialTheme.typography.bodyMedium,
            )
            PixelProgress(ui.collectionFraction, color = Yellow, track = androidx.compose.ui.graphics.Color(0xFF3A322C))
            PixelButton("Abrir la guía →", onClick = { onOpen("guide") }, container = Yellow, contentColor = Dark)
        }
        PixelCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
            "weather" to "Calendario",
            "map" to "Mapa ilustrado",
            "pixel" to "Pixel art",
            "hobbies" to "Hobbies",
        ).forEach { (route, label) ->
            PixelCard(Modifier.clickable { onOpen(route) }) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
