package com.guiaheartopia.app.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.share.CommunityCardFactory
import com.guiaheartopia.app.share.CommunityImageFilename
import com.guiaheartopia.app.share.CommunityImageKind
import com.guiaheartopia.app.share.CommunityImageRenderer
import com.guiaheartopia.app.share.CommunityImageSharer
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.contentRevision
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Módulo Comunidad: crea dos imágenes PNG para enviar a comunidades.
 * Una de calendario y otra de clima; ambas incluyen ubicaciones de Fluorita y Liuliu.
 */
@Composable
fun CommunityImageScreen(
    countryId: String,
    zoneId: String,
    regionKey: String = "",
    onRegion: (String, String, String) -> Unit,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val revision = contentRevision()

    val country = Countries.byId(countryId)
    val chosenZone = Countries.zone(country.id, zoneId, regionKey)
    val safeZone = chosenZone.zoneId
    val zone: ZoneId = DexSchedule.zoneOf(safeZone)

    var kindName by rememberSaveable { mutableStateOf(CommunityImageKind.CALENDAR.name) }
    var monthToken by rememberSaveable { mutableStateOf(YearMonth.now(zone).toString()) }
    var selectedToken by rememberSaveable { mutableStateOf<String?>(null) }

    val kind = CommunityImageKind.valueOf(kindName)
    val month = runCatching { YearMonth.parse(monthToken) }.getOrElse { YearMonth.now(zone) }
    val today = LocalDate.now(zone)
    val selected = selectedToken?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: today
    val now = ZonedDateTime.now(zone)

    val sky = remember(revision, safeZone) { SkyCatalog.load(context, safeZone) }

    val card = remember(kind, country, chosenZone, safeZone, month, selected, today, sky) {
        when (kind) {
            CommunityImageKind.CALENDAR -> CommunityCardFactory.calendarCard(
                country = country,
                chosenZone = chosenZone,
                zone = zone,
                month = month,
                today = today,
                selected = selected,
                now = now,
            )
            CommunityImageKind.WEATHER -> CommunityCardFactory.weatherCard(
                country = country,
                chosenZone = chosenZone,
                zone = zone,
                sky = sky,
                date = selected,
                now = now,
            )
        }
    }

    var rendered by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(card) {
        rendered = withContext(Dispatchers.Default) { CommunityImageRenderer.render(card) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Comunidad", style = MaterialTheme.typography.titleMedium)
        Text(
            "Crea una imagen para enviar a las comunidades: calendario o clima, con ubicaciones de Fluorita y Liuliu.",
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = kind == CommunityImageKind.CALENDAR,
                onClick = { kindName = CommunityImageKind.CALENDAR.name },
                label = { Text("Calendario") },
            )
            FilterChip(
                selected = kind == CommunityImageKind.WEATHER,
                onClick = { kindName = CommunityImageKind.WEATHER.name },
                label = { Text("Clima") },
            )
        }

        if (kind == CommunityImageKind.CALENDAR) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { monthToken = month.minusMonths(1).toString() }) { Text("Mes anterior") }
                Text(
                    card.monthTitle.ifBlank { month.toString() },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = { monthToken = month.plusMonths(1).toString() }) { Text("Mes siguiente") }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { selectedToken = selected.minusDays(1).toString() }) { Text("Día anterior") }
            TextButton(onClick = { selectedToken = selected.plusDays(1).toString() }) { Text("Día siguiente") }
            TextButton(onClick = {
                monthToken = YearMonth.from(today).toString()
                selectedToken = today.toString()
            }) { Text("Volver a hoy") }
        }

        PixelCard {
            Text(
                if (kind == CommunityImageKind.CALENDAR) "Imagen de calendario" else "Imagen de clima",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Día seleccionado: ${card.selectedDay.title} · Liuliu: ${card.selectedDay.liuliuLabel} · Fluorita: ${card.selectedDay.fluritaLabel}",
                style = MaterialTheme.typography.bodySmall,
            )
            val bmp = rendered
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Vista previa de la imagen para comunidades",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text("Preparando imagen…", style = MaterialTheme.typography.bodySmall)
            }
        }

        PixelButton(
            text = "Compartir imagen",
            onClick = {
                rendered?.let { bmp ->
                    val filename = CommunityImageFilename.forCard(card)
                    scope.launch { CommunityImageSharer.share(context, bmp, filename) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = rendered != null,
        )
        PixelButton(
            text = "Volver",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            outlined = true,
        )
        // No padding inferior adicional: el banner AdMob vive en bottomBar del Scaffold (AGENTS).
    }
}
