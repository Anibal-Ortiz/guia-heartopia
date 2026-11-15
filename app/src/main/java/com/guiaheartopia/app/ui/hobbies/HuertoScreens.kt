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
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.AficionesLevelSummary
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.guide.LevelDropdown
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.PaperSubtle
import java.time.Instant

private val HuertoTabs = listOf("Siembras", "Flores")

@Composable
fun HuertoScreen(
    ui: GuideUi,
    onBack: () -> Unit,
    onOpenSiembra: (String) -> Unit,
    onOpenFlor: (String) -> Unit,
    onToggleSiembraFound: (String) -> Unit,
    onToggleFlorFound: (String) -> Unit,
    onCancelCrop: (String) -> Unit,
    onOpenAboveLevel: () -> Unit,
    initialTab: Int = 0,
    onlyAboveLevel: Boolean = false,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val bundle = remember(revision) { AficionesCatalog.load(context) }
  var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    if (!bundle.huertoReady) {
        ProntoScreen("Huerto", "Siembras y flores", onBack)
        return
    }
    val siembraLevel = ui.hobbies.firstOrNull { it.id == "hob-siembra" }?.level ?: 0
    val lockedSiembras = remember(bundle, siembraLevel) {
        AficionesLevelSummary.lockedCount(bundle.siembras.map { it.nivelSiembra }, siembraLevel) +
            AficionesLevelSummary.lockedCount(bundle.flores.mapNotNull { it.nivelSiembra }, siembraLevel)
    }

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
            title = "Huerto",
            subtitle = "Siembras y flores",
        )
        if (lockedSiembras > 0) {
            LevelLockedBanner(lockedSiembras, onOpenAboveLevel)
        }
        if (ui.activeCrops.isNotEmpty()) {
            ActiveCropsPanel(ui.activeCrops, onCancelCrop)
        }
        PillTabs(HuertoTabs, tab) { tab = it }
        when (tab) {
            0 -> SiembrasTab(
                siembras = bundle.siembras,
                found = ui.huertoFound,
                playerLevel = siembraLevel,
                onlyAboveLevel = onlyAboveLevel,
                onOpen = onOpenSiembra,
                onToggleFound = onToggleSiembraFound,
            )
            else -> FloresTab(
                flores = bundle.flores,
                found = ui.florFound,
                playerLevel = siembraLevel,
                onlyAboveLevel = onlyAboveLevel,
                onOpen = onOpenFlor,
                onToggleFound = onToggleFlorFound,
            )
        }
    }
}

@Composable
fun ProntoScreen(title: String, subtitle: String, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        Text(title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
        PixelCard { Text("Próximamente", style = MaterialTheme.typography.titleMedium, color = Ink) }
    }
}

@Composable
private fun LevelLockedBanner(count: Int, onOpen: () -> Unit) {
    PixelCard(container = PaperSubtle) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$count fichas piden más nivel que el tuyo", style = MaterialTheme.typography.bodyMedium, color = Ink)
            TextButton(onClick = onOpen) { Text("Ver lista") }
        }
    }
}

@Composable
private fun ActiveCropsPanel(crops: List<ActiveCrop>, onCancel: (String) -> Unit) {
    val now = Instant.now().toEpochMilli()
    PixelCard(category = "Siembra") {
        Text("Cultivos en curso", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        crops.forEach { crop ->
            val remaining = (crop.readyAtEpochMilli - now).coerceAtLeast(0)
            val label = AficionesContent.formatGrowthMinutes((remaining / 60_000).toInt())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(crop.nombre, style = MaterialTheme.typography.bodyLarge, color = Ink)
                    Text("Listo en $label", style = MaterialTheme.typography.bodySmall, color = GreenDark)
                }
                TextButton(onClick = { onCancel(crop.id) }) { Text("Cancelar") }
            }
        }
    }
}

@Composable
private fun SiembrasTab(
    siembras: List<SiembraEntry>,
    found: Set<String>,
    playerLevel: Int,
    onlyAboveLevel: Boolean,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
) {
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val catalogMax = remember(siembras) { LevelFilter.maximum(siembras.map { it.nivelSiembra }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = siembras.filter { entry ->
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelSiembra, levelCap)
        val aboveOk = !onlyAboveLevel || entry.nivelSiembra > playerLevel
        val markOk = when (onlyConseguidas) {
            true -> entry.id in found
            false -> entry.id !in found
            else -> true
        }
        levelOk && aboveOk && markOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelSiembra }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
    }
    val grouped = sorted.groupBy { it.nivelSiembra }.toSortedMap()

    ListToolbar(
        found = found.size,
        total = siembras.size,
        sortByLevel = sortByLevel,
        onSort = { sortByLevel = !sortByLevel },
        onlyConseguidas = onlyConseguidas,
        onConseguidas = { onlyConseguidas = it },
    )
    LevelDropdown(selected = levelCap, maximum = catalogMax, onSelect = { chosenLevel = it ?: -1 })

    grouped.forEach { (level, items) ->
        Text("Nivel $level", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
        items.forEach { entry ->
            SiembraCard(entry, entry.id in found, onOpen = { onOpen(entry.id) }, onToggleFound = { onToggleFound(entry.id) })
        }
    }
}

@Composable
private fun FloresTab(
    flores: List<FlorEntry>,
    found: Set<String>,
    playerLevel: Int,
    onlyAboveLevel: Boolean,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
) {
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val catalogMax = remember(flores) { LevelFilter.maximum(flores.map { it.nivelSiembra }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = flores.filter { entry ->
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelSiembra, levelCap)
        val aboveOk = !onlyAboveLevel || (entry.nivelSiembra != null && entry.nivelSiembra > playerLevel)
        val markOk = when (onlyConseguidas) {
            true -> entry.id in found
            false -> entry.id !in found
            else -> true
        }
        levelOk && aboveOk && markOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelSiembra ?: 99 }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
    }
    val withLevel = sorted.filter { it.nivelSiembra != null }.groupBy { it.nivelSiembra!! }.toSortedMap()
    val sinNivel = sorted.filter { it.nivelSiembra == null }

    ListToolbar(
        found = found.size,
        total = flores.size,
        sortByLevel = sortByLevel,
        onSort = { sortByLevel = !sortByLevel },
        onlyConseguidas = onlyConseguidas,
        onConseguidas = { onlyConseguidas = it },
    )
    if (catalogMax > 0) {
        LevelDropdown(selected = levelCap, maximum = catalogMax, onSelect = { chosenLevel = it ?: -1 })
    }

    withLevel.forEach { (level, items) ->
        Text("Nivel $level", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
        items.forEach { entry ->
            FlorCard(entry, entry.id in found, onOpen = { onOpen(entry.id) }, onToggleFound = { onToggleFound(entry.id) })
        }
    }
    if (sinNivel.isNotEmpty()) {
        Text("Sin nivel", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
        sinNivel.forEach { entry ->
            FlorCard(entry, entry.id in found, onOpen = { onOpen(entry.id) }, onToggleFound = { onToggleFound(entry.id) })
        }
    }
}

@Composable
private fun ListToolbar(
    found: Int,
    total: Int,
    sortByLevel: Boolean,
    onSort: () -> Unit,
    onlyConseguidas: Boolean?,
    onConseguidas: (Boolean?) -> Unit,
) {
    Text("Conseguidas $found/$total", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Muted)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = sortByLevel,
            onClick = onSort,
            label = { Text(if (sortByLevel) "Orden: nivel" else "Orden: nombre") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HoneySoft, selectedLabelColor = Ink),
        )
        FilterChip(
            selected = onlyConseguidas == true,
            onClick = { onConseguidas(if (onlyConseguidas == true) null else true) },
            label = { Text("Conseguidas") },
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenSoft, selectedLabelColor = GreenDark),
        )
        FilterChip(
            selected = onlyConseguidas == false,
            onClick = { onConseguidas(if (onlyConseguidas == false) null else false) },
            label = { Text("Pendientes") },
        )
    }
}

@Composable
private fun SiembraCard(entry: SiembraEntry, found: Boolean, onOpen: () -> Unit, onToggleFound: () -> Unit) {
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = "Siembra") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Nivel ${entry.nivelSiembra}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            ConseguidoToggle(found, onToggleFound)
        }
    }
}

@Composable
private fun FlorCard(entry: FlorEntry, found: Boolean, onOpen: () -> Unit, onToggleFound: () -> Unit) {
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = entry.nombre) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                entry.nivelSiembra?.let { Text("Nivel $it", style = MaterialTheme.typography.bodySmall, color = Muted) }
            }
            ConseguidoToggle(found, onToggleFound)
        }
    }
}

@Composable
fun SiembraDetailScreen(
    entry: SiembraEntry,
    recetas: List<RecetaEntry>,
    found: Boolean,
    activeCrop: ActiveCrop?,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onPlant: () -> Unit,
    onOpenReceta: (String) -> Unit,
) {
    val usadas = remember(entry, recetas) { AficionesContent.recetasQueUsanSiembra(recetas, entry) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        PixelCard(category = "Siembra") {
            Text(entry.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { ConseguidoToggle(found, onToggleFound) }
            LabelValue("Nivel de siembra", entry.nivelSiembra.toString(), false)
            if (entry.tiempoCrecimientoMinutos > 0) {
                LabelValue("Tiempo de crecimiento", AficionesContent.formatGrowthMinutes(entry.tiempoCrecimientoMinutos), false)
            }
            entry.precioSemilla?.let { LabelValue("Precio de semilla", it.toString(), false) }
            entry.fuenteSemilla?.let { LabelValue("Dónde comprar", it, false) }
            if (entry.precioVentaEstrellas.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Venta por estrellas", style = MaterialTheme.typography.labelMedium, color = Muted)
                StarPriceRow(entry.precioVentaEstrellas)
            }
            entry.maestriaCosechas?.let { LabelValue("Maestría", it, false) }
            entry.categoria?.let { LabelValue("Categoría", it, false) }
            entry.evento?.let { LabelValue("Evento", it, false) }
            if (usadas.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Se usa en", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                usadas.forEach { receta ->
                    TextButton(onClick = { onOpenReceta(receta.id) }) { Text(receta.nombre) }
                }
            }
            if (entry.tiempoCrecimientoMinutos > 0) {
                if (activeCrop == null) {
                    PixelButton(
                        text = "Avísame cuando esté lista",
                        onClick = onPlant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text("Ya tienes un temporizador para este cultivo.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun FlorDetailScreen(
    entry: FlorEntry,
    found: Boolean,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        PixelCard(category = entry.nombre) {
            Text(entry.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
            ConseguidoToggle(found, onToggleFound)
            entry.nivelSiembra?.let { LabelValue("Nivel de siembra", it.toString(), false) }
            entry.variantes.forEach { v ->
                val origen = AficionesContent.origenFlorLabel(v.origen)
                val precio = v.precioVenta?.let { " · $it" }.orEmpty()
                val estrellas = v.estrellas?.let { " · ★$it" }.orEmpty()
                LabelValue(v.color, "$origen$precio$estrellas", false)
            }
        }
    }
}