package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Un tramo del servidor. [start] es inclusivo y [end] exclusivo, ambos en hora UTC.
 * Noche cruza la medianoche UTC: acaba al día siguiente.
 */
data class GameSlot(
    val key: String,
    val label: String,
    val emoji: String,
    val start: LocalTime,
    val end: LocalTime,
) {
    val crossesMidnight: Boolean get() = !end.isAfter(start)

    fun containsUtc(time: LocalTime): Boolean = if (crossesMidnight) {
        !time.isBefore(start) || time.isBefore(end)
    } else {
        !time.isBefore(start) && time.isBefore(end)
    }
}

/** Tramo ya convertido a la zona del país, con el desfase real de cada límite. */
data class LocalGameWindow(
    val slot: GameSlot,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
    fun contains(instant: Instant): Boolean =
        !instant.isBefore(start.toInstant()) && instant.isBefore(end.toInstant())
}

/**
 * Horario del juego. El servidor no usa el horario de verano del jugador:
 * los tramos viven en UTC y la zona del país solo cambia cómo se muestran.
 */
object DexSchedule {
    /**
     * Tramos en UTC, en un solo sitio.
     *
     * Horas tomadas de Chile a UTC−3 el 2026-11-06: Amanecer 02:00–08:00,
     * Día 08:00–14:00, Tarde (Atardecer en heartodex) 14:00–20:00 y Noche 20:00–02:00.
     * Se asume que el servidor usa un desfase fijo, sin horario de verano.
     * Si el juego sigue el horario de verano de Chile, cambia estas horas.
     */
    val UTC_SLOTS: List<GameSlot> = listOf(
        GameSlot("Dawn", "Amanecer", "🌅", LocalTime.of(5, 0), LocalTime.of(11, 0)),
        GameSlot("Day", "Día", "☀️", LocalTime.of(11, 0), LocalTime.of(17, 0)),
        GameSlot("Dusk", "Tarde", "🌤️", LocalTime.of(17, 0), LocalTime.of(23, 0)),
        GameSlot("Night", "Noche", "🌆", LocalTime.of(23, 0), LocalTime.of(5, 0)),
    )

    val allDay = setOf("Day", "Dawn", "Dusk", "Night")

    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun availableAllDay(schedule: List<String>): Boolean = allDay.all { it in schedule }

    fun slotAt(instant: Instant): GameSlot {
        val time = LocalTime.ofInstant(instant, ZoneOffset.UTC)
        return UTC_SLOTS.first { it.containsUtc(time) }
    }

    /** El clima no entra: solo cuenta el tramo. Una ficha sin horario no está «ahora». */
    fun availableNow(schedule: List<String>, instant: Instant): Boolean {
        if (schedule.isEmpty()) return false
        return slotAt(instant).key in schedule
    }

    fun window(slotKey: String, utcDate: LocalDate, zone: ZoneId): LocalGameWindow {
        val slot = UTC_SLOTS.first { it.key == slotKey }
        return windowOnUtcDate(slot, utcDate, zone)
    }

    fun currentWindow(zone: ZoneId, instant: Instant): LocalGameWindow {
        val slot = slotAt(instant)
        val utc = instant.atZone(ZoneOffset.UTC)
        val startDate = if (slot.crossesMidnight && utc.toLocalTime().isBefore(slot.end)) {
            utc.toLocalDate().minusDays(1)
        } else {
            utc.toLocalDate()
        }
        return windowOnUtcDate(slot, startDate, zone)
    }

    /**
     * Los cuatro tramos en la zona del país: el que está en curso y, si ya pasó,
     * el siguiente. Cada límite usa el desfase de ese instante, horario de verano incluido.
     */
    fun windowsAround(zone: ZoneId, instant: Instant): List<LocalGameWindow> {
        val utcDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
        return UTC_SLOTS.map { slot ->
            val candidates = listOf(-1L, 0L, 1L).map { delta ->
                windowOnUtcDate(slot, utcDate.plusDays(delta), zone)
            }
            candidates.firstOrNull { it.contains(instant) }
                ?: candidates.first { instant.isBefore(it.start.toInstant()) }
        }
    }

    fun titled(slot: GameSlot): String = if (slot.emoji.isBlank()) slot.label else "${slot.emoji} ${slot.label}"

    fun hoursLabel(window: LocalGameWindow): String =
        "${window.start.toLocalTime().format(clock)}–${window.end.toLocalTime().format(clock)}"

    fun untilLabel(window: LocalGameWindow, now: ZonedDateTime): String {
        val endClock = window.end.toLocalTime().format(clock)
        val endDate = window.end.toLocalDate()
        val today = now.withZoneSameInstant(window.end.zone).toLocalDate()
        return when {
            endDate == today -> "hasta las $endClock"
            endDate == today.plusDays(1) -> "hasta mañana a las $endClock"
            else -> "hasta el $endDate a las $endClock"
        }
    }

    fun scheduleEs(value: String): String = UTC_SLOTS.firstOrNull { it.key == value }?.label ?: value

    fun scheduleLabel(value: String): String {
        val slot = UTC_SLOTS.firstOrNull { it.key == value } ?: return value
        return titled(slot)
    }

    fun weatherEs(value: String): String = when (value) {
        "Sunny" -> "Soleado"
        "Rainy" -> "Lluvioso"
        "Rainbow" -> "Arcoíris"
        else -> value
    }

    fun zoneOf(zoneId: String): ZoneId =
        runCatching { ZoneId.of(zoneId) }.getOrElse { ZoneId.of("America/Santiago") }

    private fun windowOnUtcDate(slot: GameSlot, utcDate: LocalDate, zone: ZoneId): LocalGameWindow {
        val start = ZonedDateTime.of(utcDate, slot.start, ZoneOffset.UTC).withZoneSameInstant(zone)
        val endDate = if (slot.crossesMidnight) utcDate.plusDays(1) else utcDate
        val end = ZonedDateTime.of(endDate, slot.end, ZoneOffset.UTC).withZoneSameInstant(zone)
        return LocalGameWindow(slot, start, end)
    }
}
