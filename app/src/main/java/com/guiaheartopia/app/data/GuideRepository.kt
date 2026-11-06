package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.PlayerProfile
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.guideStore by preferencesDataStore(name = "guia_heartopia")

class GuideRepository(
    context: Context,
    locale: Locale = Locale.getDefault(),
    deviceZone: ZoneId = ZoneId.systemDefault(),
) {
    private val dataStore = context.applicationContext.guideStore
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val key = stringPreferencesKey("snapshot")
    val defaults: GuideSnapshot

    init {
        val (country, zone) = Countries.resolve(locale.country, deviceZone)
        defaults = GuideSnapshot(profile = PlayerProfile(countryId = country, zoneId = zone))
    }

    val snapshots: Flow<GuideSnapshot> = dataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map defaults
        runCatching { json.decodeFromString(GuideSnapshot.serializer(), raw) }.getOrElse { defaults }
    }

    suspend fun update(block: (GuideSnapshot) -> GuideSnapshot) {
        dataStore.edit { prefs ->
            val current = prefs[key]?.let { raw ->
                runCatching { json.decodeFromString(GuideSnapshot.serializer(), raw) }.getOrNull()
            } ?: defaults
            prefs[key] = json.encodeToString(GuideSnapshot.serializer(), block(current))
        }
    }
}
