package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.SkyTable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

/**
 * Tramos del JSON (hora civil de la zona elegida) convertidos a instantes y recortados
 * por día civil. El JSON declara `serverZone: "reloj-local"`, así que tabla y relojes
 * usan la misma zona; no hay desfase UTC que aplicar.
 */
object SkyWeatherLocal {
    fun utcSlotRange(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<Instant, Instant> {
        val source = WeatherSource.zoneFor(zone)
        val start = ZonedDateTime.of(date, slot.start, source).toInstant()
        val end = when {
            slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start) ->
                ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, source).toInstant()
            else -> ZonedDateTime.of(date, slot.end, source).toInstant()
        }
        return start to end
    }

    /** Fechas del JSON (hora civil de [zone]) que tocan el día civil indicado. */
    fun utcDatesOverlapping(localDate: LocalDate, zone: ZoneId): List<LocalDate> =
        listOf(localDate)

    /** Fechas de origen comprendidas entre dos instantes (inclusive). */
    fun utcDatesBetween(first: LocalDate, last: LocalDate): List<LocalDate> {
        val out = ArrayList<LocalDate>()
        var cursor = first
        while (!cursor.isAfter(last)) {
            out += cursor
            cursor = cursor.plusDays(1)
        }
        return out
    }

    /** Fechas del JSON (hora civil de [zone]) comprendidas entre dos instantes (inclusive). */
    fun utcDatesBetween(start: Instant, end: Instant, zone: ZoneId): List<LocalDate> {
        val source = WeatherSource.zoneFor(zone)
        return utcDatesBetween(
            start.atZone(source).toLocalDate(),
            end.atZone(source).toLocalDate(),
        )
    }

    fun clipToLocalDay(
        start: Instant,
        end: Instant,
        localDate: LocalDate,
        zone: ZoneId,
    ): Pair<ZonedDateTime, ZonedDateTime>? {
        val dayStart = localDate.atStartOfDay(zone).toInstant()
        val dayEnd = localDate.plusDays(1).atStartOfDay(zone).toInstant()
        val clipStart = maxOf(start, dayStart)
        val clipEnd = minOf(end, dayEnd)
        if (!clipStart.isBefore(clipEnd)) return null
        return ZonedDateTime.ofInstant(clipStart, zone) to ZonedDateTime.ofInstant(clipEnd, zone)
    }

    fun spansOnLocalDay(table: SkyTable, localDate: LocalDate, zone: ZoneId): List<SkySpan> {
        val pieces = ArrayList<SkySpan>()
        for (utcDate in utcDatesOverlapping(localDate, zone)) {
            val skyDay = table.on(utcDate) ?: continue
            for (slot in skyDay.slots) {
                val display = slot.kind.climateDisplay()
                if (display == SkyKind.SIN_DATOS) continue
                val (s, e) = utcSlotRange(utcDate, slot, zone)
                val clipped = clipToLocalDay(s, e, localDate, zone) ?: continue
                pieces += SkySpan(display, slot.status, null, null, clipped.first, clipped.second)
            }
        }
        pieces.sortBy { it.start.toInstant() }
        return SkySpanSupport.mergeAdjacent(pieces)
    }

    fun barSegmentsOnLocalDay(table: SkyTable, localDate: LocalDate, zone: ZoneId): List<SkyBarSegment> {
        val spans = spansOnLocalDay(table, localDate, zone)
        if (spans.isEmpty()) return emptyList()
        val totalMinutes = spans.sumOf { durationMinutes(it) }.coerceAtLeast(1L)
        return spans.map { span ->
            val weight = max(1, ((durationMinutes(span) * 4) / totalMinutes).toInt())
            SkyBarSegment(span.kind, weight)
        }
    }

    fun summaryOnLocalDay(table: SkyTable, localDate: LocalDate, zone: ZoneId): String =
        spansOnLocalDay(table, localDate, zone)
            .joinToString(" · ") { formatSpan(it, localDate, zone) }
            .ifBlank { "Sin datos de clima" }

    fun formatSpan(span: SkySpan, focusLocalDate: LocalDate, zone: ZoneId): String {
        val label = span.kind.climateDisplay().label
        val dayStart = focusLocalDate.atStartOfDay(zone)
        val dayEnd = focusLocalDate.plusDays(1).atStartOfDay(zone)
        val coversFull = !span.start.isAfter(dayStart) && !span.end.isBefore(dayEnd)
        if (coversFull) return "$label · Todo el día"
        val clock = DateTimeFormatter.ofPattern("HH:mm")
        val t1 = span.start.toLocalTime().format(clock)
        val endClock = span.end.toLocalTime()
        val t2 = endClock.format(clock)
        val minutes = java.time.Duration.between(span.start.toInstant(), span.end.toInstant()).toMinutes()
        val duration = if (minutes % 60L == 0L) "${minutes / 60} h" else "$minutes min"
        val endsAtLocalMidnight = span.end == dayEnd || endClock == LocalTime.MIDNIGHT
        if (t1 == t2) {
            val dateFmt = DateTimeFormatter.ofPattern("d MMM", Locale("es"))
            return "$label del ${span.start.format(dateFmt)} $t1 al ${span.end.format(dateFmt)} $t2 ($duration)"
        }
        if (endsAtLocalMidnight) {
            return "$label $t1–24:00 ($duration, hasta medianoche)"
        }
        val endDate = span.end.toLocalDate()
        if (endDate.isAfter(focusLocalDate)) {
            return "$label $t1–$t2 del ${endDate.format(DateTimeFormatter.ofPattern("d MMM"))} ($duration)"
        }
        return "$label $t1–$t2 ($duration)"
    }

    private fun durationMinutes(span: SkySpan): Long {
        val s = span.start.toInstant()
        val e = span.end.toInstant()
        return max(1, java.time.Duration.between(s, e).toMinutes())
    }

}
