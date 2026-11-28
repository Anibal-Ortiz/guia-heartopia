package com.guiaheartopia.app.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.guiaheartopia.app.data.AficionesCatalog
import com.guiaheartopia.app.data.AvatarStore
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.CatalogLabels
import com.guiaheartopia.app.domain.CatalogMarks
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.SiembraEntry
import com.guiaheartopia.app.ui.components.AficionesImage
import com.guiaheartopia.app.ui.components.CertificadoMarkIcon
import com.guiaheartopia.app.ui.components.ConseguidoMarkIcon
import com.guiaheartopia.app.ui.components.remember5SecondTick
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.SkyWeather
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.climateDisplay
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.CozyCrystalIcon
import com.guiaheartopia.app.ui.components.CozyGuideIcon
import com.guiaheartopia.app.ui.components.CozyHobbiesIcon
import com.guiaheartopia.app.ui.components.CozyHomeIcon
import com.guiaheartopia.app.ui.components.CozyTreeIcon
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.components.headerForTheme
import com.guiaheartopia.app.ui.components.NowBadge
import com.guiaheartopia.app.ui.components.NotebookCard
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.SoftCheck
import com.guiaheartopia.app.ui.components.rememberMinuteTick
import com.guiaheartopia.app.ui.weather.ClimateSkyBar
import com.guiaheartopia.app.ui.weather.WeatherAmbientBackground
import com.guiaheartopia.app.ui.weather.WeatherBarShimmer
import com.guiaheartopia.app.ui.weather.WeatherGlyphAnimated
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.DarkCard
import com.guiaheartopia.app.ui.theme.Green
import com.guiaheartopia.app.ui.theme.GreenDark
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GreenTint
import com.guiaheartopia.app.ui.theme.Honey
import com.guiaheartopia.app.ui.theme.HoneySoft
import com.guiaheartopia.app.ui.theme.HoneyTint
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.OnArtworkText
import com.guiaheartopia.app.ui.theme.Orange
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.theme.Peach
import com.guiaheartopia.app.ui.theme.PinkSoft
import com.guiaheartopia.app.ui.theme.Rose
import com.guiaheartopia.app.ui.theme.Sky
import com.guiaheartopia.app.ui.theme.SkyBlue
import com.guiaheartopia.app.ui.theme.SkyTint
import com.guiaheartopia.app.ui.theme.Yellow
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class DexColumnData(
    val category: String,
    val emoji: String,
    val showCertified: Boolean,
    val found: Int,
    val total: Int,
    val certified: Int,
)

@Composable
fun HomeScreen(
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onDismissReminderPrompt: () -> Unit = {},
    adFreeActive: Boolean = false,
    onWatchAdFree: () -> Unit = {},
    onPickAvatar: (Uri) -> Unit = {},
    onClearAvatar: () -> Unit = {},
    onCancelCrop: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val catalog = remember(revision) { com.guiaheartopia.app.data.CreatureCatalog.load(context) }
    val aficiones = remember(revision) { com.guiaheartopia.app.data.AficionesCatalog.load(context) }
    val sky = remember(revision) { com.guiaheartopia.app.data.SkyCatalog.load(context, ui.profile.zoneId) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val nowInstant = rememberMinuteTick()
    val available = remember(catalog, sky, zone, nowInstant) {
        catalog.count { SkyWeather.availableNow(it.schedule, it.weather, nowInstant, sky, zone) }
    }
    val byCategory = remember(catalog) { catalog.groupBy { it.category } }
    val today = remember(zone, nowInstant) { LocalDate.now(zone) }
    val climateDay = remember(nowInstant, zone) { SkyWeatherGameDay.serverDawnLocalDate(nowInstant, zone) }
    val climateDate = remember(climateDay) {
        climateDay.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))
    }
    val climateLines = remember(sky, climateDay, zone) {
        SkyWeatherGameDay.compactLinesForGameDay(sky, climateDay, zone)
    }
    val spot = remember(today) { ResourceCalendar.on(today) }
    val codesRaw = remember(revision) {
        com.guiaheartopia.app.data.AndroidContentLibrary(context)
            .effectiveBytes(RedeemCodes.PATH)
            ?.decodeToString()
    }
    val activeCodes = remember(codesRaw, today) { RedeemCodes.active(RedeemCodes.parse(codesRaw), today) }
    val unredeemedCodes = remember(activeCodes, ui.redeemedCodes) {
        RedeemCodes.unredeemed(activeCodes, ui.redeemedCodes)
    }
    val eventsDoc = remember(revision) { ReminderPlan.parseEvents(
        com.guiaheartopia.app.data.AndroidContentLibrary(context)
            .effectiveBytes("calendar/eventos.json")
            ?.decodeToString(),
    ) }
    val activeSeason = remember(eventsDoc, zone, nowInstant) {
        ReminderPlan.activeSeasons(eventsDoc, zone, nowInstant.atZone(zone)).firstOrNull()
    }
    // restore() escribe en disco: va en un efecto, nunca en la fase de composición.
    var avatarJpeg by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(ui.profile.avatarJpegBase64) {
        AvatarStore.restore(context, ui.profile.avatarJpegBase64)
        avatarJpeg = ui.profile.avatarJpegBase64
    }
    val avatarFile = remember(avatarJpeg) {
        AvatarStore.file(context).takeIf { avatarJpeg != null && it.isFile }
    }
    var avatarMenu by remember { mutableStateOf(false) }
    val pickAvatar = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) onPickAvatar(uri)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GreetingHeroBanner(
            greeting = ui.greeting,
            todayLabel = ui.todayLabel.replaceFirstChar { it.titlecase() },
            avatar = {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GreenSoft)
                        .border(1.5.dp, GreenTint, CircleShape)
                        .semantics { contentDescription = if (avatarFile == null) "Elegir foto de perfil" else "Cambiar foto de perfil" }
                        .clickable { avatarMenu = true },
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarFile != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(avatarFile).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        CozyHomeIcon(tint = GreenDark, size = 30.dp, filled = true)
                    }
                }
            },
        )

        if (avatarMenu) {
            AlertDialog(
                onDismissRequest = { avatarMenu = false },
                title = { Text("Foto de perfil") },
                text = { Text("Se guarda solo en el teléfono. Puedes cambiarla o quitarla.") },
                confirmButton = {
                    TextButton(onClick = {
                        avatarMenu = false
                        pickAvatar.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    }) { Text(if (avatarFile == null) "Elegir foto" else "Cambiar foto") }
                },
                dismissButton = {
                    Row {
                        if (avatarFile != null) {
                            TextButton(onClick = {
                                avatarMenu = false
                                onClearAvatar()
                            }) { Text("Quitar foto") }
                        }
                        TextButton(onClick = { avatarMenu = false }) { Text("Cancelar") }
                    }
                },
            )
        }
        activeSeason?.let { season ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(HoneySoft)
                    .border(1.dp, HoneyTint, RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("🎉", fontSize = 14.sp)
                Column {
                    Text(
                        season.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    season.remaining?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Muted)
                    }
                }
            }
        }

        if (unredeemedCodes.isNotEmpty()) {
            NotebookCard(modifier = Modifier.clickable { onOpen("codigos") }) {
                Text(
                    if (unredeemedCodes.size == 1) {
                        "Tienes un código vigente sin marcar como canjeado"
                    } else {
                        "Tienes ${unredeemedCodes.size} códigos vigentes sin marcar como canjeado"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }

        val reminders = ui.profile.reminders
        if (!reminders.anyAlertOn() && !reminders.promptDismissed) {
            NotebookCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "¿Quieres que te avise del clima y los eventos? Activa los recordatorios en Ajustes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "×",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable(onClick = onDismissReminderPrompt),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                }
                PixelButton(
                    "Ir a Ajustes",
                    onClick = { onOpen("settings/recordatorios") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Panel compacto de cultivos activos (solo los que no están listos) - clickable para ir a Huerto
        val nowMillis5s = remember5SecondTick().toEpochMilli()
        val activeCrops = ui.activeCrops.filter { it.readyAtEpochMilli > nowMillis5s }
        if (activeCrops.isNotEmpty()) {
            val aficiones = remember(revision) { AficionesCatalog.load(context) }
            val siembraById = remember(aficiones) { aficiones.siembras.associateBy { it.id } }
            PixelCard(
                container = Paper,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { onOpen("huerto") }
                    .semantics { contentDescription = "Ver cultivos en huerto" }
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🌱 Cultivos en curso", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Muted)
                }
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activeCrops.forEach { crop ->
                        val remaining = (crop.readyAtEpochMilli - nowMillis5s).coerceAtLeast(0)
                        val label = AficionesContent.formatGrowthMinutes((remaining / 60_000).toInt())
                        val siembra = siembraById[crop.siembraId]
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Miniatura de la siembra (como la planta genérica)
                            if (siembra != null) {
                                AficionesImage(
                                    imagen = siembra.imagen,
                                    sectionLabel = "Siembra",
                                    size = 40.dp,
                                    shapeRadius = 8.dp,
                                    contentDescription = crop.nombre,
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(crop.nombre, style = MaterialTheme.typography.bodyMedium, color = Ink)
                                Text("Listo en $label", style = MaterialTheme.typography.labelSmall, color = GreenDark)
                            }
                            // X para cancelar esta alarma (consuma el click, no navega)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable { onCancelCrop(crop.id) }
                                    .semantics { contentDescription = "Cancelar aviso de ${crop.nombre}" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("×", style = MaterialTheme.typography.titleMedium, color = Muted)
                            }
                        }
                    }
                }
            }
        }

        PixelCard(container = Paper) {
            // Se saca de las mismas franjas del día del servidor que la barra y el texto.
            // conditionAt() usa la fecha civil del JSON y a medianoche puede adelantarse
            // al tramo de nieve que todavía continúa hasta las 02:00.
            val currentKind = remember(sky, climateDay, zone, nowInstant) {
                SkyWeatherGameDay.frameClimatesForGameDay(sky, climateDay, zone)
                    .firstOrNull { frame ->
                        !nowInstant.isBefore(frame.start.toInstant()) &&
                            nowInstant.isBefore(frame.end.toInstant())
                    }
                    ?.kind?.climateDisplay() ?: SkyKind.SIN_DATOS
            }
            Box(Modifier.fillMaxWidth()) {
                WeatherAmbientBackground(currentKind, Modifier.matchParentSize())
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Clima de hoy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                            Text(climateDate, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        WeatherGlyphAnimated(currentKind, Modifier.size(30.dp))
                    }
                    if (climateLines.isEmpty()) {
                        Text("Sin datos de clima para hoy.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                            ClimateSkyBar(sky, climateDay, zone, Modifier.fillMaxWidth().height(20.dp))
                            WeatherBarShimmer(Modifier.matchParentSize())
                        }
                        Spacer(Modifier.height(6.dp))
                        com.guiaheartopia.app.ui.weather.ClimateDetailList(climateLines, showFrames = false)
                    }
                }
            }
        }

        // Live Game Time Widget
        GameTimeCard(zone.id)

        val totalItems = remember(catalog) { catalog.size }
        val totalFound = remember(catalog, ui.found) { catalog.count { it.id in ui.found } }
        val collectionPercent = remember(totalItems, totalFound) {
            if (totalItems > 0) (totalFound * 100 / totalItems) else 0
        }
        val certifiedTotal = remember(catalog, ui.certified) { catalog.count { it.id in ui.certified } }

        PixelCard(modifier = Modifier.testTag("coleccion"), container = Paper) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(HoneySoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        CozyGuideIcon(tint = Honey, size = 18.dp, filled = true)
                    }
                    Text(
                        "Tu Colección",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                }
                NowBadge(
                    text = "Disponibles ahora: $available",
                    onClick = { onOpen("guide/disponibles-ahora") },
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.semantics { contentDescription = "Capturados $totalFound de $totalItems" },
                ) {
                    ConseguidoMarkIcon(
                        on = totalFound > 0,
                        size = 22.dp,
                        modifier = Modifier.semantics { contentDescription = "Capturados" },
                    )
                    Text(
                        "$totalFound/$totalItems ($collectionPercent%)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.semantics { contentDescription = "Certificados $certifiedTotal" },
                ) {
                    CertificadoMarkIcon(
                        on = certifiedTotal > 0,
                        size = 22.dp,
                        modifier = Modifier.semantics { contentDescription = "Certificados" },
                    )
                    Text(
                        certifiedTotal.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }
            }

            PixelProgress(
                if (totalItems == 0) 0f else totalFound.toFloat() / totalItems,
                color = Green,
                track = GreenSoft,
            )

            val siembraIds = remember(aficiones) { aficiones.siembras.map { it.id }.toSet() }
            val florIds = remember(aficiones) { aficiones.flores.map { it.id }.toSet() }
            val recetaIds = remember(aficiones) { aficiones.recetas.map { it.id }.toSet() }
            val dexColumns = remember(byCategory, ui.found, ui.certified) {
                listOf(
                    Triple("Peces", "🐟", true),
                    Triple("Pájaros", "🐦", true),
                    Triple("Insectos", "🐛", true),
                ).map { (cat, icon, cert) ->
                    val items = byCategory[cat].orEmpty()
                    val tally = CatalogMarks.tally(cat, items.map { it.id }, ui.found, ui.certified)
                    DexColumnData(cat, icon, cert, tally.found, tally.total, tally.certified)
                }
            }
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dexColumns.forEach { col ->
                        CollectionColumn(
                            emoji = col.emoji,
                            name = CatalogLabels.category(col.category),
                            captured = col.found,
                            total = col.total,
                            certified = if (col.showCertified) col.certified else null,
                            onClick = { onOpen("guide/categoria/${col.category}") },
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CollectionColumn(
                        emoji = "🌱",
                        name = "Siembra",
                        captured = ui.huertoFound.count { it in siembraIds },
                        total = aficiones.siembras.size,
                        certified = ui.huertoCertified.count { it in siembraIds },
                        onClick = { onOpen("huerto") },
                    )
                    CollectionColumn(
                        emoji = "🌸",
                        name = "Flores",
                        captured = ui.florFound.count { it in florIds },
                        total = aficiones.flores.size,
                        certified = ui.florCertified.count { it in florIds },
                        onClick = { onOpen("huerto/flores") },
                    )
                    CollectionColumn(
                        emoji = "🍳",
                        name = "Cocina",
                        captured = ui.recetaFound.count { it in recetaIds },
                        total = aficiones.recetas.size,
                        certified = ui.recetaCertified.count { it in recetaIds },
                        onClick = { onOpen("cocina") },
                    )
                }
            }

            PixelButton(
                "Explorar Catálogo →",
                onClick = { onOpen("guide") },
                container = Green,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        com.guiaheartopia.app.ui.components.TwigDivider(0)
        PixelCard(container = Paper) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Peach),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("💎", fontSize = 15.sp)
                }
                Text(
                    "Recursos Especiales Hoy",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Fluorita Card
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GreenSoft)
                        .border(1.dp, GreenTint, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CozyCrystalIcon(tint = GreenDark, size = 18.dp)
                        Text("Fluorita", style = MaterialTheme.typography.titleSmall, color = GreenDark)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        ResourceCalendar.label(spot.flurita),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }

                // Liuliu Card
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HoneySoft)
                        .border(1.dp, HoneyTint, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CozyTreeIcon(tint = Honey, size = 18.dp)
                        Text("Liuliu", style = MaterialTheme.typography.titleSmall, color = Honey)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        ResourceCalendar.label(spot.liuliu),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }
            }

        }

        if (!adFreeActive) {
            NotebookCard {
                Text(
                    "Cada video elimina los anuncios por 3 horas (acumulable hasta 12 horas).",
                    style = MaterialTheme.typography.bodySmall,
                )
                PixelButton(
                    "Ver video · +3h sin anuncios",
                    onClick = onWatchAdFree,
                    outlined = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        NotebookCard {
            PixelButton(
                "Configuración de alertas y recordatorios",
                onClick = { onOpen("settings/recordatorios") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GreetingHeroBanner(
    greeting: String,
    todayLabel: String,
    avatar: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Cream),
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                headerForTheme(com.guiaheartopia.app.R.drawable.header_inicio),
            ),
            contentDescription = null,
            // FillWidth + anclado abajo: se ve el cuaderno completo sin recortarlo.
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SingleLineShrinkingGreeting(greeting)
                Text(
                    todayLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnArtworkText,
                )
            }
            avatar()
        }
    }
}

@Composable
private fun SingleLineShrinkingGreeting(
    greeting: String,
    minFontSize: TextUnit = 13.sp,
) {
    val baseSize = MaterialTheme.typography.titleLarge.fontSize
    val baseStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        // Se mide una sola vez por (texto, ancho) con búsqueda binaria, en vez de reducir
        // 0.5 sp en cada onTextLayout (muchas recomposiciones para textos largos).
        val fontSize = remember(greeting, baseSize, minFontSize, maxWidthPx, baseStyle) {
            var low = minFontSize.value
            var high = baseSize.value
            repeat(10) {
                val mid = (low + high) / 2f
                val width = measurer.measure(
                    text = AnnotatedString(greeting),
                    style = baseStyle.copy(fontSize = mid.sp),
                ).size.width
                if (width <= maxWidthPx) low = mid else high = mid
            }
            low.sp
        }
        Text(
            greeting,
            style = baseStyle.copy(fontSize = fontSize),
            color = OnArtworkText,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CollectionColumn(
    emoji: String,
    name: String,
    captured: Int,
    total: Int,
    certified: Int?,
    onClick: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperSubtle)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = Muted,
            textAlign = TextAlign.Center,
            softWrap = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.semantics { contentDescription = "Capturados $captured de $total" },
        ) {
            ConseguidoMarkIcon(
                on = captured > 0,
                size = 16.dp,
                modifier = Modifier.semantics { contentDescription = "Capturados" },
            )
            val pct = if (total > 0) captured * 100 / total else 0
            Text(
                "$captured/$total ($pct%)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GreenDark,
            )
        }
        if (certified != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.semantics { contentDescription = "Certificados $certified" },
            ) {
                CertificadoMarkIcon(
                    on = certified > 0,
                    size = 16.dp,
                    modifier = Modifier.semantics { contentDescription = "Certificados" },
                )
                Text(
                    certified.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GreenDark,
                )
            }
        }
    }
}
