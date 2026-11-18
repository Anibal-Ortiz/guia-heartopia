package com.guiaheartopia.app.domain

import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ReminderPrefs(
    val routineEnabled: Boolean = false,
    val routineMinutes: Int = 9 * 60,
    val weatherEnabled: Boolean = false,
    val weatherMinutes: Int = 8 * 60,
    val eventsEnabled: Boolean = false,
    /** Marca que el usuario ha configurado el interruptor maestro de eventos. */
    val eventsMasterTouched: Boolean = false,
    val eventsMinutes: Int = 18 * 60,
    val pescaEnabled: Boolean = false,
    val avesEnabled: Boolean = false,
    val insectosEnabled: Boolean = false,
    val temporadaEnabled: Boolean = false,
    val promptDismissed: Boolean = false,
) {
    fun anyAlertOn(): Boolean =
        weatherEnabled || eventsEnabled || pescaEnabled || avesEnabled || insectosEnabled || temporadaEnabled
}

/** Próximo aviso en la zona del reloj que se pasa. No usa alarma exacta. */
object ReminderPlanner {
    /**
     * Resuelve la hora en el día indicado. En un salto DST la hora puede no existir:
     * se ajusta con [java.time.zone.ZoneRules.getValidOffsets] y el desfase de la
     * transición, en vez de dejar que [LocalDateTime.atZone] desplace el resultado
     * al día siguiente.
     */
    private fun atTime(day: java.time.LocalDate, minutes: Int, zone: java.time.ZoneId): ZonedDateTime {
        val local = day.atTime(minutes / 60, minutes % 60)
        val rules = zone.rules
        val offsets = rules.getValidOffsets(local)
        return when {
            offsets.size == 1 -> ZonedDateTime.ofLocal(local, zone, offsets[0])
            offsets.isEmpty() -> {
                // Hueco: la hora no existe. Se corrige con la duración de la transición.
                val shift = rules.getTransition(local)?.duration ?: java.time.Duration.ofHours(1)
                local.plus(shift).atZone(zone)
            }
            else -> ZonedDateTime.ofLocal(local, zone, offsets[0]) // solapamiento: desfase anterior
        }
    }

    fun next(now: ZonedDateTime, minutesOfDay: Int): ZonedDateTime {
        val minutes = minutesOfDay.coerceIn(0, 23 * 60 + 59)
        var day = now.toLocalDate()
        repeat(4) {
            val zoned = atTime(day, minutes, now.zone)
            if (zoned.toInstant().isAfter(now.toInstant())) return zoned
            day = day.plusDays(1)
        }
        return now.plusDays(1)
    }
}
