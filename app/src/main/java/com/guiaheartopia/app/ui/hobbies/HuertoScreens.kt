package com.guiaheartopia.app.ui.hobbies

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.SeasonTags
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.domain.TextMatch
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.ui.components.AficionesImage
import com.guiaheartopia.app.ui.components.AlarmAdStatusChip
import com.guiaheartopia.app.ui.components.ComingSoonCard
import com.guiaheartopia.app.ui.components.CreatureZoomDialog
import com.guiaheartopia.app.ui.components.EmptyStateCard
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.remember5SecondTick
import com.guiaheartopia.app.ui.components.rememberAficionesModel
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.contentRevision
import com.guiaheartopia.app.ui.guide.GuideSearchField
import com.guiaheartopia.app.ui.theme.AlertRed
import com.guiaheartopia.app.ui.theme.AlertSoft
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

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
    onPlant: (String, String, Int) -> Unit,
    cropAlarmUnlocked: Boolean = false,
    cropAlarmUntilMillis: Long = 0L,
    onWatchAlarmAd: () -> Unit = {},
    initialTab: Int = 0,
) {
    val context = LocalContext.current
    val revision = contentRevision()
    val bundle = remember(revision) { AficionesCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val eventsDoc = remember(revision) {
        val raw = AndroidContentLibrary(context).effectiveBytes("calendar/eventos.json")?.decodeToString()
        ReminderPlan.parseEvents(raw)
    }
    val activeSeasonKeys = remember(eventsDoc, zone) {
        SeasonTags.activeKeys(eventsDoc, zone, ZonedDateTime.now(zone))
    }
    var showPastSeasons by rememberSaveable { mutableStateOf(false) }
    val visibleSiembras = remember(bundle.siembras, activeSeasonKeys, showPastSeasons) {
        bundle.siembras.filter { SeasonTags.isPast(it.evento, activeSeasonKeys) == showPastSeasons }
    }
    // Las flores no traen etiqueta de temporada en el catálogo publicado.
    val visibleFlores = if (showPastSeasons) emptyList() else bundle.flores
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    if (!bundle.huertoReady) {
        ProntoScreen("Huerto", "Siembras y flores", onBack)
        return
    }

    // Búsqueda por nombre (misma experiencia que bichos/peces)
    var query by rememberSaveable { mutableStateOf("") }

    // Estado de la lista de siembras: permite saltar a una siembra desde la cápsula
    val siembrasListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val onCropClick: (String) -> Unit = { siembraId ->
        tab = 0 // asegurar pestaña Siembras
        scope.launch {
            // El item 0 es el buscador, así que sumamos 1 al índice de la siembra
            val index = visibleSiembras
                .filter { TextMatch.contains(it.nombre, query) }
                .sortedBy { it.nombre }
                .indexOfFirst { it.id == siembraId }
            if (index >= 0) siembrasListState.animateScrollToItem(index + 1)
        }
    }

    // Mismo comportamiento que el chip «Sin ads»: se pliega al bajar y vuelve
    // a mostrarse solo al llegar arriba.
    var alarmVisible by remember { mutableStateOf(true) }
    val alarmScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && consumed.y == 0f) {
                    // Lista en el tope superior: mostrar
                    alarmVisible = true
                } else if (consumed.y < 0f) {
                    // Bajando: ocultar
                    alarmVisible = false
                }
                return Offset.Zero
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .nestedScroll(alarmScroll),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_huerto,
            title = "Huerto",
            subtitle = "Siembras y flores",
            compact = true,
            onBack = onBack,
            imageOffsetY = 96.dp,
        )
        AnimatedVisibility(
            visible = alarmVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AlarmAdStatusChip(
                    untilMillis = cropAlarmUntilMillis,
                    onWatchAlarmAd = onWatchAlarmAd,
                )
                if (ui.activeCrops.isNotEmpty()) {
                    ActiveCropsPanel(
                        crops = ui.activeCrops,
                        siembras = bundle.siembras,
                        onCropClick = onCropClick,
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PillTabs(HuertoTabs, tab, fillWidth = false, modifier = Modifier.weight(1f)) { tab = it }
            FilterChip(
                selected = showPastSeasons,
                onClick = { showPastSeasons = !showPastSeasons },
                label = { Text("Temporadas pasadas", maxLines = 1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )
        }
        when (tab) {
            0 -> SiembrasTab(
                siembras = visibleSiembras,
                found = ui.huertoFound,
                certified = ui.huertoCertified,
                activeCrops = ui.activeCrops,
                listState = siembrasListState,
                query = query,
                onQueryChange = { query = it },
                onOpen = onOpenSiembra,
                onPlant = onPlant,
                onCancelCrop = onCancelCrop,
                onToggleFound = onToggleSiembraFound,
                onToggleCertified = onToggleSiembraCertified,
                cropAlarmUnlocked = cropAlarmUnlocked,
                onAlarmLocked = {
                    Toast.makeText(
                        context,
                        "Debes ver un vídeo para activar las alarmas de siembra",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                pastOnly = showPastSeasons,
                modifier = Modifier.weight(1f),
            )
            else -> FloresTab(
                flores = visibleFlores,
                found = ui.florFound,
                certified = ui.florCertified,
                query = query,
                onQueryChange = { query = it },
                onOpen = onOpenFlor,
                onToggleFound = onToggleFlorFound,
                onToggleCertified = onToggleFlorCertified,
                pastOnly = showPastSeasons,
                modifier = Modifier.weight(1f),
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
        ComingSoonCard(title = title, subtitle = subtitle)
    }
}

/**
 * Píldora verde idéntica al chip «Sin ads»: una mini figura de cada cultivo y
 * su tiempo restante, sin palabras. Se pliega al bajar por la pantalla.
 */
@Composable
private fun ActiveCropsPanel(
    crops: List<ActiveCrop>,
    siembras: List<SiembraEntry>,
    onCropClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = remember5SecondTick().toEpochMilli()
    val active = crops.filter { it.readyAtEpochMilli > now }
    if (active.isEmpty()) return
    val siembraById = remember(siembras) { siembras.associateBy { it.id } }
    val shape = RoundedCornerShape(999.dp)
    FlowRow(
        modifier = modifier
            .clip(shape)
            .background(GreenSoft)
            .border(1.dp, GreenTint, shape)
            .padding(start = 9.dp, end = 9.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        active.forEach { crop ->
            val remaining = (crop.readyAtEpochMilli - now).coerceAtLeast(0)
            val label = AficionesContent.formatGrowthMinutes((remaining / 60_000).toInt())
            val siembra = siembraById[crop.siembraId]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Mini figura del cultivo: al tocarla hace scroll hasta la siembra (sin abrir detalle)
                if (siembra != null) {
                    AficionesImage(
                        imagen = siembra.imagen,
                        sectionLabel = "Siembra",
                        size = 22.dp,
                        shapeRadius = 6.dp,
                        contentDescription = "Ir a ${crop.nombre} en la lista",
                        onClick = { onCropClick(crop.siembraId) },
                    )
                }
                // Tiempo restante, en verde y sin palabras
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenDark,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SiembrasTab(
    siembras: List<SiembraEntry>,
    found: Set<String>,
    certified: Set<String>,
    activeCrops: List<ActiveCrop>,
    listState: LazyListState,
    query: String,
    onQueryChange: (String) -> Unit,
    onOpen: (String) -> Unit,
    onPlant: (String, String, Int) -> Unit,
    onCancelCrop: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, Boolean) -> Unit,
    cropAlarmUnlocked: Boolean,
    onAlarmLocked: () -> Unit,
    pastOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val nameSuggestions = remember(siembras) { siembras.map { it.nombre }.distinct().sorted() }
    val sorted = siembras
        .filter { TextMatch.contains(it.nombre, query) }
        .sortedBy { it.nombre }
    val cropBySiembra = remember(activeCrops) { activeCrops.associateBy { it.siembraId } }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Buscador como item: se desplaza con la lista y se oculta al bajar (como bichos)
        item(key = "buscador") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GuideSearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Buscar siembra...",
                    leadingIcon = "🔍",
                    draftKey = "huerto:siembras:name",
                    shape = RoundedCornerShape(14.dp),
                    dense = true,
                    suggestions = nameSuggestions,
                )
            }
        }
        if (sorted.isEmpty()) {
            item(key = "vacio") {
                EmptyStateCard(
                    title = when {
                        pastOnly -> "Sin temporadas pasadas"
                        siembras.isEmpty() -> "Sin datos todavía"
                        else -> "Sin coincidencias"
                    },
                    message = when {
                        pastOnly -> "No hay siembras de temporadas ya finalizadas."
                        siembras.isEmpty() -> "Las siembras aparecerán cuando se publique el catálogo."
                        else -> "Ninguna siembra coincide con la búsqueda."
                    },
                    icon = if (pastOnly) "🗓️" else if (siembras.isEmpty()) "🌱" else "🔍",
                )
            }
        }
        items(sorted, key = { it.id }) { entry ->
            SiembraCard(
                entry,
                found = entry.id in found,
                certified = entry.id in certified,
                activeCrop = cropBySiembra[entry.id],
                alarmUnlocked = cropAlarmUnlocked,
                onOpen = { onOpen(entry.id) },
                onToggleAlarm = {
                    val crop = cropBySiembra[entry.id]
                    if (crop != null) {
                        onCancelCrop(crop.id)
                    } else if (cropAlarmUnlocked) {
                        onPlant(entry.id, entry.nombre, entry.tiempoCrecimientoMinutos)
                    } else {
                        onAlarmLocked()
                    }
                },
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
    query: String,
    onQueryChange: (String) -> Unit,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, Boolean) -> Unit,
    pastOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val nameSuggestions = remember(flores) { flores.map { it.nombre }.distinct().sorted() }
    val sorted = flores
        .filter { TextMatch.contains(it.nombre, query) }
        .sortedBy { it.nombre }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        // Buscador como item: se desplaza con la lista y se oculta al bajar (como bichos)
        item(key = "buscador") {
            GuideSearchField(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = "Buscar flor...",
                leadingIcon = "🔍",
                draftKey = "huerto:flores:name",
                shape = RoundedCornerShape(14.dp),
                dense = true,
                suggestions = nameSuggestions,
            )
        }
        if (sorted.isEmpty()) {
            item(key = "vacio") {
                EmptyStateCard(
                    title = when {
                        pastOnly -> "Sin temporadas pasadas"
                        flores.isEmpty() -> "Sin datos todavía"
                        else -> "Sin coincidencias"
                    },
                    message = when {
                        pastOnly -> "Las flores no tienen temporada asignada en el catálogo."
                        flores.isEmpty() -> "Las flores aparecerán cuando se publique el catálogo."
                        else -> "Ninguna flor coincide con la búsqueda."
                    },
                    icon = if (pastOnly) "🗓️" else if (flores.isEmpty()) "🌱" else "🔍",
                )
            }
        }
        items(sorted, key = { it.id }) { entry ->
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
    activeCrop: ActiveCrop?,
    alarmUnlocked: Boolean,
    onOpen: () -> Unit,
    onToggleAlarm: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val certifiable = AficionesMarks.certifiableSiembra(entry)
    var zoom by remember(entry.id) { mutableStateOf(false) }
    var alarmFlash by remember(entry.id) { mutableStateOf(false) }
    LaunchedEffect(alarmFlash) {
        if (alarmFlash) {
            delay(1_500L)
            alarmFlash = false
        }
    }
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 70 % del ancho para la ficha (imagen + texto), 30 % para los botones.
            Row(
                Modifier.weight(0.7f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AficionesImage(
                    imagen = entry.imagen,
                    sectionLabel = "Siembra",
                    size = 64.dp,
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
            }
            // Los botones se apilan en esa columna estrecha: alarma, conseguido y certificado.
            Column(
                Modifier.weight(0.3f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (entry.tiempoCrecimientoMinutos > 0) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (alarmFlash) AlertSoft else if (activeCrop != null) GreenSoft else Paper)
                            .border(
                                1.dp,
                                if (alarmFlash) AlertRed else if (activeCrop != null) GreenTint else Line,
                                RoundedCornerShape(999.dp),
                            )
                            .clickable {
                                if (activeCrop == null && !alarmUnlocked) alarmFlash = true
                                onToggleAlarm()
                            }
                            .semantics {
                                contentDescription = if (activeCrop != null) {
                                    "Quitar alarma de ${entry.nombre}"
                                } else {
                                    "Poner alarma de ${entry.nombre}"
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(
                                if (activeCrop != null) com.guiaheartopia.app.R.drawable.ic_clock_alarm_off
                                else com.guiaheartopia.app.R.drawable.ic_clock_alarm,
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                AficionCertifyRow(
                    found = found,
                    certified = certified,
                    certifiable = certifiable,
                    onToggleFound = onToggleFound,
                    onToggleCertified = onToggleCertified,
                    compact = true,
                    vertical = true,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mismo reparto 70/30 que en las siembras.
            Row(
                Modifier.weight(0.7f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AficionesImage(
                    imagen = entry.imagen,
                    sectionLabel = entry.nombre,
                    size = 64.dp,
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
            }
            Column(Modifier.weight(0.3f), horizontalAlignment = Alignment.End) {
                AficionCertifyRow(
                    found = found,
                    certified = certified,
                    certifiable = certifiable,
                    onToggleFound = onToggleFound,
                    onToggleCertified = onToggleCertified,
                    compact = true,
                    vertical = true,
                )
            }
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
    alarmUnlocked: Boolean = false,
    onWatchAlarmAd: () -> Unit = {},
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
                        text = if (alarmUnlocked) "Avísame cuando esté lista" else "Ver vídeo · activar alarma 3h",
                        onClick = if (alarmUnlocked) onPlant else onWatchAlarmAd,
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
    var zoomId by remember(entry.id) { mutableStateOf<String?>(null) }
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