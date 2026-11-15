package com.guiaheartopia.app.ui.hobbies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.CocinaIngredienteLine
import com.guiaheartopia.app.domain.CocinaIngredienteRef
import com.guiaheartopia.app.domain.CocinaIngredients
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
import com.guiaheartopia.app.ui.components.IngredientImage
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.components.rememberAficionesModel
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.guide.LevelDropdown
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted

private val CocinaTabs = listOf("Platos", "Ingredientes")

@Composable
fun CocinaScreen(
    ui: GuideUi,
    onBack: () -> Unit,
    onOpenReceta: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, Boolean) -> Unit,
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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedIngredient by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    val filtros = remember(bundle) {
        val eventos = bundle.recetas.mapNotNull { it.evento }.distinct().sorted()
        listOf("Todas", "Común", "Evento") + eventos
    }
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var onlyCertificadas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val catalogMax = remember(bundle) { LevelFilter.maximum(bundle.recetas.map { it.nivelCocina }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)
    val ingredientCatalog = remember(bundle) {
        CocinaIngredients.catalogUnique(bundle.recetas, bundle.siembras)
    }

  val filteredRecetas = bundle.recetas.filter { entry ->
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
        val certOk = when (onlyCertificadas) {
            true -> entry.id in ui.recetaCertified
            false -> entry.id !in ui.recetaCertified
            else -> true
        }
        searchOk && filtroOk && levelOk && aboveOk && markOk && certOk
    }
    val sortedRecetas = if (sortByLevel) {
        filteredRecetas.sortedWith(compareBy({ it.nivelCocina }, { it.nombre }))
    } else {
        filteredRecetas.sortedBy { it.nombre }
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
        PillTabs(CocinaTabs, tab) {
            tab = it
            selectedIngredient = null
        }
        if (tab == 0) {
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
                onlyCertificadas = onlyCertificadas,
                onCertificadas = { onlyCertificadas = it },
                extraChips = {
                    filtros.forEach { f ->
                        FilterChip(
                            selected = filtro == f,
                            onClick = { filtro = f },
                            label = { Text(f, maxLines = 1, overflow = TextOverflow.Clip) },
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
                "${sortedRecetas.size} en esta lista",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Muted,
            )
            sortedRecetas.forEach { entry ->
                RecetaCard(
                    entry = entry,
                    siembras = bundle.siembras,
                    recetas = bundle.recetas,
                    found = entry.id in ui.recetaFound,
                    certified = entry.id in ui.recetaCertified,
                    onOpen = { onOpenReceta(entry.id) },
                    onToggleFound = { onToggleFound(entry.id) },
                    onToggleCertified = {
                        onToggleCertified(entry.id, AficionesMarks.certifiableReceta(entry))
                    },
                )
            }
        } else {
            val selected = selectedIngredient?.let { key ->
                ingredientCatalog.firstOrNull { it.key == key }
            }
            if (selected != null) {
                TextButton(onClick = { selectedIngredient = null }) { Text("← Ingredientes") }
                Text(
                    selected.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                IngredientImage(
                    imagen = CocinaIngredients.resolveImagen(selected, bundle.siembras, bundle.recetas),
                    size = 72.dp,
                    contentDescription = selected.displayName,
                )
                Text("Recetas que lo usan", style = MaterialTheme.typography.titleMedium, color = Ink)
                CocinaIngredients.recetasConIngrediente(bundle.recetas, bundle.siembras, selected.key)
                    .sortedBy { it.nombre }
                    .forEach { entry ->
                        TextButton(onClick = { onOpenReceta(entry.id) }) { Text(entry.nombre) }
                    }
            } else {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar ingrediente...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                val filteredIng = ingredientCatalog.filter {
                    query.isBlank() || TextMatch.contains(it.displayName, query)
                }
                filteredIng.forEach { ref ->
                    IngredientListRow(ref, bundle.siembras, bundle.recetas) {
                        selectedIngredient = ref.key
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientListRow(
    ref: CocinaIngredienteRef,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
    onClick: () -> Unit,
) {
    PixelCard(Modifier.clickable(onClick = onClick).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IngredientImage(
                imagen = CocinaIngredients.resolveImagen(ref, siembras, recetas),
                size = 56.dp,
                contentDescription = ref.displayName,
            )
            Text(ref.displayName, style = MaterialTheme.typography.titleMedium, color = Ink)
        }
    }
}

@Composable
private fun RecetaCard(
    entry: RecetaEntry,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
    found: Boolean,
    certified: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableReceta(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
    val model = rememberAficionesModel(entry.imagen)
    val ingredientLines = remember(entry, siembras, recetas) {
        CocinaIngredients.linesForReceta(entry, siembras, recetas)
    }
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
            verticalAlignment = Alignment.Top,
        ) {
            AficionesImage(
                imagen = entry.imagen,
                sectionLabel = "Cocina",
                size = 88.dp,
                contentDescription = ImageZoom.enlargedDescription(entry.nombre),
                onClick = { if (model != null) zoom = true },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Nivel ${entry.nivelCocina}", style = MaterialTheme.typography.bodySmall, color = Muted)
                entry.evento?.let {
                    Text("Evento: $it", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                AficionCertifyRow(
                    found = found,
                    certified = certified,
                    certifiable = certifiable,
                    onToggleFound = onToggleFound,
                    onToggleCertified = onToggleCertified,
                )
                if (ingredientLines.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ingredientLines.forEach { line ->
                            IngredientMini(line, siembras, recetas)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IngredientMini(
    line: CocinaIngredienteLine,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
) {
    val imagen = CocinaIngredients.resolveImagen(line.ref, siembras, recetas)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IngredientImage(imagen = imagen, size = 36.dp, contentDescription = line.ref.displayName)
        line.cantidad?.let {
            Text("×$it", style = MaterialTheme.typography.labelSmall, color = Muted)
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
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
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
            entry.origen?.takeIf { it.isNotBlank() }?.let { origen ->
                LabelValue("Origen", origen, false)
            }
            if (entry.ingredientes.isNotEmpty()) {
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
    val elegir = ing.elegir
    if (elegir != null && ing.opciones.isNotEmpty()) {
        Text(
            "Elige $elegir de los siguientes:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Ink,
        )
        ing.opciones.forEach { opt ->
            val ref = CocinaIngredients.refForOpcion(opt, siembras, recetas)
            SingleIngredientRow(ref, null, siembras, recetas, onOpenSiembra, onOpenReceta)
        }
        return
    }
    val ref = CocinaIngredients.refForIngrediente(ing, siembras, recetas)
    SingleIngredientRow(ref, ing.cantidad, siembras, recetas, onOpenSiembra, onOpenReceta)
}

@Composable
private fun SingleIngredientRow(
    ref: CocinaIngredienteRef,
    cantidad: Int?,
    siembras: List<SiembraEntry>,
    recetas: List<RecetaEntry>,
    onOpenSiembra: (String) -> Unit,
    onOpenReceta: (String) -> Unit,
) {
    val imagen = CocinaIngredients.resolveImagen(ref, siembras, recetas)
    val suffix = cantidad?.let { " ×$it" }.orEmpty()
    val label = ref.displayName + suffix
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IngredientImage(imagen = imagen, size = 44.dp, contentDescription = ref.displayName)
        when {
            ref.idSiembra != null -> TextButton(onClick = { onOpenSiembra(ref.idSiembra) }) { Text(label) }
            ref.idReceta != null -> TextButton(onClick = { onOpenReceta(ref.idReceta) }) { Text(label) }
            else -> Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink)
        }
    }
}