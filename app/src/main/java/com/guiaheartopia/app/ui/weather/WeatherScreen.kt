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
import com.guiaheartopia.app.data.SampleWeather
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.data.VerifiedCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyWeather
import com.guiaheartopia.app.domain.WeatherKind
import com.guiaheartopia.app.ui.components.GameTimeCard
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

private enum class CalendarView(val label: String) {
    MONTH("Mes"),
    WEEK("Semana"),
    LIST("Lista"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherScreen(
    countryId: String,
    zoneId: String,
    originId: String,
    onRegion: (String, String, String) -> Unit,
) {
    val country = Countries.byId(countryId)
    val safeZone = country.zones.firstOrNull { it.zoneId == zoneId }?.zoneId ?: country.zones.first().zoneId
    val zone = runCatching { ZoneId.of(safeZone) }.getOrElse { ZoneId.of("America/Santiago") }
    val origin = SampleWeather.origins.firstOrNull { it.id == originId } ?: SampleWeather.origins.first()
    var monthToken by rememberSaveable { mutableStateOf(YearMonth.now(zone).toString()) }
    var selectedToken by rememberSaveable { mutableStateOf<String?>(null) }
    var filterName by rememberSaveable { mutableStateOf(WeatherFilter.ALL.name) }
    var viewName by rememberSaveable { mutableStateOf(CalendarView.MONTH.name) }
    val skyContext = androidx.compose.ui.platform.LocalContext.current
    val sky = androidx.compose.runtime.remember { SkyCatalog.load(skyContext) }
    val month = YearMonth.parse(monthToken)
    val filter = WeatherFilter.valueOf(filterName)
    val now = ZonedDateTime.now(zone)
    val clock = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale("es"))
    val selected = selectedToken?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Clima por país", style = MaterialTheme.typography.headlineSmall)
        GameTimeCard(zone.id)
        val zoneLabel = Countries.zoneLabel(zone.id)
        Text("Reloj: ${now.format(clock)} · desfase ${now.offset.id}", style = MaterialTheme.typography.bodySmall)
        val today = LocalDate.now(zone)
        TodayResources(today)
        PickerField(
            label = "País",
            value = country.name,
            options = Countries.all.map { it.id to it.name },
            onPick = { id ->
                val next = Countries.byId(id)
                val nextZone = next.zones.firstOrNull { it.zoneId == safeZone }?.zoneId ?: next.zones.first().zoneId
                onRegion(id, nextZone, origin.id)
            },
        )
        PickerField(
            label = "Zona horaria",
            value = country.zones.first { it.zoneId == safeZone }.label,
            options = country.zones.map { it.zoneId to it.label },
            onPick = { onRegion(country.id, it, origin.id) },
        )
        PickerField(
            label = "Origen horario",
            value = origin.name,
            options = SampleWeather.origins.map { it.id to it.name },
            onPick = { onRegion(country.id, safeZone, it) },
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
                month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("es"))).replaceFirstChar { it.titlecase() },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { monthToken = month.plusMonths(1).toString() }) { Text("Mes siguiente") }
        }
        if (month != YearMonth.from(now)) {
            TextButton(onClick = { monthToken = YearMonth.from(now).toString() }) { Text("Volver a este mes") }
        }
        Text("Zona de esta vista: $zoneLabel", style = MaterialTheme.typography.bodySmall)
        Text(sky.reviewLabel(), style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalendarView.entries.forEach { item ->
                FilterChip(
                    selected = viewName == item.name,
                    onClick = { viewName = item.name },
                    label = { Text(item.label) },
                )
            }
        }
        val view = CalendarView.valueOf(viewName)
        if (view == CalendarView.MONTH) {
        WeekHeader()
        MonthGrid(
            month = month,
            today = now.toLocalDate(),
            selected = selected,
            filter = filter,
            sky = sky,
            onSelect = { selectedToken = it.toString() },
        )
        } else if (view == CalendarView.WEEK) {
            WeekList(month, selected ?: now.toLocalDate(), zone, sky) { selectedToken = it.toString() }
        } else {
            MonthList(month, selected, zone, sky) { selectedToken = it.toString() }
        }
        Legend()
        val day = selected ?: now.toLocalDate()
        DayDetail(day, zone, sky)
    }
}

@Composable
private fun TodayResources(today: LocalDate) {
    val day = ResourceCalendar.on(today)
    PixelCard {
        Text("Hoy", style = MaterialTheme.typography.titleSmall)
        Text("Liuliu: ${ResourceCalendar.label(day.liuliu)}", color = Green)
        Text("flurita: ${ResourceCalendar.label(day.flurita)}", color = Orange)
        Text(certaintyCopy(day.certainty), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WeekHeader() {
    Row(Modifier.fillMaxWidth()) {
        listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
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
                        val bg = when {
                            selected == date -> GreenSoft
                            date == today -> Sky
                            else -> Paper
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.62f)
                                .border(if (selected == date) 2.dp else 1.dp, if (selected == date) Green else Ink.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                .background(bg, RoundedCornerShape(10.dp))
                                .clickable { onSelect(date) }
                                .padding(2.dp),
                        ) {
                            Text("$dayNumber", style = MaterialTheme.typography.labelMedium)
                            SkyBar(sky.on(date), Modifier.align(Alignment.Center).fillMaxWidth().height(16.dp))
                            Column(Modifier.align(Alignment.BottomCenter)) {
                                if (filter != WeatherFilter.FLURITA && data.liuliu != null) {
                                    Text(shortSpot(data.liuliu), color = Green, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp)
                                }
                                if (filter != WeatherFilter.LIULIU && data.flurita != null) {
                                    Text(shortSpot(data.flurita), color = Orange, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp)
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

private fun certaintyCopy(certainty: DayCertainty): String = when (certainty) {
    DayCertainty.CONSENSO -> "Dos listas coinciden en este día."
    DayCertainty.ESTIMADO -> "Estimado con el ciclo de 50 días. La otra lista no publica este mes día a día."
    DayCertainty.SIN_DATOS -> "Sin datos para este día. No hay una lista que lo confirme."
}

@Composable
private fun DayDetail(date: LocalDate, zone: ZoneId, sky: com.guiaheartopia.app.data.SkyTable) {
    val title = date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es")))
    val data = ResourceCalendar.on(date)
    val event = VerifiedCatalog.events.firstOrNull { event ->
        event.start != null && event.end != null && !date.isBefore(event.start) && !date.isAfter(event.end)
    }
    Text(title.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleMedium)
    Text("Hora local de ${Countries.zoneLabel(zone.id)}", style = MaterialTheme.typography.bodySmall)
    PixelCard {
        Text("Liuliu: ${ResourceCalendar.label(data.liuliu)}", color = Green, style = MaterialTheme.typography.titleSmall)
        Text("flurita: ${ResourceCalendar.label(data.flurita)}", color = Orange, style = MaterialTheme.typography.titleSmall)
        Text(certaintyCopy(data.certainty), style = MaterialTheme.typography.bodyMedium)
        if (data.sources > 0) {
            Text(
                if (data.certainty == DayCertainty.CONSENSO) "Fuentes de acuerdo: ${data.sources}." else "Listas que publican el día: ${data.sources}.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    val weatherDay = sky.on(date)
    PixelCard {
        Text("Clima en tu hora", style = MaterialTheme.typography.titleSmall)
        if (weatherDay == null) {
            Text("Sin datos de clima para este día.", style = MaterialTheme.typography.bodyMedium)
        } else {
            SkyBar(weatherDay, Modifier.fillMaxWidth().height(22.dp))
            Text(SkyWeather.summary(weatherDay, zone), style = MaterialTheme.typography.bodyLarge)
            weatherDay.slots.forEach { slot ->
                val (start, end) = SkyWeather.localWindow(date, slot, zone)
                val clock = DateTimeFormatter.ofPattern("HH:mm")
                Text(
                    "${slot.title()}  ${start.toLocalTime().format(clock)}–${end.toLocalTime().format(clock)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
    if (event != null) {
        PixelCard {
            Text(event.title, style = MaterialTheme.typography.titleSmall)
            Text(event.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SkyBar(day: SkyDay?, modifier: Modifier = Modifier) {
    val slots = day?.slots
    Row(modifier.clip(RoundedCornerShape(8.dp)).background(Beige)) {
        if (slots.isNullOrEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight().background(Beige))
        } else {
            slots.forEach { slot ->
                Box(
                    Modifier.weight(1f).fillMaxHeight().background(skyColor(slot.kind)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(slot.kind.emoji, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Clip)
                }
            }
        }
    }
}

private fun skyColor(kind: SkyKind): Color = when (kind) {
    SkyKind.SOLEADO -> Honey
    SkyKind.DESPEJADO -> Color(0xFFD9E4F2)
    SkyKind.NUBLADO -> Color(0xFFC5CED6)
    SkyKind.LLUVIA -> Color(0xFF7EB6D6)
    SkyKind.ARCOIRIS -> Color(0xFFD7B4E8)
    SkyKind.ARCOIRIS_LUNA -> Color(0xFFB9A6D6)
    SkyKind.METEORO -> Color(0xFFE7B15A)
    SkyKind.NIEVE -> Color(0xFFF4F7FB)
    SkyKind.AURORA -> Color(0xFF9AD7C4)
    SkyKind.CALOR -> Color(0xFFF0A07A)
    SkyKind.PETALOS -> Color(0xFFF3C1D4)
    SkyKind.SIN_DATOS -> Beige
}

@Composable
private fun WeekList(
    month: YearMonth,
    anchor: LocalDate,
    zone: ZoneId,
    sky: com.guiaheartopia.app.data.SkyTable,
    onSelect: (LocalDate) -> Unit,
) {
    val start = anchor.minusDays((anchor.dayOfWeek.value - 1).toLong())
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (offset in 0 until 7) {
            val date = start.plusDays(offset.toLong())
            if (date.month != month.month) continue
            DayHoursRow(date, zone, sky, onSelect)
        }
    }
}

@Composable
private fun MonthList(
    month: YearMonth,
    selected: LocalDate?,
    zone: ZoneId,
    sky: com.guiaheartopia.app.data.SkyTable,
    onSelect: (LocalDate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (day in 1..month.lengthOfMonth()) {
            DayHoursRow(month.atDay(day), zone, sky, onSelect, selected)
        }
    }
}

@Composable
private fun DayHoursRow(
    date: LocalDate,
    zone: ZoneId,
    sky: com.guiaheartopia.app.data.SkyTable,
    onSelect: (LocalDate) -> Unit,
    selected: LocalDate? = null,
) {
    val weatherDay = sky.on(date)
    PixelCard(Modifier.clickable { onSelect(date) }.then(if (selected == date) Modifier.border(2.dp, Green, RoundedCornerShape(22.dp)) else Modifier)) {
        Text("${date.dayOfMonth}", style = MaterialTheme.typography.titleSmall)
        SkyBar(weatherDay, Modifier.fillMaxWidth().height(18.dp))
        Text(
            weatherDay?.let { SkyWeather.summary(it, zone) } ?: "Sin datos",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun Legend() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Número verde: Liuliu", color = Green, style = MaterialTheme.typography.bodySmall)
        Text("Número naranja: flurita", color = Orange, style = MaterialTheme.typography.bodySmall)
        Text("B bosque · R ruinas", style = MaterialTheme.typography.bodySmall)
        Text("Barra: sol, lluvia, arcoíris y el resto, en tu hora", style = MaterialTheme.typography.bodySmall)
        Text("Fondo verde: día elegido", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun WeatherGlyph(kind: WeatherKind, modifier: Modifier = Modifier) {
    Canvas(modifier.border(1.dp, Ink).background(Paper)) {
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
