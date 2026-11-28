@file:OptIn(ExperimentalLayoutApi::class)

package com.guiaheartopia.app.ui.weather

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.dexEntryMatchesEvent
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.climateDisplay
import com.guiaheartopia.app.domain.WeatherKind
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.theme.Beige
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.LocalAppPalette
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.Sky
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class WeatherFilter(val label: String) {
    ALL("Todo"),
    FLURITA("Fluorita"),
    LIULIU("Liuliu"),
    WEATHER("Con clima"),
}

/** Una celda del mes ya derivada: la composición solo la dibuja. */
private data class CellData(
    val date: LocalDate?,
    val dayNumber: Int,
    val liuliu: String?,
    val flurita: String?,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasEvent: Boolean,
    val bg: Color,
    val borderStroke: Dp,
    val borderColor: Color,
    val numberColor: Color,
    val numberBold: Boolean,
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherScreen(
    countryId: String,
    zoneId: String,
    regionKey: String = "",
    onRegion: (String, String, String) -> Unit,
    onOpenEventCreatures: (String) -> Unit = {},
) {
    val country = Countries.byId(countryId)
    val chosenZone = Countries.zone(country.id, zoneId, regionKey)
    val safeZone = chosenZone.zoneId
    val zone = runCatching { ZoneId.of(safeZone) }.getOrElse { ZoneId.of("America/Santiago") }
    var monthToken by rememberSaveable { mutableStateOf(YearMonth.now(zone).toString()) }
    var selectedToken by rememberSaveable { mutableStateOf<String?>(null) }
    var filterName by rememberSaveable { mutableStateOf(WeatherFilter.ALL.name) }
    val skyContext = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val sky = androidx.compose.runtime.remember(revision, safeZone) { SkyCatalog.load(skyContext, safeZone) }
    val month = YearMonth.parse(monthToken)
    val filter = WeatherFilter.valueOf(filterName)
    val now = ZonedDateTime.now(zone)
    val clock = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.forLanguageTag("es"))
    val selected = selectedToken?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val eventsDoc = androidx.compose.runtime.remember(revision) {
        val raw = com.guiaheartopia.app.data.AndroidContentLibrary(skyContext)
            .effectiveBytes("calendar/eventos.json")
            ?.decodeToString()
        ReminderPlan.parseEvents(raw)
    }
    val eventDays = androidx.compose.runtime.remember(eventsDoc, zone) {
        ReminderPlan.eventCalendarDays(eventsDoc, zone)
    }
    val todayDate = now.toLocalDate()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        com.guiaheartopia.app.ui.components.SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_clima,
            title = "Calendario",
            subtitle = "El clima del mes, arriba del detalle",
            // Misma técnica que las otras cabeceras: se baja la imagen escalada
            // ×2,6 para que el corte caiga sobre el fondo y no sobre el dibujo.
            imageOffsetY = 120.dp,
        )
        val zoneLabel = Countries.zoneLabel(zone.id)
        PickerField(
            label = "País",
            value = country.name,
            options = Countries.all.map { it.id to it.name },
            onPick = { id ->
                val next = Countries.byId(id).zones.first()
                onRegion(id, next.zoneId, next.key)
            },
        )
        PickerField(
            label = if (country.zones.size > 1) "Estado o región" else "Zona horaria",
            value = chosenZone.label,
            options = country.zones.map { it.key to it.label },
            onPick = { key ->
                val zone = country.zones.first { it.key == key }
                onRegion(country.id, zone.zoneId, zone.key)
            },
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WeatherFilter.entries.forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { filterName = item.name },
                    label = { Text(item.label) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { monthToken = month.minusMonths(1).toString() }) { Text("Mes anterior") }
            Text(
                month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("es"))).replaceFirstChar { it.titlecase() },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { monthToken = month.plusMonths(1).toString() }) { Text("Mes siguiente") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (month != YearMonth.from(now)) {
                TextButton(onClick = { monthToken = YearMonth.from(now).toString() }) { Text("Volver a este mes") }
            }
            TextButton(onClick = {
                monthToken = YearMonth.from(now).toString()
                selectedToken = todayDate.toString()
            }) { Text("Volver a hoy") }
        }
        Text("Hora local: $zoneLabel · ${now.format(clock)}", style = MaterialTheme.typography.bodySmall)
        Text(
            "El día del servidor empieza en el amanecer y sigue Amanecer, Día, Tarde y Noche, hasta el amanecer siguiente.",
            style = MaterialTheme.typography.bodySmall,
        )
        WeekHeader()
        MonthGrid(
            month = month,
            today = todayDate,
            selected = selected,
            filter = filter,
            sky = sky,
            zone = zone,
            eventDays = eventDays,
            onSelect = { selectedToken = it.toString() },
        )
        Legend()
        val day = selected ?: now.toLocalDate()
        DayDetail(day, zone, sky)
        CalendarEvents(zone, onOpenCreatures = onOpenEventCreatures)
    }
}

@Composable
fun CalendarEvents(
    zone: ZoneId,
    onOpenCreatures: ((String) -> Unit)? = null,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val doc = androidx.compose.runtime.remember(revision) {
        val raw = com.guiaheartopia.app.data.AndroidContentLibrary(context)
            .effectiveBytes("calendar/eventos.json")
            ?.decodeToString()
        ReminderPlan.parseEvents(raw)
    }
    val catalog = androidx.compose.runtime.remember(revision) {
        com.guiaheartopia.app.data.CreatureCatalog.load(context)
    }
    val now = java.time.ZonedDateTime.now(zone)
    val rows = androidx.compose.runtime.remember(doc, zone) {
        ReminderPlan.calendarRows(doc, zone, now)
    }
    if (rows.isEmpty()) return
    Text("Eventos", style = MaterialTheme.typography.titleMedium)
    rows.forEach { row ->
        val creatureCount = catalog.count { dexEntryMatchesEvent(it, row.id, row.title) }
        PixelCard {
            Text(row.title, style = MaterialTheme.typography.titleMedium, color = Ink)
            row.detail?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink) }
            if (creatureCount > 0 && onOpenCreatures != null) {
                PixelButton(
                    text = "Ver criaturas del evento ($creatureCount)",
                    onClick = { onOpenCreatures(row.id) },
                    outlined = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WeekHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(com.guiaheartopia.app.ui.theme.PaperSubtle)
            .padding(vertical = 6.dp),
    ) {
        listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                ),
                color = Ink,
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate?,
    filter: WeatherFilter,
    sky: com.guiaheartopia.app.data.SkyTable,
    zone: ZoneId,
    eventDays: Set<LocalDate> = emptySet(),
    onSelect: (LocalDate) -> Unit,
) {
    // Se calcula una sola vez por mes/selección/filtro/paleta en vez de resolver cada celda
    // (recursos + colores) en cada recomposición. La paleta entra en las keys para que el
    // cambio de tema renueve los colores cacheados.
    val cellPalette = LocalAppPalette.current
    val cells = remember(month, today, selected, eventDays, filter, cellPalette) {
        val first = month.atDay(1)
        val lead = (first.dayOfWeek.value + 6) % 7
        val total = ((lead + month.lengthOfMonth() + 6) / 7) * 7
        (0 until total).map { index ->
            val dayNumber = index - lead + 1
            if (dayNumber !in 1..month.lengthOfMonth()) {
                CellData(null, 0, null, null, false, false, false, cellPalette.paper, 1.dp, cellPalette.line, cellPalette.ink, false)
            } else {
                val date = month.atDay(dayNumber)
                val data = ResourceCalendar.on(date)
                val isToday = date == today
                val isSelected = selected == date
                val hasEvent = date in eventDays
                CellData(
                    date = date,
                    dayNumber = dayNumber,
                    liuliu = if (filter != WeatherFilter.FLURITA) data.liuliu?.let(::shortSpot) else null,
                    flurita = if (filter != WeatherFilter.LIULIU) data.flurita?.let(::shortSpot) else null,
                    isToday = isToday,
                    isSelected = isSelected,
                    hasEvent = hasEvent,
                    bg = when {
                        isSelected -> cellPalette.greenSoft
                        isToday -> cellPalette.honeySoft
                        else -> cellPalette.paper
                    },
                    borderStroke = if (isToday || isSelected) 2.dp else 1.dp,
                    borderColor = when {
                        isSelected -> cellPalette.green
                        isToday -> cellPalette.honey
                        hasEvent -> cellPalette.orange
                        else -> cellPalette.line
                    },
                    numberColor = if (isSelected) cellPalette.green else cellPalette.ink,
                    numberBold = isToday || isSelected,
                )
            }
        }
    }
    val rows = cells.size / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 7) {
                    val cell = cells[row * 7 + col]
                    val date = cell.date
                    if (date == null) {
                        Box(Modifier.weight(1f).aspectRatio(0.78f))
                    } else {
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.64f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(cell.borderStroke, cell.borderColor, RoundedCornerShape(10.dp))
                                .background(cell.bg)
                                .clickable { onSelect(date) }
                                .padding(3.dp),
                        ) {
                            Text(
                                "${cell.dayNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (cell.numberBold) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                ),
                                color = cell.numberColor,
                            )
                            ClimateSkyBar(sky, date, zone, Modifier.align(Alignment.Center).fillMaxWidth().height(16.dp))
                            if (cell.hasEvent) {
                                Text(
                                    "●",
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    color = Orange,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            Column(Modifier.align(Alignment.BottomCenter)) {
                                cell.liuliu?.let {
                                    Text(it, color = Green, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                                cell.flurita?.let {
                                    Text(it, color = Orange, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shortSpot(raw: String): String = when (raw) {
    "Bosque" -> "B"
    "Ruinas" -> "R"
    else -> raw
}

@Composable
private fun DayDetail(date: LocalDate, zone: ZoneId, sky: com.guiaheartopia.app.data.SkyTable) {
    val title = date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es")))
    val data = ResourceCalendar.on(date)
    Text(title.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleMedium)
    Text("Hora local de ${Countries.zoneLabel(zone.id)}", style = MaterialTheme.typography.bodySmall)
    PixelCard {
        Text("Liuliu: ${ResourceCalendar.label(data.liuliu)}", color = Green, style = MaterialTheme.typography.titleSmall)
        Text("Fluorita: ${ResourceCalendar.label(data.flurita)}", color = Orange, style = MaterialTheme.typography.titleSmall)
    }
    val lines = SkyWeatherGameDay.compactLinesForGameDay(sky, date, zone)
    PixelCard {
        Text("Clima por franjas del juego", style = MaterialTheme.typography.titleSmall)
        if (lines.isEmpty()) {
            Text("Sin datos de clima para este día.", style = MaterialTheme.typography.bodyMedium)
        } else {
            ClimateSkyBar(sky, date, zone, Modifier.fillMaxWidth().height(22.dp))
            ClimateDetailList(lines, showFrames = false)
        }
    }
}

@Composable
fun ClimateDetailList(
    lines: List<com.guiaheartopia.app.domain.SkyWeatherGameDay.ClimateLine>,
    showFrames: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEach { line ->
            if (line.frame && !showFrames) return@forEach
            Text(
                line.text,
                modifier = if (line.frame || !showFrames) Modifier else Modifier.padding(start = 8.dp),
                style = if (line.frame) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
            )
        }
    }
}

@Composable
fun ClimateSkyBar(
    sky: com.guiaheartopia.app.data.SkyTable,
    localDate: LocalDate,
    zone: ZoneId,
    modifier: Modifier = Modifier,
) {
    val frames = SkyWeatherGameDay.barFrameSegmentsForGameDay(sky, localDate, zone)
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(Beige)) {
        if (frames.all { it.isEmpty() }) {
            Box(Modifier.weight(1f).fillMaxHeight().background(Beige))
        } else {
            frames.forEachIndexed { index, segments ->
                Row(Modifier.weight(1f).fillMaxHeight()) {
                    val total = segments.sumOf { it.weight }.coerceAtLeast(1)
                    segments.forEach { segment ->
                        Box(
                            Modifier
                                .weight(segment.weight.toFloat() / total)
                                .fillMaxHeight()
                                .background(skyColor(segment.kind)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                segment.kind.climateDisplay().emoji,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }
                if (index < frames.lastIndex) {
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Line),
                    )
                }
            }
        }
    }
}

@Composable
private fun skyColor(kind: SkyKind): Color = when (kind.climateDisplay()) {
    SkyKind.LLUVIA -> Color(0xFF7EB6D6)
    SkyKind.ARCOIRIS -> Color(0xFFD7B4E8)
    SkyKind.NIEVE -> Color(0xFFE7F2FA)
    SkyKind.METEORO -> Color(0xFF2C3A4A)
    SkyKind.SIN_DATOS -> Beige
    else -> Honey
}

@Composable
private fun Legend() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Número verde: Liuliu", color = Green, style = MaterialTheme.typography.bodySmall)
        Text("Número naranja: Fluorita", color = Orange, style = MaterialTheme.typography.bodySmall)
        Text("B bosque · R ruinas", style = MaterialTheme.typography.bodySmall)
        Text("Barra: despejado ☀️, lluvia 🌧️, arcoíris 🌈, nieve ❄️ o meteoritos ☄️", style = MaterialTheme.typography.bodySmall)
        Text("Fondo verde: día elegido", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun WeatherGlyph(kind: WeatherKind, modifier: Modifier = Modifier) {
    val glyphInk = Ink
    val glyphHoney = Honey
    val glyphSky = Sky
    Canvas(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, com.guiaheartopia.app.ui.theme.Line, RoundedCornerShape(8.dp))
            .background(Paper),
    ) {
        val ink = glyphInk
        val w = size.minDimension
        when (kind) {
            WeatherKind.SUN -> {
                drawCircle(glyphHoney, w * 0.22f, center)
                for (i in 0 until 8) {
                    val a = i * 45.0 * kotlin.math.PI / 180.0
                    drawLine(
                        ink,
                        Offset(center.x + kotlin.math.cos(a).toFloat() * w * 0.32f, center.y + kotlin.math.sin(a).toFloat() * w * 0.32f),
                        Offset(center.x + kotlin.math.cos(a).toFloat() * w * 0.46f, center.y + kotlin.math.sin(a).toFloat() * w * 0.46f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Square,
                    )
                }
            }
            WeatherKind.RAIN -> {
                drawCircle(glyphSky, w * 0.22f, Offset(center.x, center.y - w * 0.1f))
                drawLine(glyphSky, Offset(center.x - w * 0.15f, center.y + w * 0.15f), Offset(center.x - w * 0.25f, center.y + w * 0.4f), 3f)
                drawLine(glyphSky, Offset(center.x + w * 0.1f, center.y + w * 0.12f), Offset(center.x, center.y + w * 0.42f), 3f)
            }
            WeatherKind.CLOUD -> drawCircle(Color(0xFFD9E2EC), w * 0.28f, center)
            WeatherKind.SNOW -> {
                drawLine(Color.White, Offset(center.x, center.y - w * 0.35f), Offset(center.x, center.y + w * 0.35f), 3f)
                drawLine(Color.White, Offset(center.x - w * 0.3f, center.y), Offset(center.x + w * 0.3f, center.y), 3f)
                drawCircle(glyphSky, w * 0.08f, center)
            }
            WeatherKind.STORM -> {
                drawLine(glyphHoney, Offset(center.x - w * 0.1f, center.y - w * 0.35f), Offset(center.x + w * 0.05f, center.y), 4f)
                drawLine(glyphHoney, Offset(center.x + w * 0.05f, center.y), Offset(center.x - w * 0.15f, center.y + w * 0.4f), 4f)
            }
            WeatherKind.FOG -> {
                drawLine(Color(0xFFB7C4CE), Offset(w * 0.15f, center.y - w * 0.15f), Offset(w * 0.85f, center.y - w * 0.15f), 3f)
                drawLine(Color(0xFFB7C4CE), Offset(w * 0.2f, center.y + w * 0.1f), Offset(w * 0.8f, center.y + w * 0.1f), 3f)
            }
            WeatherKind.UNVERIFIED -> drawCircle(glyphHoney, w * 0.12f, center)
        }
    }
}
