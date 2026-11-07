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
                country.zones.firstOrNull { it.zoneId == profile.zoneId }?.label ?: country.zones.first().label,
                country.zones.map { it.zoneId to it.label },
                onPick = { onRegion(country.id, it, profile.weatherOriginId) },
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
        ReminderBlock(profile.reminders, onReminders)
        BackupBlock(onExport, onImport)
        PixelButton("Política de privacidad", onClick = onPrivacy, outlined = true)
        PixelButton("Términos", onClick = onTerms, outlined = true)
        PixelCard {
            Text("Créditos y fuentes", style = MaterialTheme.typography.titleMedium)
            Text(
                "Guía Heartopia no está afiliada a quienes crean el juego. No lo controla ni pinta la pantalla por ti.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Peces, aves, insectos y fauna salen de las páginas índice en español de heartodex, leídas el 7 de noviembre de 2026. No se abrió cada ficha ni se copiaron imágenes. El índice no trae precio ni cebo. Nivel y sombra se muestran solo cuando la tarjeta los incluye.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "En noviembre de 2026, las parcelas de flurita y Liuliu coinciden en Tidebell y heartodex. La noche de la adivinación usa el cierre del 21 de noviembre cuando las listas no coinciden.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Las comidas de la fauna coinciden en tres guías y aquí están en español. La letra de la interfaz es Plus Jakarta Sans, licencia OFL. La paleta y los tamaños de la cuadrícula son una referencia aproximada, no el lienzo del juego.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
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
            "Avisos locales a la hora de tu zona. El sistema puede retrasarlos unos minutos. No pedimos una alarma exacta.",
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
        Text("Exporta o recupera perfil, rutina, colección, aficiones y ajustes. El archivo es un respaldo de la versión 1.", style = MaterialTheme.typography.bodySmall)
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
