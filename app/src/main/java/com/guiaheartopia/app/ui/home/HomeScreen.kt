package com.guiaheartopia.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guiaheartopia.app.domain.Countries
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

@Composable
fun HomeScreen(
    ui: GuideUi,
    onOpen: (String) -> Unit,
    onDismissReminderPrompt: () -> Unit = {},
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
        val seasons = remember(revision, zone) {
            val raw = com.guiaheartopia.app.data.AndroidContentLibrary(context)
                .effectiveBytes("calendar/eventos.json")
                ?.decodeToString()
            com.guiaheartopia.app.domain.ReminderPlan.activeSeasons(
                com.guiaheartopia.app.domain.ReminderPlan.parseEvents(raw),
                zone,
                java.time.ZonedDateTime.now(zone),
            )
        }
        seasons.forEach { season ->
            NotebookCard {
                Text(season.name, style = MaterialTheme.typography.titleMedium, color = Ink)
                season.remaining?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink) }
            }
        }
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GreenSoft)
                        .border(1.5.dp, GreenTint, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🏡", fontSize = 24.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("📍", fontSize = 12.sp)
                Text(
                    "${country.name} · ${Countries.zoneLabel(ui.profile.zoneId)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
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

        // Colección Heartodex
        val totalFound = ui.found.size
        val totalItems = catalog.size
        val collectionPercent = if (totalItems > 0) (totalFound.toFloat() / totalItems * 100).toInt() else 0

        PixelCard(container = Paper) {
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
                NowBadge("$available ahora")
            }

            Text(
                "$totalFound de $totalItems criaturas registradas ($collectionPercent%)",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )

            PixelProgress(
                if (totalItems == 0) 0f else totalFound.toFloat() / totalItems,
                color = Green,
                track = GreenSoft,
            )

            // Category summary pills
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val catIcons = mapOf(
                    "Peces" to "🐟",
                    "Pájaros" to "🐦",
                    "Insectos" to "🐛",
                )
                listOf("Peces", "Pájaros", "Insectos").forEach { cat ->
                    val items = byCategory[cat].orEmpty()
                    val foundCount = items.count { it.id in ui.found }
                    val icon = catIcons[cat] ?: "🐾"
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaperSubtle)
                            .border(1.dp, Line, RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(icon, fontSize = 16.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Muted,
                            maxLines = 1,
                        )
                        Text(
                            "$foundCount/${items.size}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GreenDark,
                        )
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaperSubtle)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("🐾", fontSize = 16.sp)
                    Text("Animales", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Muted, maxLines = 1)
                    Text(
                        "${ui.sighted.size}/${ui.animals.size}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GreenDark,
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

        Spacer(Modifier.height(8.dp))
    }
}
