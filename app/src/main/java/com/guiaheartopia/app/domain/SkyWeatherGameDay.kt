package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.SkyTable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Clima del calendario alineado al **día del juego** (franjas DexSchedule en UTC),
 * no al día civil 00:00–24:00 recortado.
 *
 * El JSON guarda bloques UTC 00–06, 06–12, 12–18, 18–24 por fecha civil UTC.
 * Cada franja de juego toma la condición del bloque con mayor solapamiento en UTC
 * (misma regla para mes, detalle e Inicio).
 */
object SkyWeatherGameDay {
    /** Orden mostrado: noche → amanecer → día → tarde. */
    private val frameOrder = listOf("Night", "Dawn", "Day", "Dusk")

    data class GameClimateSpan(
        val kind: SkyKind,
        val status: SkyStatus,
        val start: ZonedDateTime,
        val end: ZonedDateTime,
        val frameKey: String,
    ) {
        fun label(): String {
            return kind.climateDisplay().label
        }
    }

    fun spansForGameDay(table: SkyTable, localGameDay: LocalDate, zone: ZoneId): List<GameClimateSpan> {
        val utcAnchor = localGameDay
        return frameOrder.mapNotNull { key ->
            val window = DexSchedule.window(key, utcAnchor, zone)
            val pick = pickCatalogSlot(table, window.start.toInstant(), window.end.toInstant())
                ?: return@mapNotNull null
            GameClimateSpan(
                kind = pick.kind.climateDisplay(),
                status = pick.status,
                start = window.start,
                end = window.end,
                frameKey = key,
            )
        }
    }

    fun barSegmentsForGameDay(table: SkyTable, localGameDay: LocalDate, zone: ZoneId): List<SkyBarSegment> =
        spansForGameDay(table, localGameDay, zone).map { SkyBarSegment(it.kind, 1) }

    fun formatSpan(span: GameClimateSpan, focusLocalDate: LocalDate): String {
        val clock = DateTimeFormatter.ofPattern("HH:mm")
        val t1 = span.start.toLocalTime().format(clock)
        val t2 = span.end.toLocalTime().format(clock)
        val suffix = if (span.end.toLocalDate().isAfter(focusLocalDate)) " del día siguiente" else ""
        return "${span.label()} $t1–$t2$suffix"
    }

    fun summaryForGameDay(table: SkyTable, localGameDay: LocalDate, zone: ZoneId): String =
        spansForGameDay(table, localGameDay, zone)
            .joinToString(" · ") { formatSpan(it, localGameDay) }
            .ifBlank { "Sin datos de clima" }

    /**
     * Bloque del catálogo con mayor duración de solapamiento con [utcStart, utcEnd).
     */
    internal fun pickCatalogSlot(table: SkyTable, utcStart: Instant, utcEnd: Instant): SkySlot? {
        var best: Pair<SkySlot, Long>? = null
        val utcDates = utcDatesBetween(utcStart, utcEnd)
        for (utcDate in utcDates) {
            val day = table.on(utcDate) ?: continue
            for (slot in day.slots) {
                val (s, e) = utcSlotRange(utcDate, slot)
                val overlap = overlapMinutes(utcStart, utcEnd, s, e)
                if (overlap <= 0) continue
                if (best == null || overlap > best!!.second) {
                    best = slot to overlap
                }
            }
        }
        return best?.first
    }

    private fun utcDatesBetween(start: Instant, end: Instant): List<LocalDate> {
        val a = start.atZone(ZoneOffset.UTC).toLocalDate()
        val b = end.minusNanos(1).atZone(ZoneOffset.UTC).toLocalDate()
        val out = ArrayList<LocalDate>()
        var cur = a
        while (!cur.isAfter(b)) {
            out += cur
            cur = cur.plusDays(1)
        }
        return out
    }

    private fun utcSlotRange(utcDate: LocalDate, slot: SkySlot): Pair<Instant, Instant> {
        val start = ZonedDateTime.of(utcDate, slot.start, ZoneOffset.UTC).toInstant()
        val end = when {
            slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start) ->
                ZonedDateTime.of(utcDate.plusDays(1), LocalTime.MIDNIGHT, ZoneOffset.UTC).toInstant()
            else -> ZonedDateTime.of(utcDate, slot.end, ZoneOffset.UTC).toInstant()
        }
        return start to end
    }

    private fun overlapMinutes(aStart: Instant, aEnd: Instant, bStart: Instant, bEnd: Instant): Long {
        val start = maxOf(aStart, bStart)
        val end = minOf(aEnd, bEnd)
        if (!start.isBefore(end)) return 0
        return Duration.between(start, end).toMinutes()
    }
}
