package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.SkyTable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

/**
 * Conversión de tramos UTC del JSON a días y horas locales (IANA), con recorte por día civil.
 */
object SkyWeatherLocal {
    fun utcSlotRange(utcDate: LocalDate, slot: SkySlot): Pair<Instant, Instant> {
        val start = ZonedDateTime.of(utcDate, slot.start, ZoneOffset.UTC).toInstant()
        val end = when {
            slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start) ->
                ZonedDateTime.of(utcDate.plusDays(1), LocalTime.MIDNIGHT, ZoneOffset.UTC).toInstant()
            else -> ZonedDateTime.of(utcDate, slot.end, ZoneOffset.UTC).toInstant()
        }
        return start to end
    }

    fun utcDatesOverlapping(localDate: LocalDate, zone: ZoneId): List<LocalDate> {
        val first = localDate.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDate()
        val last = localDate.plusDays(1).atStartOfDay(zone).minusNanos(1)
            .withZoneSameInstant(ZoneOffset.UTC).toLocalDate()
        val out = ArrayList<LocalDate>()
        var cursor = first
        while (!cursor.isAfter(last)) {
            out += cursor
            cursor = cursor.plusDays(1)
        }
        return out
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
                val (s, e) = utcSlotRange(utcDate, slot)
                val clipped = clipToLocalDay(s, e, localDate, zone) ?: continue
                pieces += SkySpan(display, slot.status, null, null, clipped.first, clipped.second)
            }
        }
        pieces.sortBy { it.start.toInstant() }
        return mergeAdjacent(pieces)
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

    private fun mergeAdjacent(sorted: List<SkySpan>): List<SkySpan> {
        if (sorted.isEmpty()) return sorted
        val out = ArrayList<SkySpan>()
        for (span in sorted) {
            val prev = out.lastOrNull()
            if (prev != null &&
                prev.kind == span.kind &&
                prev.status == span.status &&
                prev.end.toInstant() == span.start.toInstant()
            ) {
                out[out.lastIndex] = prev.copy(end = span.end)
            } else {
                out += span
            }
        }
        return out
    }
}
