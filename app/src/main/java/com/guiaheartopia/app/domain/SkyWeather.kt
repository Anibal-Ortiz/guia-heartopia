package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

enum class SkyKind(
    val label: String,
    val emoji: String,
    val creature: String,
) {
    DESPEJADO("Despejado", "🌕", "SUN"),
    NUBLADO("Nublado", "☁️", "SUN"),
    SOLEADO("Sol", "☀️", "SUN"),
    LLUVIA("Lluvia", "🌧️", "RAIN"),
    ARCOIRIS("Arcoíris", "🌈", "RAINBOW"),
    ARCOIRIS_LUNA("Arcoíris de luna", "🌙", "RAINBOW"),
    METEORO("Meteoros", "☄️", "OTHER"),
    NIEVE("Nieve", "❄️", "OTHER"),
    AURORA("Aurora", "🌌", "OTHER"),
    CALOR("Calor", "🔥", "OTHER"),
    PETALOS("Lluvia de pétalos", "🌸", "OTHER"),
    SIN_DATOS("Sin datos", "·", "NONE"),
}

enum class SkyStatus { ACUERDO, POR_CONFIRMAR, SIN_DATOS }

data class SkySlot(
    val start: LocalTime,
    val end: LocalTime,
    val kind: SkyKind,
    val status: SkyStatus,
    val place: String? = null,
    val special: String? = null,
) {
    fun title(): String {
        val base = if (kind == SkyKind.METEORO && !place.isNullOrBlank()) {
            "${kind.emoji} ${kind.label} en $place"
        } else {
            "${kind.emoji} ${kind.label}"
        }
        val extra = if (special == "luna_carmesi") " · Luna carmesí" else ""
        val mark = if (status == SkyStatus.POR_CONFIRMAR) " (por confirmar)" else ""
        return base + extra + mark
    }
}

data class SkySpan(
    val kind: SkyKind,
    val status: SkyStatus,
    val place: String?,
    val special: String?,
    val start: ZonedDateTime,
    val end: ZonedDateTime,
) {
    fun text(): String {
        val clock = DateTimeFormatter.ofPattern("HH:mm")
        val label = if (kind == SkyKind.METEORO && !place.isNullOrBlank()) {
            "${kind.emoji} ${kind.label} en $place"
        } else {
            "${kind.emoji} ${kind.label}"
        }
        return "$label ${start.toLocalTime().format(clock)}–${end.toLocalTime().format(clock)}"
    }
}

data class SkyDay(
    val date: LocalDate,
    val slots: List<SkySlot>,
)

/**
 * El reloj del calendario de Tidebell coincidía con UTC el 2026-11-07.
 * Los tramos 00–06, 06–12, 12–18 y 18–24 se guardan en esa hora de servidor
 * y se convierten a la zona del país, con horario de verano.
 */
object SkyWeather {
    private val allCreatureWeather = setOf("Sunny", "Rainy", "Rainbow")

    fun slotIndex(instant: Instant): Int {
        val hour = instant.atZone(ZoneOffset.UTC).hour
        return hour / 6
    }

    fun localWindow(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<ZonedDateTime, ZonedDateTime> {
        val start = ZonedDateTime.of(date, slot.start, ZoneOffset.UTC).withZoneSameInstant(zone)
        val endDate = if (!slot.end.isAfter(slot.start) || slot.end == LocalTime.MIDNIGHT) date.plusDays(1) else date
        val endTime = if (slot.end == LocalTime.MIDNIGHT && slot.end.hour == 0 && slot.start.hour >= 18) {
            LocalTime.MIDNIGHT
        } else slot.end
        val end = if (slot.end.hour == 0 && slot.start.hour == 18) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, ZoneOffset.UTC).withZoneSameInstant(zone)
        } else {
            ZonedDateTime.of(endDate, endTime, ZoneOffset.UTC).withZoneSameInstant(zone)
        }
        return start to end
    }

    fun slotAt(day: SkyDay?, instant: Instant): SkySlot? {
        if (day == null) return null
        val utcDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
        if (day.date != utcDate) return null
        return day.slots.getOrNull(slotIndex(instant))
    }

    /** Tramos seguidos con el mismo cielo se juntan en la hora local. */
    fun merged(day: SkyDay, zone: ZoneId): List<SkySpan> {
        val spans = ArrayList<SkySpan>()
        for (slot in day.slots) {
            if (slot.kind == SkyKind.SIN_DATOS) continue
            val (start, end) = localWindow(day.date, slot, zone)
            val previous = spans.lastOrNull()
            if (previous != null && previous.kind == slot.kind && previous.place == slot.place && previous.status == slot.status) {
                spans[spans.lastIndex] = previous.copy(end = end, special = previous.special ?: slot.special)
            } else {
                spans += SkySpan(slot.kind, slot.status, slot.place, slot.special, start, end)
            }
        }
        return spans
    }

    fun summary(day: SkyDay, zone: ZoneId): String =
        merged(day, zone).joinToString(" · ") { it.text() }.ifBlank { "Sin datos de clima" }

    /**
     * El arcoíris cubre sol, lluvia y arcoíris. La lluvia no cubre el arcoíris.
     * Un cielo especial (meteoros, nieve) no cuenta como sol ni como lluvia.
     */
    fun allows(needed: List<String>, kind: SkyKind): Boolean {
        if (needed.isEmpty() || allCreatureWeather.all { it in needed }) return true
        if (kind == SkyKind.SIN_DATOS) return true
        val sky = kind.creature
        if (sky == "RAINBOW") return true
        return needed.any { need ->
            when (need) {
                "Sunny" -> sky == "SUN"
                "Rainy" -> sky == "RAIN"
                "Rainbow" -> false
                else -> true
            }
        }
    }

    fun availableNow(
        schedule: List<String>,
        neededWeather: List<String>,
        instant: Instant,
        day: SkyDay?,
    ): Boolean {
        if (!DexSchedule.availableNow(schedule, instant)) return false
        val slot = slotAt(day, instant) ?: return true
        if (slot.status == SkyStatus.SIN_DATOS || slot.kind == SkyKind.SIN_DATOS) return true
        return allows(neededWeather, slot.kind)
    }
}
