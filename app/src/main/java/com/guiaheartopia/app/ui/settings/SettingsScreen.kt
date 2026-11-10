package com.guiaheartopia.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.guiaheartopia.app.domain.ContentSyncResult
import com.guiaheartopia.app.data.VerifiedCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.StoreCopy
import com.guiaheartopia.app.domain.SampleIds
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlaySession
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    profile: PlayerProfile,
    adsMode: AdsMode,
    adsStatus: String,
    privacyOptions: Boolean,
    onSave: (PlayerProfile) -> Unit,
    onRegion: (String, String, String) -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onAdPrivacy: () -> Unit,
    onReminders: (ReminderPrefs) -> Unit,
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String, (ImportResult) -> Unit) -> Unit,
    onPhotoCredits: () -> Unit,
    scrollToReminders: Boolean = false,
) {
    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var session by rememberSaveable(profile.session) { mutableStateOf(profile.session) }
    val country = Countries.byId(profile.countryId)
    val scroll = rememberScrollState()
    var remindersY by remember { mutableIntStateOf(0) }
    androidx.compose.runtime.LaunchedEffect(scrollToReminders, remindersY) {
        if (scrollToReminders && remindersY > 0) {
            scroll.animateScrollTo((remindersY - 24).coerceAtLeast(0))
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        com.guiaheartopia.app.ui.components.SectionBanner(
            image = com.guiaheartopia.app.R.drawable.header_aficiones,
            title = "Ajustes",
            subtitle = "Contenido, anuncios y créditos",
        )
        Text(stringResource(R.string.app_full_name), style = MaterialTheme.typography.titleMedium)
        Text(StoreCopy.UNOFFICIAL, style = MaterialTheme.typography.bodyMedium)
        PixelButton("Política de privacidad", onClick = onPrivacy, outlined = true)
        ContentUpdateCard()
        PixelCard {
            Text("Horarios", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tramos de criaturas: servidor UTC fijo; en Chile continental (UTC−3) " +
                    "Noche 02–08, Amanecer 08–14, Día 14–20, Tarde 20–02.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Text("Cuándo sueles jugar", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlaySession.entries.forEach { option ->
                FilterChip(selected = session == option, onClick = { session = option }, label = { Text(option.label) })
            }
        }
        PixelButton("Guardar perfil", onClick = {
            if (name.isNotBlank()) onSave(profile.copy(name = name.trim(), session = session))
        })
        PixelCard {
            Text("Calendario", style = MaterialTheme.typography.titleMedium)
            PickerField(
                "País",
                country.name,
                Countries.all.map { it.id to it.name },
                onPick = { id ->
                    val next = Countries.byId(id)
                    onRegion(id, next.zones.first().zoneId, SampleIds.WEATHER_ORIGIN)
                },
            )
            PickerField(
                "Zona",
                country.zones.firstOrNull { it.zoneId == profile.zoneId }?.label ?: country.zones.first().label,
                country.zones.map { it.zoneId to it.label },
                onPick = { onRegion(country.id, it, SampleIds.WEATHER_ORIGIN) },
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
        }
        androidx.compose.foundation.layout.Box(
            Modifier.onGloballyPositioned { remindersY = it.positionInParent().y.toInt() },
        ) {
            ReminderBlock(profile.reminders, onReminders)
        }
        BackupBlock(onExport, onImport)
        PixelButton("Política de privacidad", onClick = onPrivacy, outlined = true)
        PixelButton("Términos", onClick = onTerms, outlined = true)
        com.guiaheartopia.app.ui.components.TwigDivider(1)
        PixelCard {
            Text("Créditos y fuentes", style = MaterialTheme.typography.titleMedium)
            Text(VerifiedCatalog.SPOT_SOURCES, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Peces, aves, insectos y fauna salen de las páginas índice en español de heartodex, leídas el 7 de noviembre de 2026. El índice no trae precio ni cebo. Nivel y sombra se muestran solo cuando la tarjeta los incluye. El texto breve de cada ficha está escrito para esta app.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Las fotos son de especies reales, con licencias libres (CC BY, CC0 o dominio público), tomadas de Wikimedia Commons e iNaturalist. Las criaturas inventadas del juego usan la foto de su especie base.",
                style = MaterialTheme.typography.bodyMedium,
            )
            PixelButton(text = "Créditos de las fotos", onClick = onPhotoCredits, outlined = true)
            Text(
                "Liuliu y la fluorita de noviembre 2026 salen de heartodex (ubicación diaria). Diciembre no está día a día en esa página: se muestra Sin datos.",
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
        }
        Text("Versión ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ContentUpdateCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf(AndroidContentStore.status(context)) }
    var busy by remember { mutableStateOf(false) }
    val checkState by ContentCheckStore.observe(context).collectAsStateWithLifecycle(
        com.guiaheartopia.app.data.ContentCheckState(),
    )
    PixelCard {
        Text("Contenido", style = MaterialTheme.typography.titleMedium)
        Text("Versión de contenido: ${status.contentVersion}", style = MaterialTheme.typography.bodyMedium)
        Text(
            "Última actualización: ${publishedLabel(status.publishedOn)}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            ContentCheckStore.lastCheckedLabel(checkState.lastCheckedMillis),
            style = MaterialTheme.typography.bodySmall,
        )
        if (busy) {
            CircularProgressIndicator(modifier = Modifier.padding(vertical = 4.dp))
        }
        PixelButton(
            text = "Buscar actualizaciones",
            modifier = Modifier.alpha(if (busy) 0.45f else 1f),
            onClick = {
                if (!busy) {
                    busy = true
                    scope.launch {
                        val report = withContext(Dispatchers.IO) { AndroidContentStore.syncNow(context) }
                        val now = System.currentTimeMillis()
                        ContentCheckStore.record(context, report, now)
                        if (report.result == ContentSyncResult.APPLIED) {
                            ContentSignals.note(context)
                        }
                        status = AndroidContentStore.status(context)
                        busy = false
                    }
                }
            },
        )
        if (checkState.lastMessage.isNotBlank()) {
            Text(checkState.lastMessage, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun publishedLabel(iso: String): String {
    val parts = iso.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else iso
}

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
            (next.temporadaEnabled && !current.temporadaEnabled)
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
                "La temporada avisa al empezar y un día antes de terminar. " +
                "El sistema puede retrasar el aviso. No pedimos una alarma exacta.",
            style = MaterialTheme.typography.bodySmall,
        )
        ToggleRow("Clima", current.weatherEnabled) { update(current.copy(weatherEnabled = it)) }
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
            "Compilación de prueba: los anuncios usan identificadores de demostración. No son de un editor real."
        !BuildConfig.ADS_ENABLED ->
            "Esta compilación no pide anuncios."
        else ->
            "Esta compilación tiene los anuncios configurados fuera del repositorio."
    }
    return listOf(base, status).filter { it.isNotBlank() }.joinToString(" ")
}
