package com.guiaheartopia.app.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.PlaySession
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    initial: PlayerProfile,
    onContinue: (PlayerProfile) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var place by rememberSaveable { mutableStateOf(initial.placeName) }
    var countryId by rememberSaveable { mutableStateOf(initial.countryId) }
    var zoneId by rememberSaveable { mutableStateOf(initial.zoneId) }
    var session by rememberSaveable { mutableStateOf(initial.session) }
    val country = Countries.byId(countryId)
    if (country.zones.none { it.zoneId == zoneId }) zoneId = country.zones.first().zoneId

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Guía Heartopia", style = MaterialTheme.typography.headlineMedium)
        VerifyBadge("Guía independiente")
        Text(
            "Una libreta para tu partida: rutina, fichas, coleccionables, clima, hobbies y una guía para pintar a mano. No está afiliada a los creadores de Heartopia.",
            style = MaterialTheme.typography.bodyLarge,
        )
        PixelCard {
            Text("Cómo te apuntamos", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tu nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("Cómo llamas a tu lugar (opcional)") },
                singleLine = true,
            )
            Text("Sueles abrir el juego…", style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaySession.entries.forEach { option ->
                    FilterChip(
                        selected = session == option,
                        onClick = { session = option },
                        label = { Text(option.label) },
                    )
                }
            }
        }
        PixelCard {
            Text("País del calendario", style = MaterialTheme.typography.titleMedium)
            Text(
                "Si el idioma o la zona del teléfono coinciden con la lista, ya vienen elegidos. No usamos tu ubicación.",
                style = MaterialTheme.typography.bodySmall,
            )
            PickerField(
                label = "País",
                value = country.name,
                options = Countries.all.map { it.id to it.name },
                onPick = { countryId = it },
            )
            PickerField(
                label = "Zona horaria",
                value = country.zones.first { it.zoneId == zoneId }.label + " · " + zoneId,
                options = country.zones.map { it.zoneId to "${it.label} · ${it.zoneId}" },
                onPick = { zoneId = it },
            )
        }
        TextButton(onClick = onPrivacy) { Text("Política de privacidad") }
        TextButton(onClick = onTerms) { Text("Términos") }
        PixelButton(
            text = "Entrar a la guía",
            onClick = {
                if (name.isBlank()) return@PixelButton
                onContinue(
                    initial.copy(
                        name = name.trim(),
                        placeName = place.trim(),
                        session = session,
                        countryId = countryId,
                        zoneId = zoneId,
                        onboarded = true,
                    ),
                )
            },
        )
        if (name.isBlank()) {
            Text("Escribe un nombre para continuar.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
