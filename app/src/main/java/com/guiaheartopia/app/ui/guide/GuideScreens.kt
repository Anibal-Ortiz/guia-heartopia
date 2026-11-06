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
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.Muted

private val Tabs = listOf("Animales", "Peces", "Insectos", "Pájaros")

@Composable
fun GuideScreen(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onOpenWeather: () -> Unit,
    onOpenMap: () -> Unit,
    onToggleFound: (String) -> Unit,
    onAddCollectible: (String, String) -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
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
            else -> CollectibleTab(Tabs[tab], ui, onToggleFound, onAddCollectible)
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
            animal.name.contains(query.trim(), ignoreCase = true)
    }
    OutlinedTextField(query, { query = it }, label = { Text("Buscar") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
    FlowRow {
        FilterChip(selected = onlySighted, onClick = { onlySighted = !onlySighted }, label = { Text("Registrados") })
    }
    Text("Las comidas coinciden en tres guías. Los comederos no, y siguen pendientes.", style = MaterialTheme.typography.bodySmall)
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

@Composable
private fun CollectibleTab(
    category: String,
    ui: GuideUi,
    onToggleFound: (String) -> Unit,
    onAdd: (String, String) -> Unit,
) {
    var name by rememberSaveable(category) { mutableStateOf("") }
    val items = ui.collectibles.filter { it.category == category }
    Text(
        "Esta pestaña solo muestra entradas con una fuente citada. El catálogo completo sigue pendiente: las bases no coinciden en cuántas especies hay.",
        style = MaterialTheme.typography.bodySmall,
    )
    if (items.isEmpty()) Text("Sin entradas citadas en $category.", style = MaterialTheme.typography.bodyMedium)
    items.forEach { item -> CollectibleCard(item, item.id in ui.found, onToggleFound) }
    OutlinedTextField(name, { name = it }, label = { Text("Añadir en $category") }, modifier = Modifier.fillMaxWidth())
    PixelButton("Añadir nota tuya", onClick = {
        onAdd(name, category)
        name = ""
    }, outlined = true)
}

@Composable
private fun CollectibleCard(item: Collectible, found: Boolean, onToggle: (String) -> Unit) {
    PixelCard {
        Text(item.name, style = MaterialTheme.typography.titleMedium)
        Text(item.detail, color = Muted, style = MaterialTheme.typography.bodySmall)
        if (!item.verified) VerifyBadge("Una fuente, pendiente de contrastar")
        if (item.sourceNote.isNotBlank()) Text(item.sourceNote, style = MaterialTheme.typography.bodySmall)
        PixelButton(
            if (found) "✓ Registrado" else "Registrar",
            onClick = { onToggle(item.id) },
            outlined = !found,
        )
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
        if (animal.foodsVerified) VerifyBadge("Comidas contrastadas", tone = com.guiaheartopia.app.ui.theme.GreenSoft)
        if (!animal.locationVerified) VerifyBadge("Comedero pendiente")
        Text(animal.summary, style = MaterialTheme.typography.bodyLarge)
        PixelCard {
            LabelValue("Comidas", animal.favoriteFoods.joinToString("\n").ifBlank { "Pendiente" })
            LabelValue("Comedero", animal.feederLocations.joinToString().ifBlank { "Pendiente de verificar" }, emphasize = false)
            if (animal.sourceNote.isNotBlank()) Text(animal.sourceNote, style = MaterialTheme.typography.bodySmall)
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
