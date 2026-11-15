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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.guiaheartopia.app.data.AvatarStore
import com.guiaheartopia.app.domain.CatalogLabels
import com.guiaheartopia.app.domain.CatalogMarks
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.ui.components.CaptureNetIcon
import com.guiaheartopia.app.ui.components.CertificateMedalIcon
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.SkyWeather
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.CozyCrystalIcon
import com.guiaheartopia.app.ui.components.CozyGuideIcon
import com.guiaheartopia.app.ui.components.CozyHobbiesIcon
import com.guiaheartopia.app.ui.components.CozyHomeIcon
import com.guiaheartopia.app.ui.components.CozyTreeIcon
import com.guiaheartopia.app.ui.components.GameTimeCard
import com.guiaheartopia.app.ui.components.NowBadge
import com.guiaheartopia.app.ui.components.NotebookCard
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.PixelProgress
import com.guiaheartopia.app.ui.components.SoftCheck
import com.guiaheartopia.app.ui.weather.ClimateSkyBar
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

@Composable
fun HomeScreen(
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onDismissReminderPrompt: () -> Unit = {},
    adFreeActive: Boolean = false,
    onWatchAdFree: () -> Unit = {},
    onPickAvatar: (Uri) -> Unit = {},
    onClearAvatar: () -> Unit = {},
) {
    val country = Countries.byId(ui.profile.countryId)
    val context = androidx.compose.ui.platform.LocalContext.current
    val revision = com.guiaheartopia.app.ui.contentRevision()
    val catalog = remember(revision) { com.guiaheartopia.app.data.HeartodexCatalog.load(context) }
    val sky = remember(revision) { com.guiaheartopia.app.data.SkyCatalog.load(context) }
    val zone = DexSchedule.zoneOf(ui.profile.zoneId)
    val nowInstant = Instant.now()
    val available = catalog.count { SkyWeather.availableNow(it.schedule, it.weather, nowInstant, sky, zone) }
    val byCategory = catalog.groupBy { it.category }
    val today = LocalDate.now(zone)
    val climateDay = SkyWeatherGameDay.serverDawnLocalDate(nowInstant, zone)
    val climateLines = SkyWeatherGameDay.compactLinesForGameDay(sky, climateDay, zone)
    val spot = ResourceCalendar.on(today)
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
    val activeSeason = remember(eventsDoc, zone) {
        ReminderPlan.activeSeasons(eventsDoc, zone, ZonedDateTime.now(zone)).firstOrNull()
    }
    val avatarFile = remember(ui.profile.avatarJpegBase64) {
        AvatarStore.restore(context, ui.profile.avatarJpegBase64)
        AvatarStore.file(context).takeIf { ui.profile.avatarJpegBase64 != null && it.isFile }
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
        com.guiaheartopia.app.ui.components.SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_inicio,
            title = "Mi día",
            subtitle = "Horario, clima y lo que toca hoy",
        )
        PixelCard(container = PaperSubtle, elevation = 1.dp) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        ui.greeting,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Ink,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("📅", fontSize = 13.sp)
                        Text(
                            ui.todayLabel.replaceFirstChar { it.titlecase() },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Ink,
                        )
                    }
                }
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
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("📍", fontSize = 12.sp)
                Text(
                    "${country.name} · ${Countries.label(ui.profile.countryId, ui.profile.zoneId, ui.profile.regionKey)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

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

        PixelCard(container = Paper) {
            Text("Clima de hoy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
            if (climateLines.isEmpty()) {
                Text("Sin datos de clima para hoy.", style = MaterialTheme.typography.bodyMedium)
            } else {
                ClimateSkyBar(sky, climateDay, zone, Modifier.fillMaxWidth().height(20.dp))
                com.guiaheartopia.app.ui.weather.ClimateDetailList(climateLines, showFrames = false)
            }
        }

        // Live Game Time Widget
        GameTimeCard(zone.id)

        val totalItems = catalog.size
        val totalFound = catalog.count { it.id in ui.found }
        val collectionPercent = if (totalItems > 0) (totalFound * 100 / totalItems) else 0
        val certifiedTotal = catalog.count { it.id in ui.certified }

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
                    CaptureNetIcon(
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
                    CertificateMedalIcon(
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

            // Category summary pills
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("Peces", "🐟", true),
                    Triple("Pájaros", "🐦", true),
                    Triple("Insectos", "🐛", true),
                ).forEach { (cat, icon, cert) ->
                    val items = byCategory[cat].orEmpty()
                    val tally = CatalogMarks.tally(cat, items.map { it.id }, ui.found, ui.certified)
                    CollectionColumn(
                        emoji = icon,
                        name = CatalogLabels.category(cat),
                        captured = tally.found,
                        total = tally.total,
                        certified = if (cert) tally.certified else null,
                    )
                }
                CollectionColumn(
                    emoji = "🐾",
                    name = "Animales",
                    captured = ui.animals.count { it.id in ui.sighted },
                    total = ui.animals.size,
                    certified = null,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(
                    "Me faltan →",
                    onClick = { onOpen("guide/pendientes") },
                    outlined = true,
                    container = Green,
                    modifier = Modifier.weight(1f),
                )
                PixelButton(
                    "Explorar Catálogo →",
                    onClick = { onOpen("guide") },
                    container = Green,
                    modifier = Modifier.weight(1f),
                )
            }
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
                // Flurita Card
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
                        Text("Flurita", style = MaterialTheme.typography.titleSmall, color = GreenDark)
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
                PixelButton(
                    "Quitar anuncios 24 h con un video",
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
private fun androidx.compose.foundation.layout.RowScope.CollectionColumn(
    emoji: String,
    name: String,
    captured: Int,
    total: Int,
    certified: Int?,
) {
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperSubtle)
            .border(1.dp, Line, RoundedCornerShape(12.dp))
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
            CaptureNetIcon(
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
                CertificateMedalIcon(
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
