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
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.PlacedSlot
import com.guiaheartopia.app.domain.ResourceKeys
import com.guiaheartopia.app.domain.WeatherCalendar
import com.guiaheartopia.app.domain.WeatherKind
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Lilac
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.Peach
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
    val placed = WeatherCalendar.place(SampleWeather.slots, SampleWeather.origins, zone, origin.id)
    val visible = placed.filter { slot ->
        when (filter) {
            WeatherFilter.ALL -> true
            WeatherFilter.FLURITA -> slot.slot.sightings.any { it.key == ResourceKeys.FLURITA }
            WeatherFilter.LIULIU -> slot.slot.sightings.any { it.key == ResourceKeys.LIULIU }
            WeatherFilter.WEATHER -> true
        }
    }
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
        Text("Reloj: ${now.format(clock)} · UTC${now.offset.id}", style = MaterialTheme.typography.bodySmall)
        com.guiaheartopia.app.ui.components.PixelCard {
            Text("Recursos de noviembre 2026", style = MaterialTheme.typography.titleSmall)
            val spot = com.guiaheartopia.app.data.VerifiedCatalog.novemberSpot(java.time.LocalDate.now(zone))
            if (spot == null) {
                Text("Hoy no cae en el mes contrastado. El estado del clima por hora sigue pendiente.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Liuliu: ${com.guiaheartopia.app.data.VerifiedCatalog.spotLabel(spot.first)}")
                Text("flurita: ${com.guiaheartopia.app.data.VerifiedCatalog.spotLabel(spot.second)}")
            }
            Text(com.guiaheartopia.app.data.VerifiedCatalog.SPOT_SOURCES, style = MaterialTheme.typography.bodySmall)
        }
        com.guiaheartopia.app.data.VerifiedCatalog.events.forEach { event ->
            com.guiaheartopia.app.ui.components.PixelCard {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                Text(event.summary, style = MaterialTheme.typography.bodyMedium)
                Text(event.sources, style = MaterialTheme.typography.bodySmall)
            }
        }
        VerifyBadge("Pendiente de verificar")
        Text(
            "Las franjas son de ejemplo. La hora sí se convierte con la zona IANA del país, incluido el horario de verano. «flurita» y «Liuliu» se muestran con ese nombre: no hay fuente fiable aquí para corregirlos ni para decir dónde aparecen.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(origin.note, style = MaterialTheme.typography.bodySmall)
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
            value = country.zones.first { it.zoneId == safeZone }.let { "${it.label} · ${it.zoneId}" },
            options = country.zones.map { it.zoneId to "${it.label} · ${it.zoneId}" },
            onPick = { onRegion(country.id, it, origin.id) },
        )
        PickerField(
            label = "Origen horario",
            value = "${origin.name} · ${origin.zoneId}",
            options = SampleWeather.origins.map { it.id to "${it.name} · ${it.zoneId}" },
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
            TextButton(onClick = { monthToken = month.minusMonths(1).toString() }) { Text("Mes −") }
            Text(
                month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("es"))),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(onClick = { monthToken = month.plusMonths(1).toString() }) { Text("Mes +") }
        }
        Text("Zona de esta vista: ${zone.id}", style = MaterialTheme.typography.bodySmall)
        WeekHeader()
        MonthGrid(
            month = month,
            placed = visible,
            selected = selected,
            onSelect = { selectedToken = it.toString() },
        )
        Legend()
        val day = selected
        if (day == null) {
            Text("Toca un día para ver la hora local, el clima de ejemplo y si hay flurita o Liuliu.", style = MaterialTheme.typography.bodyMedium)
        } else {
            DayDetail(day, WeatherCalendar.onDate(visible, day), zone)
        }
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
    placed: List<PlacedSlot>,
    selected: LocalDate?,
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
                        Box(Modifier.weight(1f).aspectRatio(0.72f))
                    } else {
                        val date = month.atDay(dayNumber)
                        val slots = placed.filter { it.localDate == date }
                        val kind = slots.firstOrNull()?.slot?.kind
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(0.72f)
                                .border(2.dp, Ink)
                                .background(if (selected == date) Peach else Paper)
                                .clickable { onSelect(date) }
                                .padding(2.dp),
                        ) {
                            Text("$dayNumber", style = MaterialTheme.typography.labelMedium)
                            if (kind != null) {
                                WeatherGlyph(kind, Modifier.align(Alignment.Center).size(16.dp))
                            }
                            Row(Modifier.align(Alignment.BottomCenter), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (slots.any { s -> s.slot.sightings.any { it.key == ResourceKeys.FLURITA } }) {
                                    Box(Modifier.size(6.dp).background(Honey))
                                }
                                if (slots.any { s -> s.slot.sightings.any { it.key == ResourceKeys.LIULIU } }) {
                                    Box(Modifier.size(6.dp).background(Lilac))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDetail(date: LocalDate, slots: List<PlacedSlot>, zone: ZoneId) {
    val title = date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es")))
    Text(title.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.titleMedium)
    Text("Hora local de ${zone.id}", style = MaterialTheme.typography.bodySmall)
    if (slots.isEmpty()) {
        PixelCard { Text("Este día no tiene franjas de ejemplo con el filtro actual.") }
        return
    }
    slots.forEach { placed ->
        val localClock = DateTimeFormatter.ofPattern("HH:mm")
        PixelCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                WeatherGlyph(placed.slot.kind, Modifier.size(28.dp))
                Column {
                    Text(placed.slot.kind.label, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Local ${placed.local.toLocalTime().format(localClock)} · UTC${placed.local.offset.id}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Text(
                "Origen ${placed.slot.originLocal.toLocalTime().format(localClock)} del ${placed.slot.originLocal.toLocalDate()} · ${placed.originZone.id}",
                style = MaterialTheme.typography.bodySmall,
            )
            if (placed.dayShifted) {
                Text(
                    "El cambio de zona mueve esta franja al día local ${placed.localDate}, distinto del día de origen.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(placed.slot.summary, style = MaterialTheme.typography.bodyMedium)
            if (!placed.slot.verified) VerifyBadge("Pendiente de verificar")
            placed.slot.sightings.forEach { sight ->
                Text(sight.displayName, style = MaterialTheme.typography.titleSmall)
                Text(
                    "Lugar: ${sight.location ?: "pendiente de verificar"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Aparición: ${sight.appearance ?: "pendiente de verificar"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun Legend() {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WeatherKind.entries.forEach { kind ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WeatherGlyph(kind, Modifier.size(18.dp))
                Text(kind.label, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("■ miel = flurita", style = MaterialTheme.typography.bodySmall)
        Text("■ lila = Liuliu", style = MaterialTheme.typography.bodySmall)
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
