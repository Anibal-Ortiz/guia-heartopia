package com.guiaheartopia.app.domain

import java.time.ZonedDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ReminderPrefs(
    val routineEnabled: Boolean = false,
    val routineMinutes: Int = 9 * 60,
    val weatherEnabled: Boolean = false,
    val weatherMinutes: Int = 8 * 60,
    val eventsEnabled: Boolean = false,
    val eventsMinutes: Int = 18 * 60,
)

/** Próximo aviso en la zona del reloj que se pasa. No usa alarma exacta. */
object ReminderPlanner {
    fun next(now: ZonedDateTime, minutesOfDay: Int): ZonedDateTime {
        val minutes = minutesOfDay.coerceIn(0, 23 * 60 + 59)
        var day = now.toLocalDate()
        repeat(4) {
            val zoned = day.atTime(minutes / 60, minutes % 60).atZone(now.zone)
            if (zoned.toInstant().isAfter(now.toInstant())) return zoned
            day = day.plusDays(1)
        }
        return now.plusDays(1)
    }
}
