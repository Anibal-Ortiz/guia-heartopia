package com.guiaheartopia.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.VerifyBadge

@Composable
fun HomeScreen(ui: GuideUi, onOpen: (String) -> Unit) {
    val country = Countries.byId(ui.profile.countryId)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(ui.greeting, style = MaterialTheme.typography.headlineSmall)
        Text(ui.todayLabel.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodyLarge)
        if (ui.profile.placeName.isNotBlank()) {
            Text("Tu lugar: ${ui.profile.placeName}", style = MaterialTheme.typography.bodyMedium)
        }
        Text("${country.name} · ${ui.profile.zoneId}", style = MaterialTheme.typography.bodySmall)
        VerifyBadge("Guía independiente, no afiliada")
        PixelCard {
            Text("Rutina de hoy", style = MaterialTheme.typography.titleMedium)
            Text(
                "${(ui.routineFraction * 100).toInt()} % · ${ui.routine.completedIds.size} de ${ui.routine.tasks.size}",
                style = MaterialTheme.typography.bodyMedium,
            )
            PixelProgress(ui.routineFraction)
            Text("Es una lista tuya. No es la rutina oficial del juego.", style = MaterialTheme.typography.bodySmall)
        }
        PixelCard {
            Text("Coleccionables", style = MaterialTheme.typography.titleMedium)
            PixelProgress(ui.collectionFraction)
            Text("Los ítems de ejemplo siguen pendientes de verificar.", style = MaterialTheme.typography.bodySmall)
        }
        listOf(
            "routine" to "Rutina",
            "guide" to "Guía de animales y coleccionables",
            "weather" to "Calendario de climas",
            "pixel" to "Pixel art, guía manual",
            "hobbies" to "Hobbies",
            "settings" to "Ajustes",
        ).forEach { (route, label) ->
            PixelCard(Modifier.clickable { onOpen(route) }) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
