package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.PlayerProfile
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private val Context.guideStore by preferencesDataStore(name = "guia_heartopia")

class GuideRepository(
    context: Context,
    locale: Locale = Locale.getDefault(),
    deviceZone: ZoneId = ZoneId.systemDefault(),
) {
    private val dataStore = context.applicationContext.guideStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val key = stringPreferencesKey("snapshot")
    private val backupKey = stringPreferencesKey("snapshot_backup")
    private val _corruptSnapshot = MutableStateFlow(false)
    private val _corruptRaw = MutableSharedFlow<String>(replay = 1)

    /** true cuando el snapshot guardado no se pudo decodificar; la UI sigue recibiendo defaults. */
    val corruptSnapshot: StateFlow<Boolean> = _corruptSnapshot.asStateFlow()

    /** Contenido corrupto conservado para diagnóstico/recuperación. */
    val corruptRaw: SharedFlow<String> = _corruptRaw.asSharedFlow()

    val defaults: GuideSnapshot

    init {
        val (country, zone) = Countries.resolve(locale.country, deviceZone)
        defaults = GuideSnapshot(profile = PlayerProfile(countryId = country, zoneId = zone))
    }

    val snapshots: Flow<GuideSnapshot> = dataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map defaults
        val decoded = runCatching { json.decodeFromString(GuideSnapshot.serializer(), raw) }.getOrNull()
        if (decoded == null) {
            noteCorrupt(raw)
            return@map defaults
        }
        decoded
    }

    /** Guarda el raw corrupto bajo clave de respaldo sin romper el flujo de la UI. */
    private fun noteCorrupt(raw: String) {
        _corruptSnapshot.value = true
        _corruptRaw.tryEmit(raw)
        scope.launch {
            dataStore.edit { prefs ->
                if (!prefs.contains(backupKey)) prefs[backupKey] = raw
            }
        }
    }

    suspend fun replace(snapshot: GuideSnapshot) {
        dataStore.edit { prefs ->
            backupCorruptIn(prefs)
            prefs[key] = json.encodeToString(GuideSnapshot.serializer(), snapshot)
        }
        _corruptSnapshot.value = false
    }

    suspend fun update(block: (GuideSnapshot) -> GuideSnapshot) {
        dataStore.edit { prefs ->
            val raw = prefs[key]
            backupCorruptIn(prefs)
            val current = raw?.let { text ->
                runCatching { json.decodeFromString(GuideSnapshot.serializer(), text) }.getOrNull()
            } ?: defaults
            prefs[key] = json.encodeToString(GuideSnapshot.serializer(), block(current))
        }
    }

    /** Dentro de un edit: conserva el raw corrupto bajo la clave de respaldo. */
    private fun backupCorruptIn(prefs: MutablePreferences) {
        val raw = prefs[key] ?: return
        if (raw.isBlank()) return
        if (runCatching { json.decodeFromString(GuideSnapshot.serializer(), raw) }.isSuccess) return
        if (!prefs.contains(backupKey)) prefs[backupKey] = raw
    }
}
