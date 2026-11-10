package com.guiaheartopia.app.ui.guide

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.CreatureCopy
import com.guiaheartopia.app.domain.GuideSections
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.SkyWeather
import com.guiaheartopia.app.domain.TextMatch
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.CozyGuideIcon
import com.guiaheartopia.app.ui.components.CreatureGlyph
import com.guiaheartopia.app.data.PhotoCredits
import com.guiaheartopia.app.ui.components.CreatureImage
import com.guiaheartopia.app.ui.settings.PhotoCreditCaption
import com.guiaheartopia.app.ui.components.LabelValue
import com.guiaheartopia.app.ui.components.NotebookMark
import com.guiaheartopia.app.ui.components.NotebookTitle
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

private val Tabs = GuideSections.dexCategories.map { section ->
    when (section) {
        "Peces" -> "Peces"
        "Pájaros" -> "Aves"
        "Insectos" -> "Insectos"
        else -> "Fauna"
    }
}

@Composable
fun GuideScreen(
    ui: GuideUi,
    onOpenAnimal: (String) -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                NotebookTitle(
                    when (GuideSections.dexCategories[tab]) {
                        "Peces" -> "Peces"
                        "Pájaros" -> "Aves"
                        "Insectos" -> "Insectos"
                        else -> "Fauna"
                    },
                    when (GuideSections.dexCategories[tab]) {
                        "Peces" -> NotebookMark.FISH
                        "Pájaros" -> NotebookMark.BIRD
                        "Insectos" -> NotebookMark.INSECT
                        else -> NotebookMark.FAUNA
                    },
                )
                Text(
                    "Catálogo y animales silvestres",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                CozyGuideIcon(tint = GreenDark, size = 22.dp, filled = true)
            }
        }

        // Pill Tabs with Icons
        PillTabs(Tabs, tab) { tab = it }

        DexTab(GuideSections.dexCategories[tab], ui, onOpenDex, onToggleFound)

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
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val all = remember(revision) { com.guiaheartopia.app.data.HeartodexCatalog.load(context) }
    val sky = remember(revision) { SkyCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val instant = Instant.now()
    val current = DexSchedule.currentWindow(zone, instant)

    var query by rememberSaveable(category) { mutableStateOf("") }
    var place by rememberSaveable(category) { mutableStateOf("") }
    var onlyNow by rememberSaveable(category) { mutableStateOf(false) }
    var registered by rememberSaveable(category) { mutableStateOf("todos") }
    var weather by rememberSaveable(category) { mutableStateOf("Todos") }
    var allLevels by rememberSaveable(category) { mutableStateOf(false) }
    var chosenLevel by rememberSaveable(category) { mutableStateOf(-1) }
    val hobbyLevel = CreatureAvailability.hobbyLevel(category, ui.hobbies)
    val levelCap = when {
        hobbyLevel == null || allLevels -> null
        chosenLevel >= 0 -> chosenLevel
        else -> hobbyLevel
    }

    val items = all.filter { entry ->
        val timeOk = SkyWeather.availableNow(entry.schedule, entry.weather, instant, sky, zone)
        entry.category == category &&
            (query.isBlank() || TextMatch.contains(entry.name, query) || TextMatch.contains(entry.nameEn, query)) &&
            (place.isBlank() || entry.location.any { TextMatch.contains(it, place) }) &&
            (weather == "Todos" || weather in entry.weather) &&
            CreatureAvailability.levelAllowed(entry.level, levelCap) &&
            (!onlyNow || CreatureAvailability.availableNow(category, entry.level, ui.hobbies, timeOk)) &&
            when (registered) {
                "si" -> entry.id in ui.found
                "no" -> entry.id !in ui.found
                else -> true
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Buscar en $category...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Text("🔍", fontSize = 15.sp) },
            singleLine = true,
        )

        OutlinedTextField(
            value = place,
            onValueChange = { place = it },
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
                    selected = weather == key,
                    onClick = { weather = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HoneySoft,
                        selectedLabelColor = Ink,
                    ),
                )
            }

            if (hobbyLevel != null) {
                FilterChip(
                    selected = allLevels,
                    onClick = { allLevels = !allLevels },
                    label = { Text("Todos los niveles") },
                )
            }
            FilterChip(
                selected = onlyNow,
                onClick = { onlyNow = !onlyNow },
                label = { Text("Disponible ahora") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )

            FilterChip(
                selected = registered == "si",
                onClick = { registered = if (registered == "si") "todos" else "si" },
                label = { Text("✓ Registrados") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )

            FilterChip(
                selected = registered == "no",
                onClick = { registered = if (registered == "no") "todos" else "no" },
                label = { Text("○ Sin registrar") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PaperSubtle,
                    selectedLabelColor = Ink,
                ),
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${items.size} criaturas encontradas",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Muted,
            )
            Text(
                "Franja actual: ${DexSchedule.titled(current.slot)}",
                style = MaterialTheme.typography.bodySmall,
                color = GreenDark,
            )
        }

        // Guía / Leyenda visual de horarios y climas
        ScheduleWeatherLegend()
        if (hobbyLevel != null && !allLevels) {
            Text(
                "Nivel máximo: ${levelCap ?: hobbyLevel}. Se ven las criaturas con nivel requerido igual o menor.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0..10).forEach { level ->
                    FilterChip(
                        selected = (levelCap ?: hobbyLevel) == level,
                        onClick = { chosenLevel = level },
                        label = { Text(level.toString()) },
                    )
                }
            }
        }
    }

    if (items.isEmpty()) {
        PixelCard(container = PaperSubtle) {
            Text("No se encontraron criaturas con estos filtros.", style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
    }

    items.forEach { entry ->
        val isFound = entry.id in ui.found
        val now = SkyWeather.availableNow(entry.schedule, entry.weather, instant, sky, zone)

        PixelCard(Modifier.clickable { onOpen(entry.id) }) {
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
                    contentDescription = "Foto o dibujo de ${entry.name}",
                    modifier = Modifier.size(58.dp),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        entry.event ?: entry.category,
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (now) NowBadge()
            }

            // Info visual clara y alineada
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
                Text(
                    "Horario: ${entry.schedule.joinToString { DexSchedule.scheduleLabel(it) }.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    softWrap = true,
                )
                Text(
                    "Clima: ${entry.weather.joinToString { DexSchedule.weatherEs(it) }.ifBlank { "—" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    maxLines = Int.MAX_VALUE,
                )
                if (entry.level != null) {
                    Text(
                        "Nivel requerido: ${entry.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                    )
                }
            }

            PixelButton(
                text = if (isFound) "✓ Registrado en Catálogo" else "+ Añadir a Catálogo",
                onClick = { onToggleFound(entry.id) },
                outlined = !isFound,
                container = Green,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun DexDetailScreen(entry: DexEntry, zoneId: String, found: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
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
                        contentDescription = "Foto o dibujo de ${entry.name}",
                        modifier = Modifier.size(86.dp),
                        shapeRadius = 20.dp,
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
                    Text(
                        entry.description.ifBlank { CreatureCopy.line(entry.category, entry.location) },
                        color = Ink,
                        style = MaterialTheme.typography.bodySmall,
                    )
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

            // Horario con símbolos e indicador
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("⏰ Horario:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    ScheduleBadges(entry.schedule)
                }
                Text(
                    entry.schedule.joinToString { DexSchedule.scheduleLabel(it) }.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    modifier = Modifier.padding(start = 24.dp),
                )
            }

            // Clima con símbolos
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("🌤️ Clima:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    WeatherBadges(entry.weather)
                }
                Text(
                    entry.weather.joinToString { DexSchedule.weatherEs(it) }.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink,
                    modifier = Modifier.padding(start = 24.dp),
                )
            }

            LabelValue("🌐 En tu hora local", localHours.ifBlank { "—" }, false)
            if (entry.level != null) LabelValue("⭐ Nivel", entry.level.toString(), false)
            if (!entry.shadow.isNullOrBlank()) LabelValue("👥 Sombra", entry.shadow, false)
            if (!entry.event.isNullOrBlank()) LabelValue("🎉 Evento", entry.event, false)
        }

        PixelButton(
            text = if (found) "✓ Registrado en tu Catálogo" else "+ Marcar como Capturado",
            onClick = onToggle,
            outlined = !found,
            container = Green,
            modifier = Modifier.fillMaxWidth(),
        )

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
private fun ScheduleWeatherLegend() {
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
            LegendItem("🌅", "Amanecer (08-14h)")
            LegendItem("☀️", "Día (14-20h)")
            LegendItem("🌤️", "Tarde (20-02h)")
            LegendItem("🌆", "Noche (02-08h)")
            LegendItem("🌧️", "Lluvia")
            LegendItem("🌈", "Arcoíris")
        }
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

@Composable
private fun ScheduleBadges(schedule: List<String>) {
    val allSlots = listOf(
        "Dawn" to "🌅",
        "Day" to "☀️",
        "Dusk" to "🌤️",
        "Night" to "🌆",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        allSlots.forEach { (slotKey, icon) ->
            val active = slotKey in schedule
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (active) HoneySoft else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (active) Honey else Line.copy(alpha = 0.4f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    fontSize = 11.sp,
                    modifier = Modifier.clip(CircleShape),
                    color = if (active) Color.Unspecified else Color.Gray.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun WeatherBadges(weather: List<String>) {
    val allWeathers = listOf(
        "Sunny" to "☀️",
        "Rainy" to "🌧️",
        "Rainbow" to "🌈",
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        allWeathers.forEach { (wKey, icon) ->
            val active = wKey in weather
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (active) Sky else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (active) SkyBlue else Line.copy(alpha = 0.4f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    fontSize = 11.sp,
                    color = if (active) Color.Unspecified else Color.Gray.copy(alpha = 0.35f),
                )
            }
        }
    }
}

