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
import androidx.compose.material3.OutlinedTextField
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
import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.AficionesLevelSummary
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.RecetaIngrediente
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.domain.TextMatch
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.AficionesImage
import com.guiaheartopia.app.ui.components.CreatureZoomDialog
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.rememberAficionesModel
import com.guiaheartopia.app.ui.guide.LevelDropdown
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.GreenDark
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
    onToggleCertified: (String, Boolean) -> Unit,
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
    var query by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    val filtros = remember(bundle) {
        val eventos = bundle.recetas.mapNotNull { it.evento }.distinct().sorted()
        listOf("Todas", "Común", "Evento") + eventos
    }
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var onlyFavoritas by rememberSaveable { mutableStateOf(false) }
    val catalogMax = remember(bundle) { LevelFilter.maximum(bundle.recetas.map { it.nivelCocina }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = bundle.recetas.filter { entry ->
        val searchOk = query.isBlank() || TextMatch.contains(entry.nombre, query)
        val filtroOk = when (filtro) {
            "Todas" -> true
            "Común" -> entry.categoria.equals("Común", ignoreCase = true)
            "Evento" -> entry.categoria.equals("Evento", ignoreCase = true)
            else -> entry.evento.equals(filtro, ignoreCase = true)
        }
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelCocina, levelCap)
        val aboveOk = !onlyAboveLevel || entry.nivelCocina > cocinaLevel
        val markOk = when (onlyConseguidas) {
            true -> entry.id in ui.recetaFound
            false -> entry.id !in ui.recetaFound
            else -> true
        }
        val favOk = !onlyFavoritas || entry.id in ui.recetaFavorites
        searchOk && filtroOk && levelOk && aboveOk && markOk && favOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelCocina }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
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
            title = "Cocina",
            subtitle = "${bundle.recetas.size} recetas",
        )
        if (locked > 0) {
            PixelCard(container = PaperSubtle) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$locked recetas piden más nivel que el tuyo", style = MaterialTheme.typography.bodyMedium, color = Ink)
                    TextButton(onClick = onOpenAboveLevel) { Text("Ver lista") }
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar por nombre...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        AficionesListHeader(
            found = ui.recetaFound.size,
            total = bundle.recetas.size,
            sortByLevel = sortByLevel,
            onSort = { sortByLevel = !sortByLevel },
            onlyConseguidas = onlyConseguidas,
            onConseguidas = { onlyConseguidas = it },
            extraChips = {
                FilterChip(
                    selected = onlyFavoritas,
                    onClick = { onlyFavoritas = !onlyFavoritas },
                    label = { Text("Favoritas", maxLines = 1) },
                )
                filtros.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(f, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HoneySoft,
                            selectedLabelColor = Ink,
                        ),
                    )
                }
            },
        )
        LevelDropdown(selected = levelCap, maximum = catalogMax, onSelect = { chosenLevel = it ?: -1 })
        Text(
            "${sorted.size} en esta lista",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = Muted,
        )
        sorted.forEach { entry ->
            RecetaCard(
                entry,
                found = entry.id in ui.recetaFound,
                certified = entry.id in ui.recetaCertified,
                favorite = entry.id in ui.recetaFavorites,
                onOpen = { onOpenReceta(entry.id) },
                onToggleFound = { onToggleFound(entry.id) },
                onToggleCertified = {
                    onToggleCertified(entry.id, AficionesMarks.certifiableReceta(entry))
                },
                onToggleFavorite = { onToggleFavorite(entry.id) },
            )
        }
    }
}

@Composable
private fun RecetaCard(
    entry: RecetaEntry,
    found: Boolean,
    certified: Boolean,
    favorite: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableReceta(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
    val model = rememberAficionesModel(entry.imagen)
    if (zoom && model != null) {
        CreatureZoomDialog(
            name = entry.nombre,
            model = model,
            category = "Cocina",
            locations = emptyList(),
            level = entry.nivelCocina,
            onDismiss = { zoom = false },
        )
    }
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = "Cocina") {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AficionesImage(
                imagen = entry.imagen,
                sectionLabel = "Cocina",
                size = 72.dp,
                contentDescription = ImageZoom.enlargedDescription(entry.nombre),
                onClick = { if (model != null) zoom = true },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Nivel ${entry.nivelCocina}", style = MaterialTheme.typography.bodySmall, color = Muted)
                entry.categoria?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Muted) }
                entry.evento?.let { Text("Evento: $it", style = MaterialTheme.typography.bodySmall, color = Muted) }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                FavoritaToggle(favorite, onToggleFavorite)
                AficionCertifyRow(
                    found = found,
                    certified = certified,
                    certifiable = certifiable,
                    onToggleFound = onToggleFound,
                    onToggleCertified = onToggleCertified,
                )
            }
        }
    }
}

@Composable
fun RecetaDetailScreen(
    entry: RecetaEntry,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
    found: Boolean,
    certified: Boolean,
    favorite: Boolean,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenSiembra: (String) -> Unit,
    onOpenReceta: (String) -> Unit,
) {
    val certifiable = AficionesMarks.certifiableReceta(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
    val model = rememberAficionesModel(entry.imagen)
    if (zoom && model != null) {
        CreatureZoomDialog(
            name = entry.nombre,
            model = model,
            category = "Cocina",
            locations = emptyList(),
            level = entry.nivelCocina,
            onDismiss = { zoom = false },
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("Atrás") }
        PixelCard(category = "Cocina") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                AficionesImage(
                    imagen = entry.imagen,
                    sectionLabel = "Cocina",
                    size = 96.dp,
                    contentDescription = ImageZoom.enlargedDescription(entry.nombre),
                    onClick = { if (model != null) zoom = true },
                )
                Column(Modifier.weight(1f)) {
                    Text(entry.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FavoritaToggle(favorite, onToggleFavorite)
                    }
                    AficionCertifyRow(
                        found = found,
                        certified = certified,
                        certifiable = certifiable,
                        onToggleFound = onToggleFound,
                        onToggleCertified = onToggleCertified,
                    )
                }
            }
            LabelValue("Nivel de cocina", entry.nivelCocina.toString(), false)
            entry.categoria?.let { LabelValue("Categoría", it, false) }
            entry.evento?.let { LabelValue("Evento", it, false) }
            if (!certifiable) {
                Text("No certificable", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Muted)
            }
            val origen = entry.origen?.takeIf { it.isNotBlank() }
            if (origen != null) {
                LabelValue("Origen", origen, false)
            } else if (entry.ingredientes.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Ingredientes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                entry.ingredientes.forEach { ing ->
                    IngredienteRow(ing, siembras, recetas, onOpenSiembra, onOpenReceta)
                }
                if (!entry.ingredientesCompletos) {
                    Text(
                        "Faltan ${entry.ingredientesPendientes} ingredientes por confirmar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenDark,
                    )
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
        }
    }
}

@Composable
private fun IngredienteRow(
    ing: RecetaIngrediente,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
    onOpenSiembra: (String) -> Unit,
    onOpenReceta: (String) -> Unit,
) {
    val line = AficionesContent.ingredienteLine(ing, siembras, recetas)
    val sid = ing.idSiembra
    val rid = ing.idReceta
    when {
        sid != null -> TextButton(onClick = { onOpenSiembra(sid) }) { Text(line) }
        rid != null -> TextButton(onClick = { onOpenReceta(rid) }) { Text(line) }
        else -> Text(line, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}