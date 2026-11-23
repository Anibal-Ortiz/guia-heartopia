package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.licenseDataStore: DataStore<Preferences> by preferencesDataStore(name = "license")

data class LicenseState(
    val isPro: Boolean = false,
    val licenseKey: String = "",
    val activationId: String = "",
    val lastValidatedAt: Long = 0L,
    val loaded: Boolean = false,
) {
    val maskedKey: String
        get() {
            val suffix = licenseKey.takeLast(4)
            return if (licenseKey.length > 4) "••••-$suffix" else "••••"
        }
}

class LicensePreferences private constructor(context: Context) {
    private val store = context.applicationContext.licenseDataStore

    private val data = store.data.catch { error ->
        if (error is IOException) {
            emit(emptyPreferences())
        } else {
            throw error
        }
    }

    val isProFlow: Flow<Boolean> = data.map { it[KEY_IS_PRO] ?: false }

    val stateFlow: Flow<LicenseState> = data.map { prefs ->
        LicenseState(
            isPro = prefs[KEY_IS_PRO] ?: false,
            licenseKey = prefs[KEY_LICENSE_KEY].orEmpty(),
            activationId = prefs[KEY_ACTIVATION_ID].orEmpty(),
            lastValidatedAt = prefs[KEY_LAST_VALIDATED] ?: 0L,
            loaded = true,
        )
    }

    suspend fun activate(licenseKey: String, activationId: String, validatedAt: Long) {
        store.edit { prefs ->
            prefs[KEY_IS_PRO] = true
            prefs[KEY_LICENSE_KEY] = licenseKey
            prefs[KEY_ACTIVATION_ID] = activationId
            prefs[KEY_LAST_VALIDATED] = validatedAt
        }
    }

    suspend fun touchValidated(validatedAt: Long) {
        store.edit { prefs ->
            prefs[KEY_LAST_VALIDATED] = validatedAt
        }
    }

    suspend fun clear() {
        store.edit { prefs ->
            prefs[KEY_IS_PRO] = false
            prefs.remove(KEY_LICENSE_KEY)
            prefs.remove(KEY_ACTIVATION_ID)
            prefs.remove(KEY_LAST_VALIDATED)
        }
    }

    companion object {
        private val KEY_IS_PRO = booleanPreferencesKey("is_pro")
        private val KEY_LICENSE_KEY = stringPreferencesKey("license_key")
        private val KEY_ACTIVATION_ID = stringPreferencesKey("activation_id")
        private val KEY_LAST_VALIDATED = longPreferencesKey("last_validated_at")

        @Volatile
        private var instance: LicensePreferences? = null

        fun get(context: Context): LicensePreferences =
            instance ?: synchronized(this) {
                instance ?: LicensePreferences(context).also { instance = it }
            }
    }
}
