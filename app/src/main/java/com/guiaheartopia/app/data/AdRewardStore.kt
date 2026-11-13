package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.adRewardStore by preferencesDataStore(name = "ad_rewards")

data class AdRewardState(
    val adFreeUntilMillis: Long = 0L,
    val calendarUntilMillis: Long = 0L,
    val loaded: Boolean = false,
) {
    val adFreeActive: Boolean get() = loaded && adFreeUntilMillis > System.currentTimeMillis()
    val calendarUnlocked: Boolean get() = loaded && calendarUntilMillis > System.currentTimeMillis()

    /** Hasta leer DataStore no se muestra nada: el valor inicial no es «sin beneficio». */
    val suppressAds: Boolean get() = !loaded || adFreeUntilMillis > System.currentTimeMillis()
}

object AdRewardStore {
    private val KEY_AD_FREE = longPreferencesKey("ad_free_until_ms")
    private val KEY_CALENDAR = longPreferencesKey("calendar_until_ms")

    fun observe(context: Context): Flow<AdRewardState> =
        context.applicationContext.adRewardStore.data.map { prefs ->
            AdRewardState(
                adFreeUntilMillis = prefs[KEY_AD_FREE] ?: 0L,
                calendarUntilMillis = prefs[KEY_CALENDAR] ?: 0L,
                loaded = true,
            )
        }

    suspend fun grantAdFreeHours(context: Context, hours: Long = 24) {
        val until = Instant.now().plus(Duration.ofHours(hours)).toEpochMilli()
        context.applicationContext.adRewardStore.edit { it[KEY_AD_FREE] = until }
    }

    suspend fun grantCalendarUntilServerDay(context: Context, zoneId: String, now: Instant = Instant.now()) {
        val until = SkyWeatherGameDay.nextServerDawn(now, DexSchedule.zoneOf(zoneId)).toEpochMilli()
        context.applicationContext.adRewardStore.edit { it[KEY_CALENDAR] = until }
    }

    fun remainingLabel(untilMillis: Long): String {
        val minutes = Duration.between(Instant.now(), Instant.ofEpochMilli(untilMillis)).toMinutes()
        if (minutes <= 0) return ""
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours} h ${mins} min" else "$mins min"
    }
}
