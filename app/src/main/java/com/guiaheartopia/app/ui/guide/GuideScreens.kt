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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.VerifyBadge

@Composable
fun GuideScreen(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onOpenWeather: () -> Unit,
    onToggleFound: (String) -> Unit,
    onAddCollectible: (String, String) -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Animales") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Coleccionables") })
        }
        when (tab) {
            0 -> AnimalsTab(ui, onOpenAnimal, onOpenWeather, onAddAnimal)
            else -> CollectiblesTab(ui, onToggleFound, onAddCollectible)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnimalsTab(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onOpenWeather: () -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("Todos") }
    var onlySighted by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }
    var newGroup by rememberSaveable { mutableStateOf("") }
    var newFoods by rememberSaveable { mutableStateOf("") }
    var newFeeders by rememberSaveable { mutableStateOf("") }
    val groups = listOf("Todos") + ui.animals.map { it.group }.distinct().sorted()
    val shown = ui.animals.filter { animal ->
        val blob = listOf(animal.name, animal.group, animal.summary, animal.favoriteFoods.joinToString(), animal.feederLocations.joinToString())
            .joinToString(" ")
        (group == "Todos" || animal.group == group) &&
            (!onlySighted || animal.id in ui.sighted) &&
            blob.contains(query.trim(), ignoreCase = true)
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VerifyBadge("Datos iniciales por verificar")
        Text(
            "Las fichas de ejemplo no traen comidas ni comederos reales. Puedes anotar los tuyos en cada ficha.",
            style = MaterialTheme.typography.bodyMedium,
        )
        PixelButton("Abrir calendario de climas", onClick = onOpenWeather, container = com.guiaheartopia.app.ui.theme.Sky)
        OutlinedTextField(query, { query = it }, label = { Text("Buscar") }, modifier = Modifier.fillMaxWidth())
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            groups.forEach { name ->
                FilterChip(selected = group == name, onClick = { group = name }, label = { Text(name) })
            }
            FilterChip(selected = onlySighted, onClick = { onlySighted = !onlySighted }, label = { Text("Avistados") })
        }
        if (shown.isEmpty()) {
            Text("Ninguna ficha coincide.", style = MaterialTheme.typography.bodyMedium)
        }
        PixelCard {
            Text("Añadir una ficha tuya", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(newName, { newName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(newGroup, { newGroup = it }, label = { Text("Grupo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(newFoods, { newFoods = it }, label = { Text("Comidas, separadas por comas") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(newFeeders, { newFeeders = it }, label = { Text("Comederos") }, modifier = Modifier.fillMaxWidth())
            PixelButton("Guardar ficha", onClick = {
                onAddAnimal(newName, newGroup, newFoods, newFeeders)
                newName = ""
                newFoods = ""
                newFeeders = ""
            })
        }
        shown.forEach { animal ->
            PixelCard(Modifier.clickable { onOpenAnimal(animal.id) }) {
                Text(animal.name, style = MaterialTheme.typography.titleMedium)
                Text(animal.group, style = MaterialTheme.typography.bodySmall)
                if (!animal.verified) VerifyBadge(if (animal.playerOwned) "Nota tuya" else "Datos iniciales por verificar")
                if (animal.id in ui.sighted) Text("Marcado como avistado", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CollectiblesTab(
    ui: GuideUi,
    onToggleFound: (String) -> Unit,
    onAdd: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VerifyBadge("Datos iniciales por verificar")
        Text("Marca lo que tú hayas encontrado. El catálogo de ejemplo no es oficial.", style = MaterialTheme.typography.bodyMedium)
        ui.categoryProgress.forEach { progress ->
            PixelCard {
                Text("${progress.category}: ${progress.found}/${progress.total}", style = MaterialTheme.typography.titleSmall)
                PixelProgress(progress.fraction)
            }
        }
        ui.collectibles.groupBy { it.category }.forEach { (categoryName, items) ->
            Text(categoryName, style = MaterialTheme.typography.titleMedium)
            items.forEach { item ->
                PixelCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = item.id in ui.found, onCheckedChange = { onToggleFound(item.id) })
                        Column(Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
                            Text(item.detail, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        OutlinedTextField(name, { name = it }, label = { Text("Nuevo coleccionable") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(category, { category = it }, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth())
        PixelButton("Añadir", onClick = {
            onAdd(name, category)
            name = ""
        })
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(animal.name, style = MaterialTheme.typography.headlineSmall)
        Text(animal.group, style = MaterialTheme.typography.bodyMedium)
        if (!animal.verified) {
            VerifyBadge(if (animal.playerOwned) "Nota tuya" else "Datos iniciales por verificar")
        }
        Text(animal.summary, style = MaterialTheme.typography.bodyLarge)
        PixelCard {
            Text("Comidas favoritas", style = MaterialTheme.typography.titleMedium)
            if (animal.favoriteFoods.isEmpty()) {
                Text("Pendiente de verificar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                animal.favoriteFoods.forEach { Text("· $it") }
            }
            Text("Comederos", style = MaterialTheme.typography.titleMedium)
            if (animal.feederLocations.isEmpty()) {
                Text("Pendiente de verificar.", style = MaterialTheme.typography.bodyMedium)
            } else {
                animal.feederLocations.forEach { Text("· $it") }
            }
        }
        PixelCard {
            Text("Lo que tú has comprobado", style = MaterialTheme.typography.titleMedium)
            Text("Estas notas se quedan en el teléfono y no se presentan como dato oficial.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(foods, { foods = it }, label = { Text("Comidas, una por línea") }, modifier = Modifier.fillMaxWidth())
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
            })
        }
        PixelButton(if (sighted) "Quitar avistado" else "Marcar como avistado", onClick = onToggleSighted)
        PixelButton("Volver", onClick = onBack, container = com.guiaheartopia.app.ui.theme.Peach)
    }
}
