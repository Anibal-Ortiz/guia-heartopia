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

/** Clima en la app: solo sol, lluvia o arcoíris (los datos siguen con el kind original). */
fun SkyKind.climateDisplay(): SkyKind = when (this) {
    SkyKind.LLUVIA -> SkyKind.LLUVIA
    SkyKind.ARCOIRIS, SkyKind.ARCOIRIS_LUNA -> SkyKind.ARCOIRIS
    SkyKind.SIN_DATOS -> SkyKind.SIN_DATOS
    else -> SkyKind.SOLEADO
}

enum class SkyStatus { ACUERDO, POR_CONFIRMAR, SIN_DATOS }

data class SkySlot(
    val start: LocalTime,
    val end: LocalTime,
    val kind: SkyKind,
    val status: SkyStatus,
    val place: String? = null,
    val special: String? = null,
    val detail: String = "",
) {
    fun title(): String {
        val base = if (kind == SkyKind.METEORO && !place.isNullOrBlank()) {
            "${kind.emoji} ${kind.label} en $place"
        } else {
            "${kind.emoji} ${kind.label}"
        }
        val extra = if (special == "luna_carmesi") " · Luna carmesí" else ""
        return base + extra
    }

    /** Etiqueta sin emoji para la UI de clima (tres tipos: sol, lluvia, arcoíris). */
    fun plainTitle(): String {
        return kind.climateDisplay().label
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
        val label = kind.climateDisplay().label
        return "$label ${start.toLocalTime().format(clock)}–${end.toLocalTime().format(clock)}"
    }
}

data class SkyBarSegment(val kind: SkyKind, val weight: Int)

data class SkyDay(
    val date: LocalDate,
    val slots: List<SkySlot>,
)

/**
 * Las horas del calendario son el reloj local de la zona elegida, no UTC.
 * La UI usa [SkyWeatherGameDay]. El horario de las criaturas sigue en [DexSchedule].
 */
object SkyWeather {
    private val allCreatureWeather = setOf("Sunny", "Rainy", "Rainbow")

    private fun containsClock(slot: SkySlot, time: LocalTime): Boolean {
        val end = slot.end
        return if (end == LocalTime.MIDNIGHT || !end.isAfter(slot.start)) {
            !time.isBefore(slot.start)
        } else {
            !time.isBefore(slot.start) && time.isBefore(end)
        }
    }

    fun slotIndex(instant: Instant): Int {
        val hour = instant.atZone(ZoneOffset.UTC).hour
        return hour / 6
    }

    fun localWindow(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<ZonedDateTime, ZonedDateTime> {
        val (start, end) = SkyWeatherLocal.utcSlotRange(date, slot)
        return ZonedDateTime.ofInstant(start, zone) to ZonedDateTime.ofInstant(end, zone)
    }

    fun slotAt(day: SkyDay?, instant: Instant): SkySlot? {
        if (day == null) return null
        val utc = instant.atZone(ZoneOffset.UTC)
        if (day.date != utc.toLocalDate()) return null
        return day.slots.firstOrNull { slot -> containsClock(slot, utc.toLocalTime()) }
    }

    fun availableNow(
        schedule: List<String>,
        neededWeather: List<String>,
        instant: Instant,
        table: com.guiaheartopia.app.data.SkyTable,
        zone: ZoneId,
    ): Boolean {
        if (!DexSchedule.availableNow(schedule, instant)) return false
        val slot = SkyWeatherGameDay.conditionAt(table, instant, zone) ?: return true
        if (slot.status == SkyStatus.SIN_DATOS || slot.kind == SkyKind.SIN_DATOS) return true
        return allows(neededWeather, slot.kind)
    }

    fun merged(day: SkyDay, zone: ZoneId): List<SkySpan> {
        val table = com.guiaheartopia.app.data.SkyTable("", "", mapOf(day.date to day))
        return SkyWeatherGameDay.spansForGameDay(table, day.date, zone).map { span ->
            SkySpan(span.kind, span.status, null, null, span.start, span.end)
        }
    }

    fun summary(day: SkyDay, zone: ZoneId): String {
        val table = com.guiaheartopia.app.data.SkyTable("", "", mapOf(day.date to day))
        return SkyWeatherGameDay.summaryForGameDay(table, day.date, zone)
    }

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
