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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.BuildConfig
import com.guiaheartopia.app.ads.AdsMode
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlaySession
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.ui.components.PickerField
import com.guiaheartopia.app.ui.components.PixelButton
import com.guiaheartopia.app.ui.components.PixelCard
import com.guiaheartopia.app.ui.components.VerifyBadge

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
) {
    var name by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var place by rememberSaveable(profile.placeName) { mutableStateOf(profile.placeName) }
    var session by rememberSaveable(profile.session) { mutableStateOf(profile.session) }
    val country = Countries.byId(profile.countryId)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineSmall)
        VerifyBadge("Guía independiente")
        Text(
            "No está afiliada a los creadores de Heartopia. Los datos de ejemplo siguen pendientes de verificar.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(place, { place = it }, label = { Text("Tu lugar") }, modifier = Modifier.fillMaxWidth())
        Text("Cuándo sueles jugar", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlaySession.entries.forEach { option ->
                FilterChip(selected = session == option, onClick = { session = option }, label = { Text(option.label) })
            }
        }
        PixelButton("Guardar perfil", onClick = {
            if (name.isNotBlank()) onSave(profile.copy(name = name.trim(), placeName = place.trim(), session = session))
        })
        PixelCard {
            Text("Calendario", style = MaterialTheme.typography.titleMedium)
            PickerField(
                "País",
                country.name,
                Countries.all.map { it.id to it.name },
                onPick = { id ->
                    val next = Countries.byId(id)
                    onRegion(id, next.zones.first().zoneId, profile.weatherOriginId)
                },
            )
            PickerField(
                "Zona",
                profile.zoneId,
                country.zones.map { it.zoneId to "${it.label} · ${it.zoneId}" },
                onPick = { onRegion(country.id, it, profile.weatherOriginId) },
            )
        }
        PixelCard {
            Text("Anuncios y consentimiento", style = MaterialTheme.typography.titleMedium)
            Text(adsCopy(adsMode, adsStatus), style = MaterialTheme.typography.bodyMedium)
            Text(
                "El mensaje GDPR/UMP tiene que publicarse en la consola de AdMob antes de llegar a usuarios del EEE. Esta app no simula que ya aceptaste.",
                style = MaterialTheme.typography.bodySmall,
            )
            if (privacyOptions) {
                PixelButton("Opciones de privacidad de anuncios", onClick = onAdPrivacy)
            }
        }
        ReminderBlock(profile.reminders, onReminders)
        BackupBlock(onExport, onImport)
        PixelButton("Política de privacidad", onClick = onPrivacy, outlined = true)
        PixelButton("Términos", onClick = onTerms, outlined = true)
        Text("Versión ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall)
    }
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
    fun update(next: ReminderPrefs) {
        val turningOn = (next.routineEnabled || next.weatherEnabled || next.eventsEnabled) &&
            !(current.routineEnabled || current.weatherEnabled || current.eventsEnabled)
        if (turningOn && Build.VERSION.SDK_INT >= 33) {
            pending = next
            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onChange(next)
        }
    }
    PixelCard {
        Text("Recordatorios", style = MaterialTheme.typography.titleMedium)
        Text(
            "Avisos locales a la hora de tu zona. WorkManager puede retrasarlos unos minutos. No pedimos alarma exacta.",
            style = MaterialTheme.typography.bodySmall,
        )
        ToggleRow("Rutina", current.routineEnabled) { update(current.copy(routineEnabled = it)) }
        ToggleRow("Clima y recursos", current.weatherEnabled) { update(current.copy(weatherEnabled = it)) }
        ToggleRow("Eventos", current.eventsEnabled) { update(current.copy(eventsEnabled = it)) }
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
        Text("Exporta o recupera perfil, rutina, colección, hobbies y ajustes. El archivo es JSON versión 1.", style = MaterialTheme.typography.bodySmall)
        PixelButton("Exportar JSON", onClick = { create.launch("guia-heartopia-respaldo.json") }, outlined = true)
        PixelButton("Importar JSON", onClick = { open.launch(arrayOf("application/json", "text/*")) }, outlined = true)
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
            "Compilación de depuración con los ID oficiales de prueba de Google. No son anuncios de un editor real."
        !BuildConfig.ADS_ENABLED ->
            "Esta compilación no tiene IDs de AdMob. No se solicitan anuncios. Los reales se configuran fuera del repositorio."
        else ->
            "Esta compilación tiene IDs configurados fuera del repositorio."
    }
    return listOf(base, status).filter { it.isNotBlank() }.joinToString(" ")
}
