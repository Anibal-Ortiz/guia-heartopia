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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.ui.components.AficionesImage
import com.guiaheartopia.app.ui.components.CreatureZoomDialog
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.rememberAficionesModel
import com.guiaheartopia.app.ui.guide.LevelDropdown
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Muted
import java.time.Instant

private val HuertoTabs = listOf("Siembras", "Flores")

@Composable
fun HuertoScreen(
    ui: GuideUi,
    onBack: () -> Unit,
    onOpenSiembra: (String) -> Unit,
    onOpenFlor: (String) -> Unit,
    onToggleSiembraFound: (String) -> Unit,
    onToggleSiembraCertified: (String, Boolean) -> Unit,
    onToggleFlorFound: (String) -> Unit,
    onToggleFlorCertified: (String, Boolean) -> Unit,
    onCancelCrop: (String) -> Unit,
    initialTab: Int = 0,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val bundle = remember(revision) { AficionesCatalog.load(context) }
  var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    if (!bundle.huertoReady) {
        ProntoScreen("Huerto", "Siembras y flores", onBack)
        return
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
        if (ui.activeCrops.isNotEmpty()) {
            ActiveCropsPanel(ui.activeCrops, onCancelCrop)
        }
        PillTabs(HuertoTabs, tab) { tab = it }
        when (tab) {
            0 -> SiembrasTab(
                siembras = bundle.siembras,
                found = ui.huertoFound,
                certified = ui.huertoCertified,
                onOpen = onOpenSiembra,
                onToggleFound = onToggleSiembraFound,
                onToggleCertified = onToggleSiembraCertified,
            )
            else -> FloresTab(
                flores = bundle.flores,
                found = ui.florFound,
                certified = ui.florCertified,
                onOpen = onOpenFlor,
                onToggleFound = onToggleFlorFound,
                onToggleCertified = onToggleFlorCertified,
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
    certified: Set<String>,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, Boolean) -> Unit,
) {
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val catalogMax = remember(siembras) { LevelFilter.maximum(siembras.map { it.nivelSiembra }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = siembras.filter { entry ->
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelSiembra, levelCap)
        val markOk = when (onlyConseguidas) {
            true -> entry.id in found
            false -> entry.id !in found
            else -> true
        }
        levelOk && markOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelSiembra }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
    }
    val grouped = sorted.groupBy { it.nivelSiembra }.toSortedMap()

    AficionesListHeader(
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
            SiembraCard(
                entry,
                found = entry.id in found,
                certified = entry.id in certified,
                onOpen = { onOpen(entry.id) },
                onToggleFound = { onToggleFound(entry.id) },
                onToggleCertified = { onToggleCertified(entry.id, AficionesMarks.certifiableSiembra(entry)) },
            )
        }
    }
}

@Composable
private fun FloresTab(
    flores: List<FlorEntry>,
    found: Set<String>,
    certified: Set<String>,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, Boolean) -> Unit,
) {
    var sortByLevel by rememberSaveable { mutableStateOf(false) }
    var onlyConseguidas by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val catalogMax = remember(flores) { LevelFilter.maximum(flores.map { it.nivelSiembra }) ?: 0 }
    var chosenLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levelCap = if (chosenLevel < 0) null else chosenLevel.coerceAtMost(catalogMax)

    val filtered = flores.filter { entry ->
        val levelOk = CreatureAvailability.levelAllowed(entry.nivelSiembra, levelCap)
        val markOk = when (onlyConseguidas) {
            true -> entry.id in found
            false -> entry.id !in found
            else -> true
        }
        levelOk && markOk
    }
    val sorted = if (sortByLevel) {
        filtered.sortedWith(compareBy({ it.nivelSiembra ?: 99 }, { it.nombre }))
    } else {
        filtered.sortedBy { it.nombre }
    }
    val withLevel = sorted.filter { it.nivelSiembra != null }.groupBy { it.nivelSiembra!! }.toSortedMap()
    val sinNivel = sorted.filter { it.nivelSiembra == null }

    AficionesListHeader(
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
            FlorCard(
                entry,
                found = entry.id in found,
                certified = entry.id in certified,
                onOpen = { onOpen(entry.id) },
                onToggleFound = { onToggleFound(entry.id) },
                onToggleCertified = { onToggleCertified(entry.id, AficionesMarks.certifiableFlor(entry)) },
            )
        }
    }
    if (sinNivel.isNotEmpty()) {
        Text("Sin nivel", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
        sinNivel.forEach { entry ->
            FlorCard(
                entry,
                found = entry.id in found,
                certified = entry.id in certified,
                onOpen = { onOpen(entry.id) },
                onToggleFound = { onToggleFound(entry.id) },
                onToggleCertified = { onToggleCertified(entry.id, AficionesMarks.certifiableFlor(entry)) },
            )
        }
    }
}

@Composable
private fun SiembraCard(
    entry: SiembraEntry,
    found: Boolean,
    certified: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableSiembra(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
  val model = rememberAficionesModel(entry.imagen)
    if (zoom && model != null) {
        CreatureZoomDialog(
            name = entry.nombre,
            model = model,
            category = "Siembra",
            locations = emptyList(),
            level = entry.nivelSiembra,
            onDismiss = { zoom = false },
        )
    }
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = "Siembra") {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AficionesImage(
                imagen = entry.imagen,
                sectionLabel = "Siembra",
                size = 72.dp,
                contentDescription = ImageZoom.enlargedDescription(entry.nombre),
                onClick = { if (model != null) zoom = true },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text("Nivel ${entry.nivelSiembra}", style = MaterialTheme.typography.bodySmall, color = Muted)
                entry.precioSemilla?.let {
                    Text("Semilla $it", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                if (entry.tiempoCrecimientoMinutos > 0) {
                    Text(
                        AficionesContent.formatGrowthMinutes(entry.tiempoCrecimientoMinutos),
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenDark,
                    )
                }
                if (entry.precioVentaEstrellas.isNotEmpty()) {
                    Text(
                        "Venta ★1: ${entry.precioVentaEstrellas.first()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                }
                entry.evento?.let { Text("Evento: $it", style = MaterialTheme.typography.bodySmall, color = Muted) }
            }
            Column(horizontalAlignment = Alignment.End) {
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
private fun FlorCard(
    entry: FlorEntry,
    found: Boolean,
    certified: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableFlor(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
    val model = rememberAficionesModel(entry.imagen)
    if (zoom && model != null) {
        CreatureZoomDialog(
            name = entry.nombre,
            model = model,
            category = entry.nombre,
            locations = emptyList(),
            level = entry.nivelSiembra,
            onDismiss = { zoom = false },
        )
    }
    PixelCard(Modifier.clickable(onClick = onOpen).fillMaxWidth(), category = entry.nombre) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AficionesImage(
                imagen = entry.imagen,
                sectionLabel = entry.nombre,
                size = 72.dp,
                contentDescription = ImageZoom.enlargedDescription(entry.nombre),
                onClick = { if (model != null) zoom = true },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.nombre, style = MaterialTheme.typography.titleMedium, color = Ink)
                entry.nivelSiembra?.let { Text("Nivel $it", style = MaterialTheme.typography.bodySmall, color = Muted) }
                entry.precioSemilla?.let { Text("Semilla $it", style = MaterialTheme.typography.bodySmall, color = Muted) }
                entry.tiempoCrecimientoMinutos?.let {
                    Text(
                        AficionesContent.formatGrowthMinutes(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenDark,
                    )
                }
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
}

@Composable
fun SiembraDetailScreen(
    entry: SiembraEntry,
    recetas: List<RecetaEntry>,
    found: Boolean,
    certified: Boolean,
    activeCrop: ActiveCrop?,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
    onPlant: () -> Unit,
    onOpenReceta: (String) -> Unit,
) {
    val certifiable = AficionesMarks.certifiableSiembra(entry)
    val usadas = remember(entry, recetas) { AficionesContent.recetasQueUsanSiembra(recetas, entry) }
    var zoom by remember(entry.id) { mutableStateOf(false) }
    val model = rememberAficionesModel(entry.imagen)
    if (zoom && model != null) {
        CreatureZoomDialog(
            name = entry.nombre,
            model = model,
            category = "Siembra",
            locations = emptyList(),
            level = entry.nivelSiembra,
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
        PixelCard(category = "Siembra") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                AficionesImage(
                    imagen = entry.imagen,
                    sectionLabel = "Siembra",
                    size = 96.dp,
                    shapeRadius = 20.dp,
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
            if (!certifiable) {
                Text("No certificable", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Muted)
            }
            if (usadas.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Recetas que la usan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
    certified: Boolean,
    onBack: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableFlor(entry)
    var zoomId by remember { mutableStateOf<String?>(null) }
    val zoomVariant = entry.variantes.firstOrNull { it.id == zoomId || it.imagen == zoomId }
    val zoomPath = zoomVariant?.imagen ?: entry.imagen
    val zoomName = zoomVariant?.nombre?.takeIf { it.isNotBlank() }
        ?: zoomVariant?.color?.let { "${entry.nombre} $it" }
        ?: entry.nombre
    val zoomModel = rememberAficionesModel(zoomPath)
    if (zoomId != null && zoomModel != null) {
        CreatureZoomDialog(
            name = zoomName,
            model = zoomModel,
            category = entry.nombre,
            locations = emptyList(),
            level = entry.nivelSiembra,
            onDismiss = { zoomId = null },
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
        PixelCard(category = entry.nombre) {
            Text(entry.nombre, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = Ink)
            AficionCertifyRow(
                found = found,
                certified = certified,
                certifiable = certifiable,
                onToggleFound = onToggleFound,
                onToggleCertified = onToggleCertified,
            )
            entry.nivelSiembra?.let { LabelValue("Nivel de siembra", it.toString(), false) }
            entry.precioSemilla?.let { LabelValue("Precio de semilla", it.toString(), false) }
            entry.tiempoCrecimientoMinutos?.let {
                LabelValue("Tiempo de crecimiento", AficionesContent.formatGrowthMinutes(it), false)
            }
            entry.categoria?.let { LabelValue("Categoría", it, false) }
            Text("Variantes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            entry.variantes.forEach { v ->
                val titulo = v.nombre?.takeIf { it.isNotBlank() } ?: "${entry.nombre} ${v.color}"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AficionesImage(
                        imagen = v.imagen ?: entry.imagen,
                        sectionLabel = entry.nombre,
                        size = 56.dp,
                        contentDescription = ImageZoom.enlargedDescription(titulo),
                        onClick = { zoomId = v.id ?: v.imagen },
                    )
                    Column {
                        Text(titulo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text(
                            buildString {
                                append(AficionesContent.origenFlorLabel(v.origen))
                                v.estrellas?.let { append(" · ★$it") }
                                v.precioVenta?.let { append(" · Venta $it") }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}