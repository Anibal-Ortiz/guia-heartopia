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
 * El JSON guarda bloques UTC 00–06 … 18–24 por fecha civil del servidor.
 * La UI del calendario usa [SkyWeatherGameDay] (franjas del juego, mismo UTC que [DexSchedule]).
 * [slotAt] conserva los bloques de 6 h para criaturas.
 */
object SkyWeather {
    private val allCreatureWeather = setOf("Sunny", "Rainy", "Rainbow")

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
        val utcDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
        if (day.date != utcDate) return null
        return day.slots.getOrNull(slotIndex(instant))
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
