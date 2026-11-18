package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeModeStore by preferencesDataStore(name = "theme_mode")

/** Preferencia de apariencia: seguir al sistema, forzar claro o forzar oscuro. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemeModeStore {
    private val KEY_MODE = stringPreferencesKey("mode")

    fun observe(context: Context): Flow<ThemeMode> =
        context.applicationContext.themeModeStore.data.map { prefs ->
            val raw = prefs[KEY_MODE] ?: ThemeMode.SYSTEM.name
            runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
        }

    suspend fun set(context: Context, mode: ThemeMode) {
        context.applicationContext.themeModeStore.edit { prefs ->
            prefs[KEY_MODE] = mode.name
        }
    }
}
