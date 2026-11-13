package com.guiaheartopia.app.ui.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.PlaySession
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.StepBadge
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Muted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    initial: PlayerProfile,
    onContinue: (PlayerProfile) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var countryId by rememberSaveable { mutableStateOf(initial.countryId) }
    var zoneId by rememberSaveable { mutableStateOf(initial.zoneId) }
    var regionKey by rememberSaveable { mutableStateOf(initial.regionKey) }
    var session by rememberSaveable { mutableStateOf(initial.session.name) }
    val country = Countries.byId(countryId)
    if (country.zones.none { it.key == regionKey || it.zoneId == zoneId }) {
        zoneId = country.zones.first().zoneId
        regionKey = country.zones.first().key
    }
    val total = 4

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StepBadge()
            Column(Modifier.weight(1f)) {
                Text("WikiFan para Heartopia", style = MaterialTheme.typography.titleSmall)
                Text("Paso ${step + 1} de $total", style = MaterialTheme.typography.bodySmall)
            }
        }
        when (step) {
            0 -> StepName(name, { name = it }, onPrivacy, onTerms)
            1 -> StepCountry(countryId) {
                countryId = it
                val first = Countries.byId(it).zones.first()
                zoneId = first.zoneId
                regionKey = first.key
            }
            2 -> StepZone(country, regionKey) { key ->
                val zone = country.zones.first { it.key == key }
                regionKey = zone.key
                zoneId = zone.zoneId
            }
            else -> {
                StepPlay(session) { session = it }
                StepSummary(name, country.name, Countries.label(country.id, zoneId, regionKey), session)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) {
                TextButton(onClick = { step -= 1 }) { Text("← Anterior", color = Green) }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            val ready = step != 0 || name.isNotBlank()
            PixelButton(
                text = if (step == total - 1) "Entrar →" else "Continuar →",
                onClick = {
                    if (!ready) return@PixelButton
                    if (step < total - 1) step += 1
                    else {
                        onContinue(
                            initial.copy(
                                name = name.trim(),
                                session = PlaySession.valueOf(session),
                                countryId = countryId,
                                zoneId = zoneId,
                                regionKey = regionKey,
                                onboarded = true,
                            ),
                        )
                    }
                },
            )
        }
        if (step == 0 && name.isBlank()) {
            Text("Escribe un nombre para continuar.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StepName(
    name: String,
    onName: (String) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    Text("¿Cómo te llamamos?", style = MaterialTheme.typography.headlineSmall)
    Text(
        "Lo que anotes se queda en el teléfono.",
        style = MaterialTheme.typography.bodyLarge,
    )
    OutlinedTextField(name, onName, label = { Text("Tu nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    TextButton(onClick = onPrivacy) { Text("Política de privacidad") }
    TextButton(onClick = onTerms) { Text("Términos") }
}

@Composable
private fun StepCountry(selected: String, onPick: (String) -> Unit) {
    Text("Tu país", style = MaterialTheme.typography.headlineSmall)
    Text("Sirve para el calendario. No usamos la ubicación del teléfono: solo el país que elijas.", style = MaterialTheme.typography.bodyMedium)
    Countries.all.forEach { country ->
        OptionCard(country.name, selected = country.id == selected, onClick = { onPick(country.id) })
    }
}

@Composable
private fun StepZone(country: com.guiaheartopia.app.domain.Country, regionKey: String, onPick: (String) -> Unit) {
    Text(
        if (country.zones.size > 1) "Estado o región" else "Zona horaria",
        style = MaterialTheme.typography.headlineSmall,
    )
    PixelCard(container = Cream) {
        Text(
            "Las horas del juego se muestran en la zona que elijas.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    country.zones.forEach { zone ->
        OptionCard(zone.label, selected = zone.key == regionKey, onClick = { onPick(zone.key) })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepPlay(session: String, onPick: (String) -> Unit) {
    Text("¿Cuándo sueles jugar?", style = MaterialTheme.typography.headlineSmall)
    Text("Solo cambia el saludo. Los recordatorios se activan después, en Ajustes.", style = MaterialTheme.typography.bodyMedium)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaySession.entries.forEach { option ->
            FilterChip(selected = session == option.name, onClick = { onPick(option.name) }, label = { Text(option.label) })
        }
    }
}

@Composable
private fun StepSummary(name: String, country: String, zone: String, session: String) {
    Text("Listo, $name", style = MaterialTheme.typography.headlineSmall)
    PixelCard {
        Text(country, style = MaterialTheme.typography.titleMedium)
        Text(zone, style = MaterialTheme.typography.bodyMedium)
        Text(PlaySession.valueOf(session).label, style = MaterialTheme.typography.bodySmall)
    }
    Text("Puedes cambiarlo cuando quieras en Ajustes.", style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun OptionCard(label: String, selected: Boolean, onClick: () -> Unit) {
    PixelCard(
        modifier = Modifier.clip(RoundedCornerShape(22.dp)).padding(0.dp),
        container = if (selected) com.guiaheartopia.app.ui.theme.Mint else com.guiaheartopia.app.ui.theme.Paper,
    ) {
        Row(Modifier.fillMaxWidth().padding(0.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f).padding(0.dp), style = MaterialTheme.typography.bodyLarge)
            if (selected) Text("✓", color = com.guiaheartopia.app.ui.theme.Green, style = MaterialTheme.typography.titleMedium)
        }
        androidx.compose.foundation.layout.Spacer(
            Modifier
                .fillMaxWidth()
                .padding(0.dp),
        )
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (selected) "Elegido" else "Elegir", color = com.guiaheartopia.app.ui.theme.Green)
        }
    }
}
