package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.AficionesLevelSummary
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.guide.LevelDropdown
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.PaperSubtle

@Composable
fun CocinaScreen(
    ui: GuideUi,
    onBack: () -> Unit,
    onOpenReceta: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenAboveLevel: () -> Unit,
    onlyAboveLevel: Boolean = false,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val bundle = remember(revision) { AficionesCatalog.load(context) }
    if (!bundle.cocinaReady) {
        ProntoScreen("Cocina", "Recetas", onBack)
        return
    }
    val cocinaLevel = ui.hobbies.firstOrNull { it.id == "hob-cocina" }?.level ?: 0
    val locked = remember(bundle, cocinaLevel) {
        AficionesLevelSummary.lockedCount(bundle.recetas.map { it.nivelCocina }, cocinaLevel)
    }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    val categorias = remember(bundle) {
        listOf("Todas") + bundle.recetas.mapNotNull { it.categoria }.distinct().sorted()
    }
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var onlyFavoritas by rememberSaveable { mutableStateOf(false) }
    val catalogMax = remember(bundle) { LevelFilter.maximum(bundle.recetas.map { it.nivelCocina }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = bundle.recetas.filter { entry ->
        val catOk = categoria == "Todas" || entry.categoria == categoria
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelCocina, levelCap)
        val aboveOk = !onlyAboveLevel || entry.nivelCocina > cocinaLevel
        val markOk = when (onlyConseguidas) {
            true -> entry.id in ui.recetaFound
            false -> entry.id !in ui.recetaFound
            else -> true
        }
        val favOk = !onlyFavoritas || entry.id in ui.recetaFavorites
        catOk && levelOk && aboveOk && markOk && favOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelCocina }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
    }
    val grouped = sorted.groupBy { it.nivelCocina }.toSortedMap()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_aficiones,
            title = "Cocina",
            subtitle = "Recetas",
        )
        if (locked > 0) {
            PixelCard(container = PaperSubtle) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$locked recetas piden más nivel que el tuyo", style = MaterialTheme.typography.bodyMedium, color = Ink)
                    TextButton(onClick = onOpenAboveLevel) { Text("Ver lista") }
                }
            }
        }
        Text(
            "Conseguidas ${ui.recetaFound.size}/${bundle.recetas.size}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Muted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categorias.forEach { cat ->
                FilterChip(
                    selected = categoria == cat,
                    onClick = { categoria = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HoneySoft, selectedLabelColor = Ink),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = sortByLevel,
                onClick = { sortByLevel = !sortByLevel },
                label = { Text(if (sortByLevel) "Orden: nivel" else "Orden: nombre") },
            )
            FilterChip(
                selected = onlyFavoritas,
                onClick = { onlyFavoritas = !onlyFavoritas },
                label = { Text("Favoritas") },
            )
        }
        LevelDropdown(selected = levelCap, maximum = catalogMax, onSelect = { chosenLevel = it ?: -1 })
        grouped.forEach { (level, items) ->
            Text("Nivel $level", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
            items.forEach { entry ->
                RecetaCard(
                    entry,
                    found = entry.id in ui.recetaFound,
                    favorite = entry.id in ui.recetaFavorites,
                    onOpen = { onOpenReceta(entry.id) },
                    onToggleFound = { onToggleFound(entry.id) },
                    onToggleFavorite = { onToggleFavorite(entry.id) },
                )
            }
        }
    }
}

@Composable
private fun RecetaCard(
    entry: RecetaEntry,
    found: Boolean,
    favorite: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = "Cocina") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Nivel ${entry.nivelCocina}", style = MaterialTheme.typography.bodySmall, color = Muted)
                entry.categoria?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Muted) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FavoritaToggle(favorite, onToggleFavorite)
                ConseguidoToggle(found, onToggleFound)
            }
        }
    }
}

@Composable
fun RecetaDetailScreen(
    entry: RecetaEntry,
    siembras: List<SiembraEntry>,
    found: Boolean,
    favorite: Boolean,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSiembra: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        PixelCard(category = "Cocina") {
            Text(entry.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FavoritaToggle(favorite, onToggleFavorite)
                ConseguidoToggle(found, onToggleFound)
            }
            LabelValue("Nivel de cocina", entry.nivelCocina.toString(), false)
            if (entry.ingredientes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                entry.ingredientes.forEach { ing ->
                    val linked = ing.idSiembra?.let { id -> siembras.firstOrNull { it.id == id } }
                    if (linked != null) {
                        TextButton(onClick = { onOpenSiembra(linked.id) }) {
                            Text(AficionesContent.ingredienteLine(ing, siembras))
                        }
                    } else {
                        Text(AficionesContent.ingredienteLine(ing, siembras), style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                }
            }
            if (entry.energiaEstrellas.isNotEmpty()) {
                Text("Energía por estrellas", style = MaterialTheme.typography.labelMedium, color = Muted)
                StarPriceRow(entry.energiaEstrellas)
            }
            if (entry.precioVentaEstrellas.isNotEmpty()) {
                Text("Venta por estrellas", style = MaterialTheme.typography.labelMedium, color = Muted)
                StarPriceRow(entry.precioVentaEstrellas)
            }
            entry.maestria?.let { LabelValue("Maestría", it, false) }
            entry.categoria?.let { LabelValue("Categoría", it, false) }
            entry.evento?.let { LabelValue("Evento", it, false) }
        }
    }
}