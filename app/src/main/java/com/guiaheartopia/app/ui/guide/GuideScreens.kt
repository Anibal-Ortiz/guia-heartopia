package com.guiaheartopia.app.ui.guide

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.guiaheartopia.app.ui.SearchDrafts
import com.guiaheartopia.app.ui.components.CertificadoMarkIcon
import com.guiaheartopia.app.ui.components.ConseguidoMarkIcon
import com.guiaheartopia.app.ui.components.MarkIconButton
import com.guiaheartopia.app.ui.components.NoCertificableMarkButton
import com.guiaheartopia.app.ui.components.ClearSkyIcon
import com.guiaheartopia.app.ui.components.CozyGuideIcon
import com.guiaheartopia.app.ui.components.CreatureGlyph
import com.guiaheartopia.app.ui.components.CreatureZoomDialog
import com.guiaheartopia.app.ui.components.EmptyStateCard
import com.guiaheartopia.app.ui.components.rememberCreatureModel
import com.guiaheartopia.app.ui.components.rememberMinuteTick
import com.guiaheartopia.app.ui.components.CreatureImage
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
import com.guiaheartopia.app.ui.theme.OchreDark
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
import java.time.ZoneId

private val Tabs = GuideSections.dexCategories.map { CatalogLabels.category(it) }

/**
 * Iconos locales equivalentes a `Icons.Rounded.FilterList` / `Icons.Rounded.Info`.
 * El proyecto no incluye `material-icons-core`/`extended`, así que se definen aquí los trazos
 * oficiales de Material para no añadir dependencias ni tocar los archivos de build protegidos.
 */
private val FilterListRoundedIcon: ImageVector = ImageVector.Builder(
    name = "FilterListRounded",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(10f, 18f)
        lineTo(14f, 18f)
        lineTo(14f, 16f)
        lineTo(10f, 16f)
        close()
        moveTo(3f, 6f)
        lineTo(21f, 6f)
        lineTo(21f, 8f)
        lineTo(3f, 8f)
        close()
        moveTo(6f, 13f)
        lineTo(18f, 13f)
        lineTo(18f, 11f)
        lineTo(6f, 11f)
        close()
    }
}.build()

private val InfoRoundedIcon: ImageVector = ImageVector.Builder(
    name = "InfoRounded",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
        moveTo(13f, 17f)
        lineTo(11f, 17f)
        lineTo(11f, 11f)
        lineTo(13f, 11f)
        close()
        moveTo(13f, 9f)
        lineTo(11f, 9f)
        lineTo(11f, 7f)
        lineTo(13f, 7f)
        close()
    }
}.build()

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cabecera fija; la lista del catálogo se desplaza sola (LazyColumn con keys).
        if (onBack != null) {
            androidx.compose.material3.TextButton(onClick = onBack) { Text("Atrás") }
        }
        SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_guia,
            title = "Naturaleza",
            subtitle = "Peces, aves, insectos y fauna",
        )

        PillTabs(Tabs, tab) { tab = it }

        DexTab(
            category = GuideSections.dexCategories[tab],
            ui = ui,
            onOpen = onOpenDex,
            onToggleFound = onToggleFound,
            onToggleCertified = onToggleCertified,
            guideLaunch = guideLaunch,
            onSaveDexFilters = onSaveDexFilters,
            modifier = Modifier.weight(1f),
        )
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
    val nameSuggestions = remember(ui.animals) {
        ui.animals.map { it.name }.distinct().sorted()
    }

    GuideSearchField(
        query = query,
        onQueryChange = { query = it },
        placeholder = "Buscar animal...",
        leadingIcon = "🔍",
        draftKey = "guide:animals",
        shape = RoundedCornerShape(16.dp),
        suggestions = nameSuggestions,
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
        EmptyStateCard(
            title = if (ui.animals.isEmpty()) "Sin fichas todavía" else "Sin coincidencias",
            message = if (ui.animals.isEmpty()) {
                "Todavía no hay animales en el catálogo."
            } else {
                "Ningún animal coincide con tu búsqueda."
            },
            icon = if (ui.animals.isEmpty()) "🌱" else "🔍",
        )
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

/**
 * Campo de búsqueda con estado local síncrono y autocompletado opcional.
 *
 * El `value` externo puede llegar con retraso (p. ej. tras guardarlo en DataStore), lo que
 * reiniciaba la selección del cursor en cada tecla y, al escribir rápido (p. ej. "caracol"),
 * truncaba el texto a un valor viejo ("cara", "carol"). Por eso el texto mostrado **no** se
 * sincroniza con el estado persistido: vive en un borrador en memoria ([SearchDrafts]) que es
 * la única fuente de verdad y sobrevive a recomposiciones o recreaciones de la pantalla.
 * El valor externo solo se usa al abrir el buscador por primera vez.
 *
 * [suggestions] acota las predicciones al catálogo que se esté viendo (peces, insectos, aves…)
 * o a las ubicaciones presentes en ese catálogo, según el campo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuideSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: String,
    draftKey: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    dense: Boolean = false,
    resetToken: Int = 0,
    suggestions: List<String> = emptyList(),
) {
    val focusManager = LocalFocusManager.current
    // Estado síncrono: el TextFieldValue vive en memoria y nunca espera al estado persistido.
    // `resetToken` permite limpiar el campo desde fuera (p. ej. "Limpiar filtros") sin
    // reintroducir el salto de cursor al escribir.
    var textFieldValue by remember(draftKey, resetToken) {
        val initial = if (resetToken > 0) query else (SearchDrafts.read(draftKey) ?: query)
        mutableStateOf(
            TextFieldValue(
                text = initial,
                selection = TextRange(initial.length),
            ),
        )
    }
    var allowSuggestions by remember(draftKey, resetToken) { mutableStateOf(false) }

    val matches = remember(textFieldValue.text, suggestions) {
        val typed = textFieldValue.text.trim()
        if (typed.isEmpty() || suggestions.isEmpty()) {
            emptyList()
        } else {
            suggestions.filter { TextMatch.contains(it, typed) }.distinct().take(6)
        }
    }
    val showSuggestions = allowSuggestions && matches.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = showSuggestions,
        onExpandedChange = { allowSuggestions = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                SearchDrafts.write(draftKey, newValue.text)
                onQueryChange(newValue.text)
                allowSuggestions = true
            },
            placeholder = {
                Text(
                    placeholder,
                    style = if (dense) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            shape = shape,
            leadingIcon = { Text(leadingIcon, fontSize = 15.sp) },
            trailingIcon = {
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textFieldValue = TextFieldValue("")
                            SearchDrafts.write(draftKey, "")
                            onQueryChange("")
                            allowSuggestions = false
                        },
                        modifier = Modifier.semantics { contentDescription = "Limpiar búsqueda" },
                    ) {
                        Text("✕", fontSize = 15.sp, color = Ink.copy(alpha = 0.7f))
                    }
                }
            },
            singleLine = true,
            // Se deja la altura natural del componente: forzar 48 dp recortaba el texto de
            // bodyLarge (24 sp de línea + padding). En los campos compactos se usa bodyMedium.
            textStyle = if (dense) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )

        ExposedDropdownMenu(
            expanded = showSuggestions,
            onDismissRequest = { allowSuggestions = false },
            modifier = Modifier.heightIn(max = 280.dp),
        ) {
            matches.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        textFieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                        SearchDrafts.write(draftKey, suggestion)
                        onQueryChange(suggestion)
                        allowSuggestions = false
                        focusManager.clearFocus()
                    },
                )
            }
        }
    }
}

@Composable
private fun DexTab(
    category: String,
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onToggleFound: (String) -> Unit,
    onToggleCertified: (String, String) -> Unit,
    guideLaunch: GuideLaunch = GuideLaunch(),
    onSaveDexFilters: (String, DexCategoryFilters) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val all = remember(revision) { com.guiaheartopia.app.data.CreatureCatalog.load(context) }
    val sky = remember(revision) { SkyCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val instant = rememberMinuteTick()
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
    // El texto vive en memoria (y en [SearchDrafts] para sobrevivir recreaciones). Así la lista
    // se filtra al instante y no depende de cuándo termine de guardarse en DataStore.
    var nameQuery by remember(category) {
        mutableStateOf(SearchDrafts.read("dex:$category:name") ?: filters.query)
    }
    var placeQuery by remember(category) {
        mutableStateOf(SearchDrafts.read("dex:$category:place") ?: filters.place)
    }
    val canCertify = CatalogMarks.certifiable(category)
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

    var filtrosExpandidos by rememberSaveable(category) { mutableStateOf(false) }
    var showLegend by remember { mutableStateOf(false) }
    var placeResetToken by remember(category) { mutableIntStateOf(0) }

    // La leyenda del diálogo incluye siempre los tres climas canónicos.
    val weatherKeys = remember(all, category) {
        (all.filter { it.category == category }.flatMap { it.weather } +
            listOf("Sunny", "Rainy", "Rainbow")).distinct()
    }

    // Predicciones acotadas al catálogo visible: nombres de esta categoría y sus ubicaciones.
    val nameSuggestions = remember(categoryAll) {
        categoryAll.map { it.name }.distinct().sorted()
    }
    val placeSuggestions = remember(categoryAll) {
        categoryAll
            .flatMap { it.location }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    fun weatherLabel(key: String): String = when (key) {
        "Sunny" -> "☀️ Despejado"
        "Rainy" -> "🌧️ Lluvia"
        "Rainbow" -> "🌈 Arcoíris"
        else -> "Todos"
    }

    fun clearPlace() {
        placeQuery = ""
        SearchDrafts.clear("dex:$category:place")
        placeResetToken++
        onSaveDexFilters(category, stored.copy(place = ""))
    }

    fun clearFilters() {
        placeQuery = ""
        SearchDrafts.clear("dex:$category:place")
        placeResetToken++
        onSaveDexFilters(
            category,
            stored.copy(
                place = "",
                weather = "Todos",
                onlyNow = false,
                registered = "todos",
                certified = "todos",
                sortByLevel = false,
                chosenLevel = -1,
                eventFocus = "",
            ),
        )
    }

    val placeActive = placeQuery.isNotBlank()
    val weatherActive = filters.weather != "Todos"
    val nowActive = filters.onlyNow
    val sortActive = filters.sortByLevel
    val registeredActive = filters.registered != "todos"
    val certifiedActive = filters.certified != "todos"
    val levelActive = filters.chosenLevel >= 0
    val activeFilterCount = listOf(
        placeActive, weatherActive, nowActive, sortActive,
        registeredActive, certifiedActive, levelActive,
    ).count { it }

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
            (nameQuery.isBlank() || TextMatch.contains(entry.name, nameQuery)) &&
            (placeQuery.isBlank() || entry.location.any { TextMatch.contains(it, placeQuery) }) &&
            (filters.weather == "Todos" || filters.weather in entry.weather) &&
            CreatureAvailability.levelAllowed(entry.level, levelCap) &&
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

    Column(modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Cabecera y filtros: una sola celda estable para no recomponerlos al scrollear.
            item(key = "filtros") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "$capturedCount/${categoryAll.size} capturadas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                IconButton(
                    onClick = { showLegend = true },
                    modifier = Modifier
                        .size(32.dp)
                        .semantics { contentDescription = "Guía de símbolos" },
                ) {
                    Icon(
                        InfoRoundedIcon,
                        contentDescription = null,
                        tint = GreenDark,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                "Franja actual: ${DexSchedule.titled(current.slot)}",
                style = MaterialTheme.typography.bodySmall,
                color = GreenDark,
            )
        }

        // Barra compacta de una sola fila: búsqueda por nombre + acceso a filtros.
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GuideSearchField(
                query = nameQuery,
                onQueryChange = {
                    nameQuery = it
                    onSaveDexFilters(category, stored.copy(query = it))
                },
                placeholder = "Buscar por nombre...",
                leadingIcon = "🔍",
                draftKey = "dex:$category:name",
                modifier = Modifier.weight(1f),
                dense = true,
                suggestions = nameSuggestions,
            )
            FilterChip(
                selected = filtrosExpandidos || activeFilterCount > 0,
                onClick = { filtrosExpandidos = !filtrosExpandidos },
                label = {
                    Text(
                        if (activeFilterCount > 0) "Filtros ($activeFilterCount)" else "Filtros",
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        FilterListRoundedIcon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GreenSoft,
                    selectedLabelColor = GreenDark,
                ),
            )
        }

        if (!filtrosExpandidos && activeFilterCount > 0) {
            LazyRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (placeActive) {
                    item { RemovableFilterChip("📍 $placeQuery", ::clearPlace) }
                }
                if (weatherActive) {
                    item {
                        RemovableFilterChip(weatherLabel(filters.weather)) {
                            onSaveDexFilters(category, stored.copy(weather = "Todos"))
                        }
                    }
                }
                if (nowActive) {
                    item {
                        RemovableFilterChip("Disponible ahora") {
                            onSaveDexFilters(category, stored.copy(onlyNow = false))
                        }
                    }
                }
                if (sortActive) {
                    item {
                        RemovableFilterChip("Orden: nivel") {
                            onSaveDexFilters(category, stored.copy(sortByLevel = false))
                        }
                    }
                }
                if (registeredActive) {
                    item {
                        RemovableFilterChip(
                            if (filters.registered == "si") "✓ Registrados" else "○ Sin registrar",
                        ) { onSaveDexFilters(category, stored.copy(registered = "todos")) }
                    }
                }
                if (certifiedActive) {
                    item {
                        RemovableFilterChip(
                            if (filters.certified == "si") "Certificados" else "No certificados",
                        ) { onSaveDexFilters(category, stored.copy(certified = "todos")) }
                    }
                }
                if (levelActive) {
                    item {
                        RemovableFilterChip("Nivel $levelCap") {
                            onSaveDexFilters(category, stored.copy(chosenLevel = -1))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = filtrosExpandidos) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GuideSearchField(
                    query = placeQuery,
                    onQueryChange = {
                        placeQuery = it
                        onSaveDexFilters(category, stored.copy(place = it))
                    },
                    placeholder = "Filtrar por ubicación...",
                    leadingIcon = "📍",
                    draftKey = "dex:$category:place",
                    dense = true,
                    resetToken = placeResetToken,
                    suggestions = placeSuggestions,
                )

                LazyRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(listOf("Todos", "Sunny", "Rainy", "Rainbow")) { key ->
                        FilterChip(
                            selected = filters.weather == key,
                            onClick = { onSaveDexFilters(category, stored.copy(weather = key)) },
                            label = { Text(weatherLabel(key)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HoneySoft,
                                selectedLabelColor = Ink,
                            ),
                        )
                    }
                }

                LazyRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        FilterChip(
                            selected = filters.sortByLevel,
                            onClick = { onSaveDexFilters(category, stored.copy(sortByLevel = !filters.sortByLevel)) },
                            label = { Text(if (filters.sortByLevel) "Orden: nivel" else "Orden: nombre") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HoneySoft,
                                selectedLabelColor = Ink,
                            ),
                        )
                    }
                    item {
                        FilterChip(
                            selected = filters.onlyNow,
                            onClick = { onSaveDexFilters(category, stored.copy(onlyNow = !filters.onlyNow)) },
                            label = { Text("Disponible ahora") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenSoft,
                                selectedLabelColor = GreenDark,
                            ),
                        )
                    }
                    item {
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
                    }
                    item {
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
                    }
                    if (canCertify) {
                        item {
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
                        }
                        item {
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
                }

                if (activeFilterCount > 0) {
                    TextButton(
                        onClick = ::clearFilters,
                        modifier = Modifier.height(28.dp),
                    ) {
                        Text(
                            "Limpiar filtros",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                if (catalogMax != null) {
                    LevelDropdown(
                        selected = levelCap,
                        maximum = catalogMax,
                        onSelect = { level -> onSaveDexFilters(category, stored.copy(chosenLevel = level ?: -1)) },
                    )
                }
            }
        }

        if (items.isNotEmpty()) {
            Text(
                "${items.size} en esta lista",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Muted,
            )
        }
            } // Column de filtros
        } // item("filtros")

        if (items.isEmpty()) {
            item(key = "vacio") {
                EmptyStateCard(
                    title = if (categoryAll.isEmpty()) "Sin datos todavía" else "Sin coincidencias",
                    message = if (categoryAll.isEmpty()) {
                        "Esta categoría se completará cuando se publique el catálogo."
                    } else {
                        "No se encontraron criaturas con estos filtros."
                    },
                    icon = if (categoryAll.isEmpty()) "🌱" else "🔍",
                )
            }
        }

        items(items, key = { it.id }) { entry ->
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

    if (showLegend) {
        AlertDialog(
            onDismissRequest = { showLegend = false },
            title = {
                Text(
                    "Guía de símbolos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendItem("🌅", "Amanecer")
                    LegendItem("☀️", "Día")
                    LegendItem("🌤️", "Tarde")
                    LegendItem("🌆", "Noche")
                    weatherKeys.distinct().forEach { key -> LegendWeather(key) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLegend = false }) {
                    Text("Entendido", color = GreenDark)
                }
            },
        )
    }
    } // Column contenedor (LazyColumn + diálogo)
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
    val instant = rememberMinuteTick()
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

            DexLocalScheduleSection(entry.schedule, zone, instant)
            nextWindowLabel?.let { label ->
                Spacer(Modifier.height(8.dp))
                Text(
                    "Próxima ventana",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Ink,
                )
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
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
            ) { ConseguidoMarkIcon(on = found) }
            if (CatalogMarks.certifiable(entry)) {
                MarkIconButton(
                    on = certified,
                    description = if (certified) "Quitar certificado" else "Marcar como certificado",
                    onClick = onToggleCertified,
                ) { CertificadoMarkIcon(on = certified) }
            } else if (CatalogMarks.certifiable(entry.category)) {
                NoCertificableMarkButton()
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

/** Chip delgado con una × para quitar un filtro activo mientras el panel está plegado. */
@Composable
private fun RemovableFilterChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GreenSoft)
            .clickable(onClick = onRemove)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .semantics { contentDescription = "Quitar filtro $label" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = GreenDark)
        Text(
            "×",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = GreenDark,
        )
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
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
            ) { ConseguidoMarkIcon(on = found) }
            if (certifiable) {
                MarkIconButton(
                    on = certified,
                    description = if (certified) "Quitar certificado" else "Marcar como certificado",
                    onClick = onToggleCertified,
                ) { CertificadoMarkIcon(on = certified) }
            } else if (CatalogMarks.certifiable(entry.category)) {
                NoCertificableMarkButton()
            }
        }
    }
}

@Composable
private fun DexLocalScheduleSection(
    schedule: List<String>,
    zone: ZoneId,
    instant: Instant,
) {
    val windows = DexSchedule.windowsAround(zone, instant).filter { it.slot.key in schedule }
    Spacer(Modifier.height(8.dp))
    Text(
        "En tu hora local",
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = Ink,
    )
    if (windows.isEmpty()) {
        Text("—", style = MaterialTheme.typography.bodyMedium, color = Muted)
    } else {
        windows.forEach { window ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (window.slot.emoji.isNotBlank()) {
                        Text(window.slot.emoji, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        window.slot.label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Ink,
                    )
                }
                Text(
                    DexSchedule.hoursLabel(window),
                    modifier = Modifier.width(112.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }
    }
}

@Composable
private fun CertifiedSeal() {
    val sealOchre = Ochre
    val sealOchreDark = OchreDark
    val sealPaper = Paper
    Canvas(
        Modifier
            .size(16.dp)
            .semantics { contentDescription = "Certificado" },
    ) {
        val radius = size.minDimension / 2f
        drawCircle(color = sealOchre, radius = radius)
        drawCircle(
            color = sealOchreDark,
            radius = radius * 0.72f,
            style = Stroke(width = radius * 0.14f),
        )
        val arm = radius * 0.34f
        val stroke = radius * 0.16f
        drawLine(sealPaper, center + Offset(0f, -arm), center + Offset(arm, 0f), stroke, StrokeCap.Round)
        drawLine(sealPaper, center + Offset(arm, 0f), center + Offset(0f, arm), stroke, StrokeCap.Round)
        drawLine(sealPaper, center + Offset(0f, arm), center + Offset(-arm, 0f), stroke, StrokeCap.Round)
        drawLine(sealPaper, center + Offset(-arm, 0f), center + Offset(0f, -arm), stroke, StrokeCap.Round)
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
    val glyphOchre = Ochre
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
                drawPath(path, glyphOchre)
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

