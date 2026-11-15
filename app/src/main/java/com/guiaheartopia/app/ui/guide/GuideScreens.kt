package com.guiaheartopia.app.ui.guide

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.CatalogMarks
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.CreatureCopy
import com.guiaheartopia.app.domain.GuideSections
import com.guiaheartopia.app.domain.CatalogLabels
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyCatalog
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.CreatureAppearing
import com.guiaheartopia.app.domain.DexCategoryFilters
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.GuideLaunch
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.SkyWeather
import com.guiaheartopia.app.domain.dexEntryMatchesEvent
import com.guiaheartopia.app.domain.mergeGuideLaunch
import com.guiaheartopia.app.domain.TextMatch
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.CaptureNetIcon
import com.guiaheartopia.app.ui.components.CertificateMedalIcon
import com.guiaheartopia.app.ui.components.NonCertifiableIcon
import com.guiaheartopia.app.ui.components.ClearSkyIcon
import com.guiaheartopia.app.ui.components.CozyGuideIcon
import com.guiaheartopia.app.ui.components.CreatureGlyph
import com.guiaheartopia.app.ui.components.CreatureZoomDialog
import com.guiaheartopia.app.ui.components.rememberCreatureModel
import com.guiaheartopia.app.data.PhotoCredits
import com.guiaheartopia.app.ui.components.CreatureImage
import com.guiaheartopia.app.ui.settings.PhotoCreditCaption
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.SectionBanner
import com.guiaheartopia.app.ui.components.NowBadge
import com.guiaheartopia.app.ui.components.PillTabs
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Dark
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.HoneyTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Lilac
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Ochre
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.theme.Peach
import com.guiaheartopia.app.ui.theme.Purple
import com.guiaheartopia.app.ui.theme.PurpleTint
import com.guiaheartopia.app.ui.theme.Sky
import com.guiaheartopia.app.ui.theme.SkyBlue
import com.guiaheartopia.app.ui.theme.SkyTint
import java.time.Instant

private val Tabs = GuideSections.dexCategories.map { CatalogLabels.category(it) }

@Composable
fun GuideScreen(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
    onOpenDex: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, String) -> Unit,
    onAddAnimal: (String, String, String, String) -> Unit,
    onBack: (() -> Unit)? = null,
    initialTab: Int = 0,
    onlyAboveLevel: Boolean = false,
    guideLaunch: GuideLaunch = GuideLaunch(),
    onSaveDexFilters: (String, DexCategoryFilters) -> Unit = { _, _ -> },
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    androidx.compose.runtime.LaunchedEffect(initialTab, guideLaunch.eventFocus) {
        tab = initialTab
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        if (onBack != null) {
            androidx.compose.material3.TextButton(onClick = onBack) { Text("Atrás") }
        }
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_guia,
            title = "Naturaleza",
            subtitle = "Peces, aves, insectos y fauna",
        )

        // Pill Tabs with Icons
        PillTabs(Tabs, tab) { tab = it }

        DexTab(
            GuideSections.dexCategories[tab],
            ui,
            onOpenDex,
            onToggleFound,
            onToggleCertified,
            onlyAboveLevel,
            guideLaunch,
            onSaveDexFilters,
        )

        Spacer(Modifier.height(8.dp))
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

    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        placeholder = { Text("Buscar animal por nombre...") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = { Text("🔍", fontSize = 16.sp) },
        singleLine = true,
    )

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = onlySighted,
            onClick = { onlySighted = !onlySighted },
            label = { Text("🐾 Solo Registrados (${ui.sighted.size})") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GreenSoft,
                selectedLabelColor = GreenDark,
            ),
        )
    }

    if (shown.isEmpty()) {
        PixelCard(container = PaperSubtle) {
            Text("Ningún animal coincide con tu búsqueda.", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }

    shown.forEach { animal ->
        val sighted = animal.id in ui.sighted
        PixelCard(Modifier.clickable { onOpenAnimal(animal.id) }) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CreatureImage(
                    imageUrl = animal.imageUrl,
                    category = "Fauna",
                    locations = animal.feederLocations,
                    level = null,
                    modifier = Modifier.size(58.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(animal.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(animal.group, color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                if (sighted) {
                    VerifyBadge("Avistado ✓", tone = GreenSoft)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelValue("🍎 Comidas", animal.favoriteFoods.joinToString(", ").ifBlank { "Pendiente" })
                LabelValue(
                    "🪵 Comedero",
                    if (animal.locationVerified) animal.feederLocations.joinToString() else "Pendiente",
                    emphasize = false,
                )
                if (animal.likedWeather.isNotBlank()) {
                    LabelValue("🌤️ Clima favorable", animal.likedWeather, emphasize = false)
                }
            }

            PixelButton(
                text = if (sighted) "✓ Registrado" else "+ Registrar avistamiento",
                onClick = { onOpenAnimal(animal.id) },
                outlined = !sighted,
                container = Green,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // Add Animal Note Card
    PixelCard(container = PaperSubtle) {
        Text("Añadir Ficha Propia", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                placeholder = { Text("Nombre del animal...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            PixelButton(
                text = "Guardar",
                onClick = {
                    if (newName.isNotBlank()) {
                        onAddAnimal(newName.trim(), "Mis fichas", "", "")
                        newName = ""
                    }
                },
                outlined = true,
                container = Green,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DexTab(
    category: String,
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, String) -> Unit,
    onlyAboveLevel: Boolean = false,
    guideLaunch: GuideLaunch = GuideLaunch(),
    onSaveDexFilters: (String, DexCategoryFilters) -> Unit = { _, _ -> },
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val all = remember(revision) { com.guiaheartopia.app.data.HeartodexCatalog.load(context) }
    val sky = remember(revision) { SkyCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val instant = Instant.now()
    val current = DexSchedule.currentWindow(zone, instant)
    val eventsDoc = remember(revision) {
        val raw = com.guiaheartopia.app.data.AndroidContentLibrary(context)
            .effectiveBytes("calendar/eventos.json")
            ?.decodeToString()
        ReminderPlan.parseEvents(raw)
    }
    val activeSeasons = remember(eventsDoc, zone) {
        ReminderPlan.activeSeasons(eventsDoc, zone, java.time.ZonedDateTime.now(zone))
    }
    val stored = ui.dexFilters[category] ?: DexCategoryFilters()
    val filters = mergeGuideLaunch(stored, guideLaunch)
    val canCertify = CatalogMarks.certifiable(category)
    val hobbyLevel = CreatureAvailability.hobbyLevel(category, ui.hobbies)
    val catalogMax = remember(all, category) {
        LevelFilter.maximum(all.filter { it.category == category }.map { it.level })
    }
    val levelCap = if (catalogMax == null || filters.chosenLevel < 0) {
        null
    } else {
        filters.chosenLevel.coerceAtMost(catalogMax)
    }
    val categoryAll = remember(all, category) { all.filter { it.category == category } }
    val capturedCount = categoryAll.count { it.id in ui.found }

    fun eventBoost(entry: DexEntry): Boolean =
        activeSeasons.any { season -> dexEntryMatchesEvent(entry, season.id, season.name) }

    fun eventFocusOk(entry: DexEntry): Boolean {
        if (filters.eventFocus.isBlank()) return true
        val focusEvent = eventsDoc?.eventos.orEmpty().firstOrNull {
            it.id.equals(filters.eventFocus, true) || it.nombre.equals(filters.eventFocus, true)
        }
        val id = focusEvent?.id?.ifBlank { focusEvent.nombre }.orEmpty().ifBlank { filters.eventFocus }
        val name = focusEvent?.nombre.orEmpty().ifBlank { filters.eventFocus }
        return dexEntryMatchesEvent(entry, id, name)
    }

    val items = all.filter { entry ->
        val timeOk = SkyWeather.availableNow(entry.schedule, entry.weather, instant, sky, zone)
        entry.category == category &&
            eventFocusOk(entry) &&
            (filters.query.isBlank() || TextMatch.contains(entry.name, filters.query)) &&
            (filters.place.isBlank() || entry.location.any { TextMatch.contains(it, filters.place) }) &&
            (filters.weather == "Todos" || filters.weather in entry.weather) &&
            CreatureAvailability.levelAllowed(entry.level, levelCap) &&
            (!onlyAboveLevel || (entry.level != null && entry.level > (hobbyLevel ?: 0))) &&
            (!filters.onlyNow || CreatureAvailability.availableNow(category, entry.level, ui.hobbies, timeOk)) &&
            when (filters.registered) {
                "si" -> entry.id in ui.found
                "no" -> entry.id !in ui.found
                else -> true
            } &&
            when {
                !canCertify || filters.certified == "todos" -> true
                filters.certified == "si" -> entry.id in ui.certified
                else -> entry.id !in ui.certified
            }
    }.sortedWith(
        compareByDescending<DexEntry> { eventBoost(it) }
            .thenBy { if (filters.sortByLevel) it.level ?: Int.MAX_VALUE else 0 }
            .thenBy { it.name.lowercase() },
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "$capturedCount/${categoryAll.size} capturadas",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Ink,
            )
            Text(
                "Franja actual: ${DexSchedule.titled(current.slot)}",
                style = MaterialTheme.typography.bodySmall,
                color = GreenDark,
            )
        }

        OutlinedTextField(
            value = filters.query,
            onValueChange = { onSaveDexFilters(category, stored.copy(query = it)) },
            placeholder = { Text("Buscar por nombre en español...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Text("🔍", fontSize = 15.sp) },
            singleLine = true,
        )

        OutlinedTextField(
            value = filters.place,
            onValueChange = { onSaveDexFilters(category, stored.copy(place = it)) },
            placeholder = { Text("Filtrar por ubicación...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Text("📍", fontSize = 15.sp) },
            singleLine = true,
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val weatherChips = listOf(
                "Todos" to "Todos",
                "Sunny" to "☀️ Despejado",
                "Rainy" to "🌧️ Lluvia",
                "Rainbow" to "🌈 Arcoíris",
            )
            weatherChips.forEach { (key, label) ->
                FilterChip(
                    selected = filters.weather == key,
                    onClick = { onSaveDexFilters(category, stored.copy(weather = key)) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HoneySoft,
                        selectedLabelColor = Ink,
                    ),
                )
            }

            FilterChip(
                selected = filters.sortByLevel,
                onClick = { onSaveDexFilters(category, stored.copy(sortByLevel = !filters.sortByLevel)) },
                label = { Text(if (filters.sortByLevel) "Orden: nivel" else "Orden: nombre") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HoneySoft,
                    selectedLabelColor = Ink,
                ),
            )

            FilterChip(
                selected = filters.onlyNow,
                onClick = { onSaveDexFilters(category, stored.copy(onlyNow = !filters.onlyNow)) },
                label = { Text("Disponible ahora") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )

            FilterChip(
                selected = filters.registered == "si",
                onClick = {
                    val next = if (filters.registered == "si") "todos" else "si"
                    onSaveDexFilters(category, stored.copy(registered = next))
                },
                label = { Text("✓ Registrados") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )

            FilterChip(
                selected = filters.registered == "no",
                onClick = {
                    val next = if (filters.registered == "no") "todos" else "no"
                    onSaveDexFilters(category, stored.copy(registered = next))
                },
                label = { Text("○ Sin registrar") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PaperSubtle,
                    selectedLabelColor = Ink,
                ),
            )

            if (canCertify) {
                FilterChip(
                    selected = filters.certified == "si",
                    onClick = {
                        val next = if (filters.certified == "si") "todos" else "si"
                        onSaveDexFilters(category, stored.copy(certified = next))
                    },
                    label = { Text("Certificados") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HoneySoft,
                        selectedLabelColor = Ink,
                    ),
                )
                FilterChip(
                    selected = filters.certified == "no",
                    onClick = {
                        val next = if (filters.certified == "no") "todos" else "no"
                        onSaveDexFilters(category, stored.copy(certified = next))
                    },
                    label = { Text("No certificados") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PaperSubtle,
                        selectedLabelColor = Ink,
                    ),
                )
            }
        }

        if (items.isNotEmpty()) {
            Text(
                "${items.size} en esta lista",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Muted,
            )
        }

        // Guía / Leyenda visual de horarios y climas
        val weatherKeys = remember(all, category) {
            val present = all.filter { it.category == category }.flatMap { it.weather }.distinct()
            if (present.isEmpty()) listOf("Sunny", "Rainy", "Rainbow") else present
        }
        ScheduleWeatherLegend(weatherKeys)
        if (catalogMax != null) {
            LevelDropdown(
                selected = levelCap,
                maximum = catalogMax,
                onSelect = { level -> onSaveDexFilters(category, stored.copy(chosenLevel = level ?: -1)) },
            )
        }
    }

    if (items.isEmpty()) {
        PixelCard(container = PaperSubtle) {
            Text("No se encontraron criaturas con estos filtros.", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }

    items.forEach { entry ->
        val now = SkyWeather.availableNow(entry.schedule, entry.weather, instant, sky, zone)
        DexCreatureCard(
            entry = entry,
            now = now,
            found = entry.id in ui.found,
            certified = entry.id in ui.certified,
            certifiable = CatalogMarks.certifiable(entry),
            onOpen = { onOpen(entry.id) },
            onToggleFound = { onToggleFound(entry.id) },
            onToggleCertified = { onToggleCertified(entry.id, entry.category) },
        )
    }
}

@Composable
fun DexDetailScreen(
    entry: DexEntry,
    zoneId: String,
    found: Boolean,
    certified: Boolean = false,
    onToggle: () -> Unit,
    onToggleCertified: () -> Unit = {},
    onBack: () -> Unit,
    windowAlarmScheduled: Boolean = false,
    nextWindowLabel: String? = null,
    onScheduleWindow: () -> Unit = {},
    onCancelWindow: () -> Unit = {},
) {
    val zone = DexSchedule.zoneOf(zoneId)
    val instant = Instant.now()
    val detailContext = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val sky = remember(revision) { SkyCatalog.load(detailContext) }
    val now = SkyWeather.availableNow(
        entry.schedule,
        entry.weather,
        instant,
        sky,
        zone,
    )
    val localHours = DexSchedule.windowsAround(zone, instant)
        .filter { it.slot.key in entry.schedule }
        .joinToString(" · ") { "${DexSchedule.titled(it.slot)} ${DexSchedule.hoursLabel(it)}" }
    val photoCredit = remember(revision, entry.id) {
        PhotoCredits.byId(detailContext)[entry.id]
    }
    val imageModel = rememberCreatureModel(entry.imageUrl, entry.id)
    var zoom by remember { mutableStateOf(false) }

    if (zoom) {
        CreatureZoomDialog(
            name = entry.name,
            model = imageModel,
            category = entry.category,
            locations = entry.location,
            level = entry.level,
            onDismiss = { zoom = false },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PixelCard(container = PaperSubtle, category = entry.category) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CreatureImage(
                        imageUrl = entry.imageUrl,
                        category = entry.category,
                        locations = entry.location,
                        level = entry.level,
                        creatureId = entry.id,
                        contentDescription = ImageZoom.enlargedDescription(entry.name),
                        modifier = Modifier.size(116.dp),
                        shapeRadius = 20.dp,
                        onClick = { zoom = true },
                    )
                    photoCredit?.let { credit ->
                        PhotoCreditCaption(
                            credit,
                            modifier = Modifier.padding(top = 4.dp).width(92.dp),
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(entry.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Text(entry.nameEn.ifBlank { entry.category }, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    val blurb = CreatureCopy.visible(entry.description)
                    if (blurb.isNotEmpty()) {
                        Text(
                            blurb,
                            color = Ink,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (now) NowBadge()
            }
        }

        // Details Card
        PixelCard(container = Paper) {
            Text("Detalles de Aparición", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("📍 Ubicación:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Text(
                    text = entry.location.joinToString().ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = GreenDark,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Horario:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                ScheduleIcons(entry.schedule)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Clima:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                WeatherIcons(entry.weather)
            }

            LabelValue("🌐 En tu hora local", localHours.ifBlank { "—" }, false)
            nextWindowLabel?.let { LabelValue("⏭ Próxima ventana", it, false) }
            if (entry.level != null) LabelValue("⭐ Nivel", entry.level.toString(), false)
            if (!entry.shadow.isNullOrBlank()) LabelValue("👥 Sombra", entry.shadow, false)
            if (!entry.event.isNullOrBlank()) LabelValue("🎉 Evento", entry.event, false)
        }

        if (nextWindowLabel != null) {
            PixelButton(
                text = if (windowAlarmScheduled) "Cancelar aviso de franja" else "Avísame en la próxima franja",
                onClick = if (windowAlarmScheduled) onCancelWindow else onScheduleWindow,
                outlined = windowAlarmScheduled,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MarkIconButton(
                on = found,
                description = if (found) "Quitar capturado" else "Marcar como capturado",
                onClick = onToggle,
            ) { CaptureNetIcon(on = found) }
            if (CatalogMarks.certifiable(entry)) {
                MarkIconButton(
                    on = certified,
                    description = if (certified) "Quitar certificado" else "Marcar como certificado",
                    onClick = onToggleCertified,
                ) { CertificateMedalIcon(on = certified) }
            } else if (CatalogMarks.certifiable(entry.category)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Paper)
                        .border(1.dp, Line, CircleShape)
                        .clearAndSetSemantics { contentDescription = "No certificable" },
                    contentAlignment = Alignment.Center,
                ) {
                    NonCertifiableIcon(size = 26.dp)
                }
            }
        }

        PixelButton(
            text = "← Volver al catálogo",
            onClick = onBack,
            outlined = true,
            modifier = Modifier.fillMaxWidth(),
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero Card
        PixelCard(container = PaperSubtle) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CreatureImage(
                    imageUrl = animal.imageUrl,
                    category = "Fauna",
                    locations = animal.feederLocations,
                    level = null,
                    modifier = Modifier.size(76.dp),
                    shapeRadius = 18.dp,
                )
                Column(Modifier.weight(1f)) {
                    Text(animal.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                    Text(animal.group, color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                if (sighted) VerifyBadge("Avistado ✓", tone = GreenSoft)
            }
            if (animal.summary.isNotBlank()) {
                Text(animal.summary, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }

        // Guide verified info
        PixelCard(container = Paper) {
            Text("Ficha", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            LabelValue("🍎 Comidas favoritas", animal.favoriteFoods.joinToString(", ").ifBlank { "—" })
            LabelValue("🪵 Comedero", animal.feederLocations.joinToString().ifBlank { "—" }, emphasize = false)
            if (animal.likedWeather.isNotBlank()) LabelValue("🌤️ Clima favorable", animal.likedWeather, emphasize = false)
        }

        // User personal annotations
        PixelCard(container = Paper) {
            Text("Tus Notas de Campo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            OutlinedTextField(
                value = foods,
                onValueChange = { foods = it },
                label = { Text("Comidas que tú viste") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = feeders,
                onValueChange = { feeders = it },
                label = { Text("Dónde viste el comedero") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            PixelButton(
                text = "Guardar mis notas",
                onClick = {
                    onSave(
                        AnimalAnnotation(
                            foods = foods.lines().map { it.trim() }.filter { it.isNotEmpty() },
                            feeders = feeders.lines().map { it.trim() }.filter { it.isNotEmpty() },
                            note = note.trim(),
                        ),
                    )
                },
                outlined = true,
                container = Green,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PixelButton(
            text = if (sighted) "✓ Registrado como avistado" else "+ Registrar avistamiento",
            onClick = onToggleSighted,
            outlined = !sighted,
            container = Green,
            modifier = Modifier.fillMaxWidth(),
        )

        PixelButton(
            text = "← Volver",
            onClick = onBack,
            outlined = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ---------------------------------------------------------
// Componentes visuales para Horarios, Clima y Leyenda
// ---------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleWeatherLegend(weatherKeys: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PaperSubtle)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("💡", fontSize = 12.sp)
            Text(
                "Guía de símbolos:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Ink,
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LegendItem("🌅", "Amanecer")
            LegendItem("☀️", "Día")
            LegendItem("🌤️", "Tarde")
            LegendItem("🌆", "Noche")
            weatherKeys.distinct().forEach { key ->
                LegendWeather(key)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LevelDropdown(
    selected: Int?,
    maximum: Int,
    onSelect: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = if (selected == null) "Todos" else selected.toString()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Nivel") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            LevelFilter.options(maximum).forEach { level ->
                DropdownMenuItem(
                    text = { Text(if (level == null) "Todos" else level.toString()) },
                    onClick = {
                        onSelect(level)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DexCreatureCard(
    entry: DexEntry,
    now: Boolean,
    found: Boolean,
    certified: Boolean,
    certifiable: Boolean,
    onOpen: () -> Unit,
    onToggleFound: () -> Unit,
    onToggleCertified: () -> Unit,
) {
    val model = rememberCreatureModel(entry.imageUrl, entry.id)
    var zoom by remember { mutableStateOf(false) }
    if (zoom) {
        CreatureZoomDialog(
            name = entry.name,
            model = model,
            category = entry.category,
            locations = entry.location,
            level = entry.level,
            onDismiss = { zoom = false },
        )
    }
    PixelCard(Modifier.clickable { onOpen() }, category = entry.category) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CreatureImage(
                imageUrl = entry.imageUrl,
                category = entry.category,
                locations = entry.location,
                level = entry.level,
                creatureId = entry.id,
                contentDescription = ImageZoom.enlargedDescription(entry.name),
                modifier = Modifier.size(80.dp),
                onClick = { zoom = true },
            )
            Column(Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                    softWrap = true,
                )
                Text(
                    entry.event ?: CatalogLabels.category(entry.category),
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall,
                    softWrap = true,
                )
            }
            if (now) NowBadge()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Ubicación: ${entry.location.joinToString().ifBlank { "—" }}",
                style = MaterialTheme.typography.bodySmall,
                color = Ink,
                softWrap = true,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Horario:", style = MaterialTheme.typography.bodySmall, color = Ink)
                ScheduleIcons(entry.schedule)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Clima:", style = MaterialTheme.typography.bodySmall, color = Ink)
                WeatherIcons(entry.weather)
            }
            if (entry.level != null) {
                Text(
                    "Nivel requerido: ${entry.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MarkIconButton(
                on = found,
                description = if (found) "Quitar capturado" else "Marcar como capturado",
                onClick = onToggleFound,
            ) { CaptureNetIcon(on = found) }
            if (certifiable) {
                MarkIconButton(
                    on = certified,
                    description = if (certified) "Quitar certificado" else "Marcar como certificado",
                    onClick = onToggleCertified,
                ) { CertificateMedalIcon(on = certified) }
            } else if (CatalogMarks.certifiable(entry.category)) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Paper)
                        .border(1.dp, Line, CircleShape)
                        .clearAndSetSemantics { contentDescription = "No certificable" },
                    contentAlignment = Alignment.Center,
                ) {
                    NonCertifiableIcon(size = 26.dp)
                }
            }
        }
    }
}

@Composable
private fun MarkIconButton(
    on: Boolean,
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (on) HoneySoft else Paper)
            .border(width = if (on) 2.dp else 1.dp, color = if (on) Ochre else Line, shape = CircleShape)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Composable
private fun CertifiedSeal() {
    Canvas(
        Modifier
            .size(16.dp)
            .semantics { contentDescription = "Certificado" },
    ) {
        val radius = size.minDimension / 2f
        drawCircle(color = Ochre, radius = radius)
        drawCircle(
            color = Color(0xFF8C6A2F),
            radius = radius * 0.72f,
            style = Stroke(width = radius * 0.14f),
        )
        val arm = radius * 0.34f
        val stroke = radius * 0.16f
        drawLine(Paper, center + Offset(0f, -arm), center + Offset(arm, 0f), stroke, StrokeCap.Round)
        drawLine(Paper, center + Offset(arm, 0f), center + Offset(0f, arm), stroke, StrokeCap.Round)
        drawLine(Paper, center + Offset(0f, arm), center + Offset(-arm, 0f), stroke, StrokeCap.Round)
        drawLine(Paper, center + Offset(-arm, 0f), center + Offset(0f, -arm), stroke, StrokeCap.Round)
    }
}

@Composable
private fun LegendItem(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(icon, fontSize = 12.sp)
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

private val ScheduleGlyphs = listOf(
    "Dawn" to "🌅",
    "Day" to "☀️",
    "Dusk" to "🌤️",
    "Night" to "🌆",
)

@Composable
private fun ScheduleIcons(schedule: List<String>) {
    val active = ScheduleGlyphs.filter { (key, _) -> key in schedule }
    if (active.isEmpty()) {
        Text("—", style = MaterialTheme.typography.bodySmall, color = Muted)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        active.forEach { (key, icon) ->
            val label = DexSchedule.scheduleEs(key)
            Box(
                Modifier
                    .size(24.dp)
                    .clearAndSetSemantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Text(icon, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun WeatherIcons(weather: List<String>) {
    if (weather.isEmpty()) {
        Text("—", style = MaterialTheme.typography.bodySmall, color = Muted)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        weather.distinct().forEach { key ->
            WeatherGlyph(key)
        }
    }
}

@Composable
private fun WeatherGlyph(key: String) {
    val label = DexSchedule.weatherEs(key)
    Box(
        Modifier
            .size(24.dp)
            .clearAndSetSemantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        when (key) {
            "Sunny" -> ClearSkyIcon(size = 18.dp)
            "Rainy" -> Text("🌧️", fontSize = 16.sp)
            "Rainbow" -> Text("🌈", fontSize = 16.sp)
            else -> Canvas(Modifier.size(16.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height / 2f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height / 2f)
                    close()
                }
                drawPath(path, Ochre)
            }
        }
    }
}

@Composable
private fun LegendWeather(key: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        WeatherGlyph(key)
        Text(
            DexSchedule.weatherEs(key),
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
        )
    }
}

