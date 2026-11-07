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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.SampleWeather
import com.guiaheartopia.app.data.VerifiedCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.WeatherKind
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelCard
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
        WeekHeader()
        MonthGrid(
            month = month,
            today = now.toLocalDate(),
            selected = selected,
            filter = filter,
            onSelect = { selectedToken = it.toString() },
        )
        Legend()
        val day = selected ?: now.toLocalDate()
        DayDetail(day, zone)
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
                                .aspectRatio(0.78f)
                                .border(if (selected == date) 2.dp else 1.dp, if (selected == date) Green else Ink.copy(alpha = 0.35f), androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .background(bg, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .clickable { onSelect(date) }
                                .padding(3.dp),
                        ) {
                            Text("$dayNumber", style = MaterialTheme.typography.labelMedium)
                            WeatherGlyph(WeatherKind.UNVERIFIED, Modifier.align(Alignment.Center).size(14.dp))
                            Column(Modifier.align(Alignment.BottomCenter)) {
                                if (filter != WeatherFilter.FLURITA && data.liuliu != null) {
                                    Text(shortSpot(data.liuliu), color = Green, style = MaterialTheme.typography.labelMedium)
                                }
                                if (filter != WeatherFilter.LIULIU && data.flurita != null) {
                                    Text(shortSpot(data.flurita), color = Orange, style = MaterialTheme.typography.labelMedium)
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
private fun DayDetail(date: LocalDate, zone: ZoneId) {
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
    PixelCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherGlyph(WeatherKind.UNVERIFIED, Modifier.size(28.dp))
            Text("Clima por horas", style = MaterialTheme.typography.titleSmall)
        }
        Text(
            "Pendiente de verificar. Hay calendarios públicos, pero no dos que detallen las mismas horas de este mes. No rellenamos el hueco.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    if (event != null) {
        PixelCard {
            Text(event.title, style = MaterialTheme.typography.titleSmall)
            Text(event.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Legend() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Número verde: Liuliu", color = Green, style = MaterialTheme.typography.bodySmall)
        Text("Número naranja: flurita", color = Orange, style = MaterialTheme.typography.bodySmall)
        Text("B bosque · R ruinas", style = MaterialTheme.typography.bodySmall)
        Text("Círculo: clima sin confirmar", style = MaterialTheme.typography.bodySmall)
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
