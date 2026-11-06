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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guiaheartopia.app.BuildConfig
import com.guiaheartopia.app.ads.AdsMode
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.PlaySession
import com.guiaheartopia.app.domain.PlayerProfile
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
        PixelButton("Política de privacidad", onClick = onPrivacy, container = com.guiaheartopia.app.ui.theme.Peach)
        PixelButton("Términos", onClick = onTerms, container = com.guiaheartopia.app.ui.theme.Peach)
        Text("Versión ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall)
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
