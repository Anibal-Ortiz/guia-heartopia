package com.guiaheartopia.app.ui.guide

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.TextMatch
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.NowBadge
import java.time.Instant
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Muted

private val Tabs = listOf("Animales", "Peces", "Insectos", "Pájaros", "Fauna")

@Composable
fun GuideScreen(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onOpenWeather: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenDex: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
    initialTab: Int = 0,
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Guía", style = MaterialTheme.typography.headlineSmall)
        PillTabs(Tabs, tab) { tab = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PixelButton("Clima", onClick = onOpenWeather, outlined = true)
            PixelButton("Mapa", onClick = onOpenMap, outlined = true)
        }
        when (tab) {
            0 -> AnimalsTab(ui, onOpenAnimal, onAddAnimal)
            else -> DexTab(Tabs[tab], ui, onOpenDex, onToggleFound)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimalsTab(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var onlySighted by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }
    val shown = ui.animals.filter { animal ->
        (!onlySighted || animal.id in ui.sighted) &&
            TextMatch.contains(animal.name, query)
    }
    OutlinedTextField(query, { query = it }, label = { Text("Buscar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    FlowRow {
        FilterChip(selected = onlySighted, onClick = { onlySighted = !onlySighted }, label = { Text("Registrados") })
    }
    if (shown.isEmpty()) Text("Ninguna ficha coincide.", style = MaterialTheme.typography.bodyMedium)
    shown.forEach { animal ->
        PixelCard(Modifier.clickable { onOpenAnimal(animal.id) }) {
            Text(animal.name, style = MaterialTheme.typography.titleMedium)
            Text(animal.group, color = Muted, style = MaterialTheme.typography.bodySmall)
            LabelValue("Comidas", animal.favoriteFoods.joinToString(", ").ifBlank { "Pendiente" })
            LabelValue("Comedero", if (animal.locationVerified) animal.feederLocations.joinToString() else "Pendiente", emphasize = false)
            PixelButton(if (animal.id in ui.sighted) "✓ Registrado" else "Registrar", onClick = { onOpenAnimal(animal.id) }, outlined = animal.id !in ui.sighted)
        }
    }
    PixelCard {
        Text("Tu ficha", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(newName, { newName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        PixelButton("Guardar ficha", onClick = {
            onAddAnimal(newName, "Mis fichas", "", "")
            newName = ""
        }, outlined = true)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DexTab(
    category: String,
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val all = remember { com.guiaheartopia.app.data.HeartodexCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val instant = Instant.now()
    val current = DexSchedule.currentWindow(zone, instant)
    var query by rememberSaveable(category) { mutableStateOf("") }
    var place by rememberSaveable(category) { mutableStateOf("") }
    var onlyNow by rememberSaveable(category) { mutableStateOf(false) }
    var registered by rememberSaveable(category) { mutableStateOf("todos") }
    var weather by rememberSaveable(category) { mutableStateOf("Todos") }
    val items = all.filter { entry ->
        entry.category == category &&
            (query.isBlank() || TextMatch.contains(entry.name, query) || TextMatch.contains(entry.nameEn, query)) &&
            (place.isBlank() || entry.location.any { TextMatch.contains(it, place) }) &&
            (weather == "Todos" || weather in entry.weather) &&
            (!onlyNow || DexSchedule.availableNow(entry.schedule, instant)) &&
            when (registered) {
                "si" -> entry.id in ui.found
                "no" -> entry.id !in ui.found
                else -> true
            }
    }
    OutlinedTextField(query, { query = it }, label = { Text("Buscar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    OutlinedTextField(place, { place = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Todos", "Sunny", "Rainy", "Rainbow").forEach { item ->
            FilterChip(selected = weather == item, onClick = { weather = item }, label = { Text(if (item == "Todos") item else DexSchedule.weatherEs(item)) })
        }
        FilterChip(selected = onlyNow, onClick = { onlyNow = !onlyNow }, label = { Text("Disponible ahora") })
        FilterChip(selected = registered == "si", onClick = { registered = if (registered == "si") "todos" else "si" }, label = { Text("Registrados") })
        FilterChip(selected = registered == "no", onClick = { registered = if (registered == "no") "todos" else "no" }, label = { Text("Sin registrar") })
    }
    Text(
        "${DexSchedule.titled(current.slot)} · ${DexSchedule.untilLabel(current, instant.atZone(zone))}. El clima se muestra y no cuenta para «Ahora».",
        style = MaterialTheme.typography.bodySmall,
    )
    Text("${items.size} fichas", style = MaterialTheme.typography.bodySmall)
    if (items.isEmpty()) Text("Nada coincide con el filtro.", style = MaterialTheme.typography.bodyMedium)
    items.forEach { entry ->
        val now = DexSchedule.availableNow(entry.schedule, instant)
        PixelCard(Modifier.clickable { onOpen(entry.id) }) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(entry.name, style = MaterialTheme.typography.titleMedium)
                    Text(entry.event ?: entry.category, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                if (now) NowBadge()
            }
            LabelValue("Ubicación", entry.location.joinToString().ifBlank { "—" })
            LabelValue(
                "Horario",
                entry.schedule.joinToString { DexSchedule.scheduleLabel(it) }.ifBlank { "—" },
                emphasize = false,
            )
            LabelValue(
                "Clima",
                entry.weather.joinToString { DexSchedule.weatherEs(it) }.ifBlank { "—" },
                emphasize = false,
            )
            PixelButton(
                if (entry.id in ui.found) "✓ Registrado" else "Registrar",
                onClick = { onToggleFound(entry.id) },
                outlined = entry.id !in ui.found,
            )
        }
    }
}

@Composable
fun DexDetailScreen(entry: DexEntry, zoneId: String, found: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
    val zone = DexSchedule.zoneOf(zoneId)
    val instant = Instant.now()
    val now = DexSchedule.availableNow(entry.schedule, instant)
    val localHours = DexSchedule.windowsAround(zone, instant)
        .filter { it.slot.key in entry.schedule }
        .joinToString(" · ") { "${DexSchedule.titled(it.slot)} ${DexSchedule.hoursLabel(it)}" }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.name, style = MaterialTheme.typography.headlineSmall)
            }
            if (now) NowBadge()
        }
        PixelCard {
            LabelValue("Ubicación", entry.location.joinToString().ifBlank { "—" })
            LabelValue("Horario", entry.schedule.joinToString { DexSchedule.scheduleLabel(it) }.ifBlank { "—" }, false)
            LabelValue("En tu zona", localHours.ifBlank { "—" }, false)
            LabelValue("Clima", entry.weather.joinToString { DexSchedule.weatherEs(it) }.ifBlank { "—" }, false)
            if (entry.level != null) LabelValue("Nivel", entry.level.toString(), false)
            if (!entry.shadow.isNullOrBlank()) LabelValue("Sombra", entry.shadow, false)
            if (!entry.event.isNullOrBlank()) LabelValue("Evento", entry.event, false)
        }
        Text(
            "«Ahora» sigue el tramo del juego en la zona de tu país. El clima que pide la ficha se muestra, pero todavía no cuenta.",
            style = MaterialTheme.typography.bodySmall,
        )
        PixelButton(if (found) "✓ Registrado" else "Registrar", onClick = onToggle, outlined = !found)
        PixelButton("Volver", onClick = onBack, outlined = true)
    }
}

@Composable
fun AnimalDetailScreen(
    animal: AnimalRecord,
    annotation: AnimalAnnotation,
    sighted: Boolean,
    onToggleSighted: () -> Unit,
    onSave: (AnimalAnnotation) -> Unit,
    onBack: () -> Unit,
) {
    var foods by remember(animal.id) { mutableStateOf(annotation.foods.joinToString("\n")) }
    var feeders by remember(animal.id) { mutableStateOf(annotation.feeders.joinToString("\n")) }
    var note by remember(animal.id) { mutableStateOf(annotation.note) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(animal.name, style = MaterialTheme.typography.headlineSmall)
        Text(animal.group, color = Muted)
        if (animal.summary.isNotBlank()) Text(animal.summary, style = MaterialTheme.typography.bodyLarge)
        PixelCard {
            LabelValue("Comidas", animal.favoriteFoods.joinToString("\n").ifBlank { "—" })
            LabelValue("Comedero", animal.feederLocations.joinToString().ifBlank { "—" }, emphasize = false)
            if (animal.likedWeather.isNotBlank()) LabelValue("Clima", animal.likedWeather, emphasize = false)
        }
        PixelCard {
            Text("Tus notas", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(foods, { foods = it }, label = { Text("Comidas que tú viste") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(feeders, { feeders = it }, label = { Text("Dónde viste el comedero") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it }, label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())
            PixelButton("Guardar notas", onClick = {
                onSave(
                    AnimalAnnotation(
                        foods = foods.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        feeders = feeders.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        note = note.trim(),
                    ),
                )
            }, outlined = true)
        }
        PixelButton(if (sighted) "✓ Registrado" else "Registrar", onClick = onToggleSighted, outlined = !sighted)
        PixelButton("Volver", onClick = onBack, outlined = true)
    }
}
