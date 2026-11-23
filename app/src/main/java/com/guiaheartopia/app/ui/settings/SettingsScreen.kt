package com.guiaheartopia.app.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import com.guiaheartopia.app.BuildConfig
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ads.AdsMode
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiaheartopia.app.data.AndroidContentStore
import com.guiaheartopia.app.data.ContentCheckStore
import com.guiaheartopia.app.data.ContentSignals
import com.guiaheartopia.app.domain.ContentDownloadProgress
import com.guiaheartopia.app.domain.ContentSyncResult
import com.guiaheartopia.app.data.VerifiedCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.StoreCopy
import com.guiaheartopia.app.domain.SampleIds
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.ui.components.AdFreeRewardCard
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    adsMode: AdsMode,
    adsStatus: String,
    privacyOptions: Boolean,
    onRegion: (String, String, String, String) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onAdPrivacy: () -> Unit,
    onReminders: (ReminderPrefs) -> Unit,
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String, (ImportResult) -> Unit) -> Unit,
    scrollToReminders: Boolean = false,
    adFreeUntilMillis: Long = 0L,
    isPro: Boolean = false,
    onWatchAdFree: () -> Unit = {},
    onRetryAds: () -> Unit = {},
) {
    val country = Countries.byId(profile.countryId)
    val scroll = rememberScrollState()
    var remindersY by remember { mutableIntStateOf(0) }
    // El flag se consume una sola vez: si no, cualquier recomposición volvería a saltar.
    var autoScrollConsumed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(scrollToReminders, remindersY, autoScrollConsumed) {
        if (scrollToReminders && !autoScrollConsumed && remindersY > 0) {
            autoScrollConsumed = true
            scroll.animateScrollTo((remindersY - 24).coerceAtLeast(0))
        }
    }
    Column(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(Modifier.graphicsLayer { clip = false }.padding(16.dp)) {
            com.guiaheartopia.app.ui.components.SectionBanner(
                image = com.guiaheartopia.app.R.drawable.header_ajustes,
                title = "Ajustes",
                subtitle = "Contenido, anuncios y créditos",
                imageOffsetY = 96.dp,
                imageScale = 2.35f,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Text(stringResource(R.string.app_full_name), style = MaterialTheme.typography.titleMedium)
        Text(StoreCopy.UNOFFICIAL, style = MaterialTheme.typography.bodyMedium)
        ContentUpdateCard()
        PixelCard {
            Text("Horarios", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tramos de criaturas: servidor UTC fijo; en Chile continental (UTC−3) " +
                    "Noche 02–08, Amanecer 08–14, Día 14–20, Tarde 20–02.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        PixelCard {
            Text("Calendario", style = MaterialTheme.typography.titleMedium)
            PickerField(
                "País",
                country.name,
                Countries.all.map { it.id to it.name },
                onPick = { id ->
                    val next = Countries.byId(id)
                    val zone = next.zones.first()
                    onRegion(id, zone.zoneId, SampleIds.WEATHER_ORIGIN, zone.key)
                },
            )
            PickerField(
                if (country.zones.size > 1) "Estado o región" else "Zona",
                Countries.label(country.id, profile.zoneId, profile.regionKey),
                country.zones.map { it.key to it.label },
                onPick = { key ->
                    val zone = country.zones.first { it.key == key }
                    onRegion(country.id, zone.zoneId, SampleIds.WEATHER_ORIGIN, zone.key)
                },
            )
        }
        PixelCard {
            Text("Anuncios y consentimiento", style = MaterialTheme.typography.titleMedium)
            Text(adsCopy(adsMode, adsStatus), style = MaterialTheme.typography.bodyMedium)
            Text(
                "El aviso de privacidad de los anuncios tiene que estar publicado antes de llegar a Europa. La app no da por aceptado el consentimiento.",
                style = MaterialTheme.typography.bodySmall,
            )
            if (privacyOptions) {
                PixelButton("Opciones de privacidad de anuncios", onClick = onAdPrivacy)
            }
            if (adsMode == AdsMode.FAILED || adsMode == AdsMode.NOT_CONSENTED) {
                PixelButton("Reintentar consentimiento de anuncios", onClick = onRetryAds, outlined = true)
            }
        }
        AccessCodeSection()
        if (!isPro) {
            AdFreeRewardCard(
                untilMillis = adFreeUntilMillis,
                onWatchAdFree = onWatchAdFree,
            )
        }
        androidx.compose.foundation.layout.Box(
            Modifier.onGloballyPositioned { remindersY = it.positionInParent().y.toInt() },
        ) {
            ReminderBlock(profile.reminders, onReminders)
        }
        BackupBlock(onExport, onImport)
        com.guiaheartopia.app.ui.components.TwigDivider(1)
        PixelCard {
            Text("Créditos y fuentes", style = MaterialTheme.typography.titleMedium)
            Text(VerifiedCatalog.SPOT_SOURCES, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Peces, aves, insectos y fauna: nombres, lugares, horarios y niveles recopilados para esta guía. El índice no trae precio ni cebo. Nivel y sombra se muestran solo cuando la ficha los incluye. El texto breve de cada ficha está escrito para esta app.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Las imágenes de la guía se generan para esta app. Mientras se publican las nuevas, las fichas muestran un dibujo genérico.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Liuliu y la fluorita de noviembre de 2026 siguen el ciclo diario publicado por Tidebell. Diciembre no está día a día en la fuente: se muestra Sin datos.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Clima de 2026, hora a hora, leído el 10 de noviembre de 2026 en tidebell.com/calendar. " +
                    "heartopia.lol/en/weather repite las mismas 366 series. heartopia.th.gl/forecast llama a esas horas «hora de servidor» " +
                    "(América UTC−5, Global UTC+1, SEA UTC+7, TW/HK/MO UTC+8, Asia UTC+9); el 1 de enero de 2026 coincide con Tidebell. " +
                    "Tidebell no las documenta como UTC: las aplica a la fecha y hora locales. La app las muestra en tu zona IANA. " +
                    "Solo entra 2026. El sitio dice que el ciclo se repite; eso no se usa para otros años. Son datos de la comunidad, no oficiales.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Las comidas de la fauna coinciden en tres guías y aquí están en español. La letra de la interfaz es Plus Jakarta Sans, licencia OFL.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(
                    "Política de privacidad",
                    onClick = onPrivacy,
                    outlined = true,
                    modifier = Modifier.weight(1f),
                )
                PixelButton(
                    "Términos",
                    onClick = onTerms,
                    outlined = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Text("Versión ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ContentUpdateCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val busy by ContentSignals.busy().collectAsStateWithLifecycle()
    val checkState by ContentCheckStore.observe(context).collectAsStateWithLifecycle(
        com.guiaheartopia.app.data.ContentCheckState(),
    )
    val progress by ContentSignals.progress().collectAsStateWithLifecycle()
    // note() incrementa ticks tras cada APPLIED: esto refresca la versión al instante.
    val tick by ContentSignals.flow().collectAsStateWithLifecycle()
    val status = remember(tick) { AndroidContentStore.status(context) }
    val downloading = busy || progress != null

    val result = checkState.result
    val failed = result == ContentSyncResult.NETWORK_ERROR || result == ContentSyncResult.VERIFY_FAILED
    val needsNewer = result == ContentSyncResult.NEEDS_NEWER_APP

    val container = when {
        downloading -> MaterialTheme.colorScheme.surfaceVariant
        failed -> MaterialTheme.colorScheme.errorContainer
        needsNewer -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val onContainer = when {
        downloading -> MaterialTheme.colorScheme.onSurfaceVariant
        failed -> MaterialTheme.colorScheme.onErrorContainer
        needsNewer -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val accent = when {
        failed -> MaterialTheme.colorScheme.error
        needsNewer -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val glyph = when {
        downloading || needsNewer -> StatusGlyph.UPDATE
        failed -> StatusGlyph.ERROR
        else -> StatusGlyph.CHECK
    }
    val title = when {
        downloading -> "Descargando contenido…"
        failed -> "No se pudo actualizar"
        needsNewer -> "Actualización disponible"
        else -> "Contenido al día"
    }

    fun check() {
        if (busy) return
        ContentSignals.setBusy(true)
        scope.launch {
            try {
                val report = withContext(Dispatchers.IO) {
                    AndroidContentStore.syncNow(context) { ContentSignals.setProgress(it) }
                }
                ContentCheckStore.record(context, report, System.currentTimeMillis())
                if (report.result == ContentSyncResult.APPLIED) {
                    ContentSignals.note(context)
                }
            } finally {
                ContentSignals.setBusy(false)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = onContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    StatusIcon(glyph, accent, Modifier.size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "Versión ${status.contentVersion} · ${publishedLabel(status.publishedOn)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (downloading) {
                val shown = progress ?: ContentDownloadProgress(0, 0, 0L, 0L)
                val fraction = when {
                    shown.totalBytes > 0 -> shown.downloadedBytes.toFloat() / shown.totalBytes
                    shown.totalFiles > 0 -> shown.downloadedFiles.toFloat() / shown.totalFiles
                    else -> 0f
                }.coerceIn(0f, 1f)
                val percent = (fraction * 100).toInt()
                Text(
                    "Descargando... ${mb(shown.downloadedBytes)} / ${mb(shown.totalBytes)} MB" +
                        if (shown.totalBytes > 0 || shown.totalFiles > 0) " · $percent%" else "",
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = accent,
                    trackColor = onContainer.copy(alpha = 0.16f),
                )
            } else {
                val detail = if (failed || needsNewer) {
                    checkState.lastMessage
                } else {
                    ContentCheckStore.lastCheckedLabel(checkState.lastCheckedMillis)
                }
                if (detail.isNotBlank()) {
                    Text(detail, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!downloading) {
                if (failed) {
                    OutlinedButton(
                        onClick = { check() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = onContainer),
                    ) {
                        Text("Reintentar")
                    }
                } else {
                    TextButton(
                        onClick = { check() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = onContainer),
                    ) {
                        Text("Buscar actualizaciones")
                    }
                }
            }
        }
    }
}

private enum class StatusGlyph { CHECK, ERROR, UPDATE }

/** Iconos vectoriales dibujados en local para no añadir dependencias al proyecto. */
@Composable
private fun StatusIcon(glyph: StatusGlyph, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val radius = size.minDimension * 0.38f
        val centerPoint = center
        when (glyph) {
            StatusGlyph.CHECK -> {
                drawCircle(color = tint, radius = radius, style = Stroke(width = 1.6.dp.toPx()))
                drawLine(
                    color = tint,
                    start = Offset(centerPoint.x - radius * 0.45f, centerPoint.y),
                    end = Offset(centerPoint.x - radius * 0.08f, centerPoint.y + radius * 0.42f),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    color = tint,
                    start = Offset(centerPoint.x - radius * 0.08f, centerPoint.y + radius * 0.42f),
                    end = Offset(centerPoint.x + radius * 0.5f, centerPoint.y - radius * 0.42f),
                    strokeWidth = 2.dp.toPx(),
                )
            }

            StatusGlyph.ERROR -> {
                drawCircle(color = tint, radius = radius, style = Stroke(width = 1.6.dp.toPx()))
                drawLine(
                    color = tint,
                    start = Offset(centerPoint.x, centerPoint.y - radius * 0.5f),
                    end = Offset(centerPoint.x, centerPoint.y + radius * 0.22f),
                    strokeWidth = 2.dp.toPx(),
                )
                drawCircle(
                    color = tint,
                    radius = 1.6.dp.toPx(),
                    center = Offset(centerPoint.x, centerPoint.y + radius * 0.55f),
                )
            }

            StatusGlyph.UPDATE -> {
                val startAngle = -80f
                val sweep = 285f
                drawArc(
                    color = tint,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 1.8.dp.toPx()),
                )
                val endRad = Math.toRadians((startAngle + sweep).toDouble())
                val tip = Offset(
                    centerPoint.x + radius * cos(endRad).toFloat(),
                    centerPoint.y + radius * sin(endRad).toFloat(),
                )
                val tangentX = -sin(endRad).toFloat()
                val tangentY = cos(endRad).toFloat()
                val back = 6.dp.toPx()
                val side = 4.dp.toPx()
                val baseX = tip.x - back * tangentX
                val baseY = tip.y - back * tangentY
                val normalX = cos(endRad).toFloat()
                val normalY = sin(endRad).toFloat()
                drawPath(
                    path = Path().apply {
                        moveTo(tip.x, tip.y)
                        lineTo(baseX + side * normalX, baseY + side * normalY)
                        lineTo(baseX - side * normalX, baseY - side * normalY)
                        close()
                    },
                    color = tint,
                )
            }
        }
    }
}

private fun publishedLabel(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
}

private fun mb(bytes: Long): String =
    String.format(Locale.getDefault(), "%.1f", bytes / (1024f * 1024f))

@Composable
private fun ReminderBlock(current: ReminderPrefs, onChange: (ReminderPrefs) -> Unit) {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<ReminderPrefs?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val next = pending
        pending = null
        if (granted && next != null) onChange(next)
    }
    fun enabling(next: ReminderPrefs): Boolean =
        (next.weatherEnabled && !current.weatherEnabled) ||
            (next.pescaEnabled && !current.pescaEnabled) ||
            (next.avesEnabled && !current.avesEnabled) ||
            (next.insectosEnabled && !current.insectosEnabled) ||
            (next.temporadaEnabled && !current.temporadaEnabled) ||
            (next.eventsEnabled && !current.eventsEnabled)
    fun update(next: ReminderPrefs) {
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            enabling(next) &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pending = next
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onChange(next)
        }
    }
    PixelCard(modifier = Modifier.testTag("recordatorios")) {
        Text("Recordatorios", style = MaterialTheme.typography.titleMedium)
        Text(
            "El clima avisa 30 min antes de una franja que no sea despejado. " +
                "Pesca, aves e insectos avisan antes de cada hora del archivo de eventos. " +
                "La temporada avisa al empezar y un día antes de terminar.",
            style = MaterialTheme.typography.bodySmall,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(AlarmManager::class.java)
            if (manager != null && !manager.canScheduleExactAlarms()) {
                PixelButton(
                    text = "Permitir alertas puntuales",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            },
                        )
                    },
                    outlined = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        ToggleRow("Clima", current.weatherEnabled) { update(current.copy(weatherEnabled = it)) }
        ToggleRow("Avisos de eventos", current.eventsEnabled) {
            update(current.copy(eventsEnabled = it, eventsMasterTouched = true))
        }
        ToggleRow("Pesca", current.pescaEnabled) { update(current.copy(pescaEnabled = it)) }
        ToggleRow("Aves", current.avesEnabled) { update(current.copy(avesEnabled = it)) }
        ToggleRow("Insectos", current.insectosEnabled) { update(current.copy(insectosEnabled = it)) }
        ToggleRow("Eventos de temporada", current.temporadaEnabled) { update(current.copy(temporadaEnabled = it)) }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    RowLine(label) { Switch(checked, onChecked) }
}

@Composable
private fun RowLine(label: String, trailing: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label, modifier = androidx.compose.ui.Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        trailing()
    }
}

@Composable
private fun BackupBlock(
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String, (ImportResult) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        onExport { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                message = "Respaldo guardado."
            }.onFailure { message = "No se pudo guardar." }
        }
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val raw = runCatching {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        }.getOrNull()
        if (raw == null) message = "No se pudo leer el archivo." else pendingImport = raw
    }
    PixelCard {
        Text("Respaldo", style = MaterialTheme.typography.titleMedium)
        Text("Exporta o recupera perfil, colección, aficiones y ajustes. El archivo es un respaldo de la versión 1.", style = MaterialTheme.typography.bodySmall)
        PixelButton("Exportar respaldo", onClick = { create.launch("guia-heartopia-respaldo.json") }, outlined = true)
        PixelButton("Importar respaldo", onClick = { open.launch(arrayOf("application/json", "text/*")) }, outlined = true)
        message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
    val raw = pendingImport
    if (raw != null) {
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("¿Reemplazar el progreso?") },
            text = { Text("Se sobrescribe lo que hay en el teléfono con este archivo, si la versión es compatible.") },
            confirmButton = {
                TextButton(onClick = {
                    onImport(raw) { result ->
                        message = when (result) {
                            is ImportResult.Ok -> "Progreso restaurado."
                            is ImportResult.Invalid -> result.reason
                        }
                    }
                    pendingImport = null
                }) { Text("Reemplazar") }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancelar") } },
        )
    }
}

private fun adsCopy(mode: AdsMode, status: String): String {
    val base = when {
        BuildConfig.ADS_USE_GOOGLE_TEST_IDS ->
            "Los anuncios usan identificadores de demostración de Google (prueba o release sin IDs reales)."
        !BuildConfig.ADS_ENABLED ->
            "Esta compilación no pide anuncios."
        else ->
            "Esta compilación tiene los anuncios configurados fuera del repositorio."
    }
    return listOf(base, status).filter { it.isNotBlank() }.joinToString(" ")
}
