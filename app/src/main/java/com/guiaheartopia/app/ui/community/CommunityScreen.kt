package com.guiaheartopia.app.ui.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.domain.climateDisplay
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.contentRevision
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/** Sección Comunidad: genera infografía PNG propia para WhatsApp/redes, sin assets del juego. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    countryId: String,
    zoneId: String,
    regionKey: String = "",
    onRegion: (String, String, String) -> Unit,
    onBack: () -> Unit = {},
    savedCommunityName: String? = null,
    onSaveCommunityName: (String) -> Unit = {},
    monthlySpecialDays: Map<Int, String>? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val revision = contentRevision()

    val country = Countries.byId(countryId)
    val chosenZone = Countries.zone(country.id, zoneId, regionKey)
    val safeZone = chosenZone.zoneId
    val zone: ZoneId = DexSchedule.zoneOf(safeZone)

    val sky = remember(revision, safeZone) { SkyCatalog.load(context, safeZone) }

    var selectedModeName by rememberSaveable { mutableStateOf(CommunityCardMode.DAILY.name) }
    val selectedMode = CommunityCardMode.valueOf(selectedModeName)

    var communityName by rememberSaveable(savedCommunityName) {
        mutableStateOf((savedCommunityName ?: CommunityNameStore.load(context)).take(CommunityNameStore.MAX_NAME_LENGTH))
    }

    var selectedWeatherName by rememberSaveable(safeZone) {
        mutableStateOf(weatherThemeFromSky(sky, selectedDay(), zone).name)
    }
    val selectedWeather = WeatherTheme.valueOf(selectedWeatherName)

    var monthToken by rememberSaveable { mutableStateOf(YearMonth.now(zone).toString()) }
    var selectedToken by rememberSaveable { mutableStateOf<String?>(null) }

    val today = LocalDate.now(zone)
    val selected = selectedToken?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
    val month = runCatching { YearMonth.parse(monthToken) }.getOrElse { YearMonth.from(selected) }
    val now = ZonedDateTime.now(zone)

    val todayFormatted = remember(selected) {
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
        selected.format(formatter).replaceFirstChar { it.uppercase() }
    }

    val resource = remember(selected) { ResourceCalendar.on(selected) }
    var plot1 by remember(selected) { mutableIntStateOf(toPlotNumber(resource.liuliu)) }
    var plot2 by remember(selected) { mutableIntStateOf(toPlotNumber(resource.flurita)) }

    val generatedMonthlySpecialDays = remember(month, safeZone, sky, today) {
        buildMonthlySpecialDays(month, zone, sky)
    }
    val specialDays = monthlySpecialDays ?: generatedMonthlySpecialDays

    val plotLabel1 = plotLabel(resource.liuliu, plot1)
    val plotLabel2 = plotLabel(resource.flurita, plot2)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Comunidad", fontWeight = FontWeight.Bold) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Genera una imagen PNG propia para tu comunidad. No usa logotipos oficiales ni assets del juego.",
                style = MaterialTheme.typography.bodySmall,
            )

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
                    val next = country.zones.first { it.key == key }
                    onRegion(country.id, next.zoneId, next.key)
                },
            )

            // Selector de plantilla
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CommunityCardMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = selectedMode == mode,
                        onClick = { selectedModeName = mode.name },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CommunityCardMode.entries.size),
                    ) {
                        Text(mode.label)
                    }
                }
            }

            // Campo para el nombre de la comunidad. Síncrono en memoria; persiste localmente.
            OutlinedTextField(
                value = communityName,
                onValueChange = { input ->
                    val clean = input.take(CommunityNameStore.MAX_NAME_LENGTH)
                    communityName = clean
                    CommunityNameStore.save(context, clean)
                    onSaveCommunityName(clean)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de tu comunidad (opcional)") },
                placeholder = { Text("Ej. Adultos Chiquitos") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            if (selectedMode == CommunityCardMode.DAILY) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeatherTheme.entries.forEach { weather ->
                        FilterChip(
                            selected = selectedWeather == weather,
                            onClick = { selectedWeatherName = weather.name },
                            label = { Text("${weather.emoji} ${weather.label}") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlotStepper(
                        label = "Árbol de Liuliu",
                        value = plot1,
                        onValueChange = { plot1 = it },
                        modifier = Modifier.weight(1f),
                    )
                    PlotStepper(
                        label = "Cristal Fluorita",
                        value = plot2,
                        onValueChange = { plot2 = it },
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { selectedToken = selected.minusDays(1).toString() }) { Text("Día anterior") }
                    TextButton(onClick = { selectedToken = selected.plusDays(1).toString() }) { Text("Día siguiente") }
                    TextButton(onClick = { selectedToken = today.toString(); monthToken = YearMonth.from(today).toString() }) { Text("Volver a hoy") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { monthToken = month.minusMonths(1).toString() }) { Text("Mes anterior") }
                    Text(
                        remember(month) { CommunityCalendarGridTitle(month) },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TextButton(onClick = { monthToken = month.plusMonths(1).toString() }) { Text("Mes siguiente") }
                }
            }

            // Lienzo capturable en alta nitidez (proporción 4:5 ideal para WhatsApp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(28.dp))
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    },
            ) {
                when (selectedMode) {
                    CommunityCardMode.DAILY -> DailyReportCard(
                        dateText = todayFormatted,
                        weather = selectedWeather,
                        spots = listOf(
                            DailyResourceSpot("Árbol de Liuliu", "🌳", plot1, plotLabel1),
                            DailyResourceSpot("Cristal de Fluorita", "💎", plot2, plotLabel2),
                        ),
                        communityName = communityName,
                    )
                    CommunityCardMode.MONTHLY -> MonthlyCalendarCard(
                        yearMonth = month,
                        specialDays = specialDays,
                        communityName = communityName,
                        today = today,
                    )
                }
            }

            var isExporting by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    if (!isExporting) {
                        isExporting = true
                        scope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                shareBitmapAsPng(context, bitmap, communityName)
                            } finally {
                                isExporting = false
                            }
                        }
                    }
                },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (isExporting) "Generando imagen..." else "Compartir imagen en comunidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            // No padding inferior adicional: el banner AdMob vive en bottomBar del Scaffold raíz (AGENTS).
        }
    }
}

enum class CommunityCardMode(val label: String) {
    DAILY("Reporte del Día"),
    MONTHLY("Calendario Mensual"),
}

enum class WeatherTheme(
    val label: String,
    val emoji: String,
    val gradient: List<Color>,
    val accent: Color,
) {
    SUNNY(
        label = "Soleado",
        emoji = "☀️",
        gradient = listOf(Color(0xFF6EC6FF), Color(0xFFA8E6CF), Color(0xFFFFF3B0)),
        accent = Color(0xFF2E7D32),
    ),
    RAINY(
        label = "Lluvia",
        emoji = "🌧️",
        gradient = listOf(Color(0xFF4A6FA5), Color(0xFF7FA6C9), Color(0xFFD6E6F2)),
        accent = Color(0xFF1E4E79),
    ),
    RAINBOW(
        label = "Arcoíris",
        emoji = "🌈",
        gradient = listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4), Color(0xFFA1C4FD)),
        accent = Color(0xFF6A3093),
    ),
}

data class DailyResourceSpot(
    val name: String,
    val emoji: String,
    val plotNumber: Int,
    val plotLabel: String,
)

@Composable
private fun PlotStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { onValueChange(if (value <= 1) 12 else value - 1) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp),
                ) { Text("-", fontWeight = FontWeight.Bold) }

                Text(
                    text = value.toString().padStart(2, '0'),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                )

                TextButton(
                    onClick = { onValueChange(if (value >= 12) 1 else value + 1) },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(32.dp),
                ) { Text("+", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun DailyReportCard(
    dateText: String,
    weather: WeatherTheme,
    spots: List<DailyResourceSpot>,
    communityName: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(weather.gradient))
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.88f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Color.White),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "REPORTE DIARIO • HEARTOPIA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = weather.accent,
                        letterSpacing = 1.sp,
                    )
                    Text(text = dateText, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F2937))
                }
                Surface(color = weather.accent.copy(alpha = 0.14f), shape = RoundedCornerShape(14.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(weather.emoji, fontSize = 18.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(text = weather.label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = weather.accent)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            spots.forEach { spot ->
                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(weather.accent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) { Text(spot.emoji, fontSize = 24.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(text = spot.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF1F2937))
                                Surface(color = Color(0xFFFFD54F), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = spot.plotLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF3E2723),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        MiniPlotGrid(activePlot = spot.plotNumber, highlightColor = weather.accent)
                    }
                }
            }
        }

        CardSignatureFooter(communityName = communityName, accentColor = weather.accent)
    }
}

@Composable
private fun MiniPlotGrid(activePlot: Int, highlightColor: Color) {
    val selected = activePlot.coerceIn(1, 12)
    Canvas(
        modifier = Modifier
            .size(width = 84.dp, height = 64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE8F5E9))
            .border(1.dp, highlightColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(6.dp),
    ) {
        val cols = 4
        val rows = 3
        val gap = 4.dp.toPx()
        val cellW = (size.width - gap * (cols - 1)) / cols
        val cellH = (size.height - gap * (rows - 1)) / rows

        for (index in 1..12) {
            val r = (index - 1) / cols
            val c = (index - 1) % cols
            val left = c * (cellW + gap)
            val top = r * (cellH + gap)
            val isSelected = index == selected

            drawRoundRect(
                color = if (isSelected) highlightColor else Color(0xFFA5D6A7),
                topLeft = Offset(left, top),
                size = Size(cellW, cellH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
            if (isSelected) {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = minOf(cellW, cellH) * 0.26f,
                    center = Offset(left + cellW / 2f, top + cellH / 2f),
                )
            }
        }
    }
}

@Composable
private fun MonthlyCalendarCard(
    yearMonth: YearMonth,
    specialDays: Map<Int, String>,
    communityName: String,
    today: LocalDate,
) {
    val monthTitle = remember(yearMonth) { CommunityCalendarGridTitle(yearMonth) }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7
    val accent = Color(0xFF5E35B1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC), Color(0xFFE0F7FA))))
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CALENDARIO DE CLIMA Y EVENTOS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    letterSpacing = 1.sp,
                )
                Text(text = monthTitle, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F2937))
            }
        }

        Surface(color = Color.White.copy(alpha = 0.92f), shape = RoundedCornerShape(18.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = accent,
                        )
                    }
                }
                for (row in 0 until totalCells / 7) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val dayNumber = index - firstDayOffset + 1
                            val inMonth = dayNumber in 1..daysInMonth
                            val date = if (inMonth) yearMonth.atDay(dayNumber) else null
                            val isToday = date == today
                            Surface(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                color = when {
                                    !inMonth -> Color.Transparent
                                    isToday -> accent.copy(alpha = 0.18f)
                                    else -> Color.White
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = if (inMonth) BorderStroke(if (isToday) 1.2.dp else 0.5.dp, accent.copy(alpha = if (isToday) 0.8f else 0.18f)) else null,
                            ) {
                                if (inMonth) {
                                    Column(modifier = Modifier.padding(3.dp)) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = if (isToday) FontWeight.Black else FontWeight.Medium,
                                            color = Color(0xFF1F2937),
                                        )
                                        specialDays[dayNumber]?.let { icons ->
                                            Text(
                                                text = icons,
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Clip,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        CardSignatureFooter(communityName = communityName, accentColor = accent)
    }
}

@Composable
private fun CardSignatureFooter(communityName: String, accentColor: Color) {
    Surface(color = Color.White.copy(alpha = 0.88f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = communityName.ifBlank { "Tu comunidad" },
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "WikiFan para Heartopia • Disponible en Google Play",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                color = accentColor.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun selectedDay(): LocalDate = LocalDate.now()

private fun toPlotNumber(raw: String?): Int = raw?.toIntOrNull()?.coerceIn(1, 12) ?: 1

private fun plotLabel(raw: String?, plotNumber: Int): String = when {
    raw == null -> ResourceCalendar.label(null)
    raw.toIntOrNull() != null -> "Parcela ${plotNumber.toString().padStart(2, '0')}"
    raw == "Bosque" -> ResourceCalendar.label("Bosque")
    raw == "Ruinas" -> ResourceCalendar.label("Ruinas")
    else -> raw
}

private fun CommunityCalendarGridTitle(month: YearMonth): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
    return month.atDay(1).format(formatter).replaceFirstChar { it.uppercase() }
}

private fun weatherThemeFromSky(sky: com.guiaheartopia.app.data.SkyTable, date: LocalDate, zone: ZoneId): WeatherTheme {
    val kind = SkyWeatherGameDay.frameClimatesForGameDay(sky, date, zone)
        .firstOrNull()?.kind?.climateDisplay() ?: SkyKind.DESPEJADO
    return when (kind) {
        SkyKind.LLUVIA -> WeatherTheme.RAINY
        SkyKind.ARCOIRIS -> WeatherTheme.RAINBOW
        else -> WeatherTheme.SUNNY
    }
}

private fun buildMonthlySpecialDays(
    month: YearMonth,
    zone: ZoneId,
    sky: com.guiaheartopia.app.data.SkyTable,
): Map<Int, String> = buildMap {
    for (day in 1..month.lengthOfMonth()) {
        val date = month.atDay(day)
        val resource = ResourceCalendar.on(date)
        val icons = mutableListOf<String>()
        if (resource.liuliu != null) icons += "🌳"
        if (resource.flurita != null) icons += "💎"
        SkyWeatherGameDay.frameClimatesForGameDay(sky, date, zone)
            .map { it.kind.climateDisplay() }
            .filter { it != SkyKind.SIN_DATOS && it != SkyKind.DESPEJADO }
            .map { it.emoji }
            .distinct()
            .forEach { icons += it }
        if (icons.isNotEmpty()) put(day, icons.distinct().joinToString(""))
    }
}
