package com.guiaheartopia.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.adRewardStore by preferencesDataStore(name = "ad_rewards")

data class AdRewardState(
    val adFreeUntilMillis: Long = 0L,
    val calendarUntilMillis: Long = 0L,
    val cropAlarmUntilMillis: Long = 0L,
    val loaded: Boolean = false,
) {
    val adFreeActive: Boolean get() = loaded && adFreeUntilMillis > System.currentTimeMillis()
    val calendarUnlocked: Boolean get() = loaded && calendarUntilMillis > System.currentTimeMillis()
    val cropAlarmUnlocked: Boolean get() = loaded && cropAlarmUntilMillis > System.currentTimeMillis()

    /** Hasta leer DataStore no se muestra nada: el valor inicial no es «sin beneficio». */
    val suppressAds: Boolean get() = !loaded || adFreeUntilMillis > System.currentTimeMillis()
}

object AdRewardStore {
    /** Cada video bonificado suma 3 horas. */
    const val AD_FREE_PER_VIDEO_HOURS = 3L

    /** Tope acumulable: 12 horas. */
    const val AD_FREE_MAX_HOURS = 12L

    /** Solo se puede ver otro video mientras queden 9 horas o menos. */
    const val AD_FREE_ADD_MORE_HOURS = 9L

    const val AD_FREE_PER_VIDEO_MILLIS = AD_FREE_PER_VIDEO_HOURS * 3_600_000L
    const val AD_FREE_MAX_MILLIS = AD_FREE_MAX_HOURS * 3_600_000L
    const val AD_FREE_ADD_MORE_THRESHOLD_MILLIS = AD_FREE_ADD_MORE_HOURS * 3_600_000L

    /** Alarmas de siembra: misma escala que «sin anuncios» (3 h por video, tope 12 h). */
    const val CROP_ALARM_PER_VIDEO_HOURS = AD_FREE_PER_VIDEO_HOURS
    const val CROP_ALARM_MAX_HOURS = AD_FREE_MAX_HOURS
    const val CROP_ALARM_ADD_MORE_HOURS = AD_FREE_ADD_MORE_HOURS
    const val CROP_ALARM_MAX_MILLIS = AD_FREE_MAX_MILLIS
    const val CROP_ALARM_ADD_MORE_THRESHOLD_MILLIS = AD_FREE_ADD_MORE_THRESHOLD_MILLIS

    private val KEY_AD_FREE = longPreferencesKey("ad_free_until_ms")
    private val KEY_CALENDAR = longPreferencesKey("calendar_until_ms")
    private val KEY_CROP_ALARM = longPreferencesKey("crop_alarm_until_ms")

    fun observe(context: Context): Flow<AdRewardState> =
        context.applicationContext.adRewardStore.data.map { prefs ->
            AdRewardState(
                adFreeUntilMillis = prefs[KEY_AD_FREE] ?: 0L,
                calendarUntilMillis = prefs[KEY_CALENDAR] ?: 0L,
                cropAlarmUntilMillis = prefs[KEY_CROP_ALARM] ?: 0L,
                loaded = true,
            )
        }

    /**
     * Suma horas al beneficio actual. Si ya está activo se acumula sobre su
     * vencimiento; si no, se cuenta desde ahora. Nunca supera el tope de 12 h.
     */
    suspend fun grantAdFreeHours(context: Context, hours: Long = AD_FREE_PER_VIDEO_HOURS) {
        val now = System.currentTimeMillis()
        val addMillis = hours * 3_600_000L
        val maxUntil = now + AD_FREE_MAX_MILLIS
        context.applicationContext.adRewardStore.edit { prefs ->
            val current = prefs[KEY_AD_FREE] ?: 0L
            val base = if (current > now) current else now
            prefs[KEY_AD_FREE] = (base + addMillis).coerceAtMost(maxUntil)
        }
    }

    /**
     * Suma horas al desbloqueo de alarmas de siembra. Igual que «sin anuncios»:
     * acumula sobre el vencimiento, cada video añade 3 h y el tope es 12 h.
     */
    suspend fun grantCropAlarmHours(context: Context, hours: Long = CROP_ALARM_PER_VIDEO_HOURS) {
        val now = System.currentTimeMillis()
        val addMillis = hours * 3_600_000L
        val maxUntil = now + CROP_ALARM_MAX_MILLIS
        context.applicationContext.adRewardStore.edit { prefs ->
            val current = prefs[KEY_CROP_ALARM] ?: 0L
            val base = if (current > now) current else now
            prefs[KEY_CROP_ALARM] = (base + addMillis).coerceAtMost(maxUntil)
        }
    }

    suspend fun grantCalendarUntilServerDay(context: Context, zoneId: String, now: Instant = Instant.now()) {
        val until = SkyWeatherGameDay.nextServerDawn(now, DexSchedule.zoneOf(zoneId)).toEpochMilli()
        context.applicationContext.adRewardStore.edit { it[KEY_CALENDAR] = until }
    }

    fun remainingMillis(untilMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long =
        (untilMillis - nowMillis).coerceAtLeast(0L)

    fun canAddMore(remainingMillis: Long): Boolean =
        remainingMillis <= AD_FREE_ADD_MORE_THRESHOLD_MILLIS

    fun canAddCropAlarmMore(remainingMillis: Long): Boolean =
        remainingMillis <= CROP_ALARM_ADD_MORE_THRESHOLD_MILLIS

    /** Formato compacto para el contador: «2h 45m» o «3h». */
    fun formatRemaining(millis: Long): String {
        val totalMinutes = (millis.coerceAtLeast(0L) + 59_999L) / 60_000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun remainingLabel(untilMillis: Long): String = formatRemaining(remainingMillis(untilMillis))
}
