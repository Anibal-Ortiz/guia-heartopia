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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.data.VerifiedCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.ResourceCalendar
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
    FLURITA("flurita"),
    LIULIU("Liuliu"),
    WEATHER("Con clima"),
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherScreen(
    countryId: String,
    zoneId: String,
    onRegion: (String, String) -> Unit,
) {
    val country = Countries.byId(countryId)
    val safeZone = country.zones.firstOrNull { it.zoneId == zoneId }?.zoneId ?: country.zones.first().zoneId
    val zone = runCatching { ZoneId.of(safeZone) }.getOrElse { ZoneId.of("America/Santiago") }
    var monthToken by rememberSaveable { mutableStateOf(YearMonth.now(zone).toString()) }
    var selectedToken by rememberSaveable { mutableStateOf<String?>(null) }
    var filterName by rememberSaveable { mutableStateOf(WeatherFilter.ALL.name) }
    val skyContext = androidx.compose.ui.platform.LocalContext.current
    val sky = androidx.compose.runtime.remember { SkyCatalog.load(skyContext) }
    val month = YearMonth.parse(monthToken)
    val filter = WeatherFilter.valueOf(filterName)
    val now = ZonedDateTime.now(zone)
    val clock = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.forLanguageTag("es"))
    val selected = selectedToken?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Calendario", style = MaterialTheme.typography.headlineSmall)
        val zoneLabel = Countries.zoneLabel(zone.id)
        PickerField(
            label = "País",
            value = country.name,
            options = Countries.all.map { it.id to it.name },
            onPick = { id ->
                val next = Countries.byId(id)
                val nextZone = next.zones.firstOrNull { it.zoneId == safeZone }?.zoneId ?: next.zones.first().zoneId
                onRegion(id, nextZone)
            },
        )
        PickerField(
            label = "Zona horaria",
            value = country.zones.first { it.zoneId == safeZone }.label,
            options = country.zones.map { it.zoneId to it.label },
            onPick = { onRegion(country.id, it) },
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
        if (month != YearMonth.from(now)) {
            TextButton(onClick = { monthToken = YearMonth.from(now).toString() }) { Text("Volver a este mes") }
        }
        Text("Hora local: $zoneLabel · ${now.format(clock)}", style = MaterialTheme.typography.bodySmall)
        Text(
            "Cada día sigue las franjas del juego (noche, amanecer, día, tarde), no el día civil 00:00–24:00.",
            style = MaterialTheme.typography.bodySmall,
        )
        WeekHeader()
        MonthGrid(
            month = month,
            today = now.toLocalDate(),
            selected = selected,
            filter = filter,
            sky = sky,
            zone = zone,
            onSelect = { selectedToken = it.toString() },
        )
        Legend()
        val day = selected ?: now.toLocalDate()
        DayDetail(day, zone, sky)
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
    onSelect: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val lead = (first.dayOfWeek.value + 6) % 7
    val cells = lead + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayNumber = index - lead + 1
                    if (dayNumber !in 1..month.lengthOfMonth()) {
                        Box(Modifier.weight(1f).aspectRatio(0.78f))
                    } else {
                        val date = month.atDay(dayNumber)
                        val data = ResourceCalendar.on(date)
                        val isToday = date == today
                        val isSelected = selected == date
                        val bg = when {
                            isSelected -> GreenSoft
                            isToday -> Sky
                            else -> Paper
                        }
                        val borderStroke = if (isSelected) 2.dp else 1.dp
                        val borderColor = when {
                            isSelected -> Green
                            isToday -> com.guiaheartopia.app.ui.theme.SkyTint
                            else -> com.guiaheartopia.app.ui.theme.Line
                        }

                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.64f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(borderStroke, borderColor, RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { onSelect(date) }
                                .padding(3.dp),
                        ) {
                            Text(
                                "$dayNumber",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isToday || isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                ),
                                color = if (isSelected) Green else Ink,
                            )
                            ClimateSkyBar(sky, date, zone, Modifier.align(Alignment.Center).fillMaxWidth().height(16.dp))
                            Column(Modifier.align(Alignment.BottomCenter)) {
                                if (filter != WeatherFilter.FLURITA && data.liuliu != null) {
                                    Text(shortSpot(data.liuliu), color = Green, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                }
                                if (filter != WeatherFilter.LIULIU && data.flurita != null) {
                                    Text(shortSpot(data.flurita), color = Orange, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
    val event = VerifiedCatalog.events.firstOrNull { event ->
        event.start != null && event.end != null && !date.isBefore(event.start) && !date.isAfter(event.end)
    }
    Text(title.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleMedium)
    Text("Hora local de ${Countries.zoneLabel(zone.id)}", style = MaterialTheme.typography.bodySmall)
    PixelCard {
        Text("Liuliu: ${ResourceCalendar.label(data.liuliu)}", color = Green, style = MaterialTheme.typography.titleSmall)
        Text("flurita: ${ResourceCalendar.label(data.flurita)}", color = Orange, style = MaterialTheme.typography.titleSmall)
    }
    val spans = SkyWeatherGameDay.spansForGameDay(sky, date, zone)
    PixelCard {
        Text("Clima por franjas del juego", style = MaterialTheme.typography.titleSmall)
        if (spans.isEmpty()) {
            Text("Sin datos de clima para este día.", style = MaterialTheme.typography.bodyMedium)
        } else {
            ClimateSkyBar(sky, date, zone, Modifier.fillMaxWidth().height(22.dp))
            spans.forEach { span ->
                Text(SkyWeatherGameDay.formatSpan(span, date), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    if (event != null) {
        PixelCard {
            Text(event.title, style = MaterialTheme.typography.titleSmall)
            Text("${event.start} → ${event.end}", style = MaterialTheme.typography.bodyMedium)
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
    val segments = SkyWeatherGameDay.barSegmentsForGameDay(sky, localDate, zone)
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(Beige)) {
        if (segments.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight().background(Beige))
        } else {
            segments.forEach { segment ->
                Box(
                    Modifier.weight(segment.weight.toFloat()).fillMaxHeight().background(skyColor(segment.kind)),
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
    }
}

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
        Text("Número naranja: flurita", color = Orange, style = MaterialTheme.typography.bodySmall)
        Text("B bosque · R ruinas", style = MaterialTheme.typography.bodySmall)
        Text("Barra: despejado ☀️, lluvia 🌧️, arcoíris 🌈, nieve ❄️ o meteoritos ☄️", style = MaterialTheme.typography.bodySmall)
        Text("Fondo verde: día elegido", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun WeatherGlyph(kind: WeatherKind, modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, com.guiaheartopia.app.ui.theme.Line, RoundedCornerShape(8.dp))
            .background(Paper),
    ) {
        val ink = Ink
        val w = size.minDimension
        when (kind) {
            WeatherKind.SUN -> {
                drawCircle(Honey, w * 0.22f, center)
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
                drawCircle(Sky, w * 0.22f, Offset(center.x, center.y - w * 0.1f))
                drawLine(Sky, Offset(center.x - w * 0.15f, center.y + w * 0.15f), Offset(center.x - w * 0.25f, center.y + w * 0.4f), 3f)
                drawLine(Sky, Offset(center.x + w * 0.1f, center.y + w * 0.12f), Offset(center.x, center.y + w * 0.42f), 3f)
            }
            WeatherKind.CLOUD -> drawCircle(Color(0xFFD9E2EC), w * 0.28f, center)
            WeatherKind.SNOW -> {
                drawLine(Color.White, Offset(center.x, center.y - w * 0.35f), Offset(center.x, center.y + w * 0.35f), 3f)
                drawLine(Color.White, Offset(center.x - w * 0.3f, center.y), Offset(center.x + w * 0.3f, center.y), 3f)
                drawCircle(Sky, w * 0.08f, center)
            }
            WeatherKind.STORM -> {
                drawLine(Honey, Offset(center.x - w * 0.1f, center.y - w * 0.35f), Offset(center.x + w * 0.05f, center.y), 4f)
                drawLine(Honey, Offset(center.x + w * 0.05f, center.y), Offset(center.x - w * 0.15f, center.y + w * 0.4f), 4f)
            }
            WeatherKind.FOG -> {
                drawLine(Color(0xFFB7C4CE), Offset(w * 0.15f, center.y - w * 0.15f), Offset(w * 0.85f, center.y - w * 0.15f), 3f)
                drawLine(Color(0xFFB7C4CE), Offset(w * 0.2f, center.y + w * 0.1f), Offset(w * 0.8f, center.y + w * 0.1f), 3f)
            }
            WeatherKind.UNVERIFIED -> drawCircle(Honey, w * 0.12f, center)
        }
    }
}
