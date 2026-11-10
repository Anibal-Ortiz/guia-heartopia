package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.SkyTable
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Clima del día del juego (noche → amanecer → día → tarde).
 *
 * Las horas del JSON son el reloj de la zona elegida, no UTC. Si el cielo
 * cambia dentro de esa ventana, cada tramo conserva su inicio y su fin.
 */
object SkyWeatherGameDay {
    private val frameOrder = listOf("Night", "Dawn", "Day", "Dusk")

    data class GameClimateSpan(
        val kind: SkyKind,
        val status: SkyStatus,
        val start: ZonedDateTime,
        val end: ZonedDateTime,
        val frameKey: String,
        val detail: String = "",
    ) {
        fun label(): String = kind.climateDisplay().label
    }

    fun spansForGameDay(table: SkyTable, localGameDay: LocalDate, zone: ZoneId): List<GameClimateSpan> {
        val windows = frameOrder.map { DexSchedule.window(it, localGameDay, zone) }
        val from = windows.first().start.toInstant()
        val to = windows.last().end.toInstant()
        val raw = ArrayList<GameClimateSpan>()
        var date = localGameDay.minusDays(1)
        val last = localGameDay.plusDays(1)
        while (!date.isAfter(last)) {
            val day = table.on(date)
            if (day != null) {
                for (slot in day.slots) {
                    val (start, end) = localRange(date, slot, zone)
                    val clipStart = maxOf(start.toInstant(), from)
                    val clipEnd = minOf(end.toInstant(), to)
                    if (!clipStart.isBefore(clipEnd)) continue
                    val zonedStart = clipStart.atZone(zone)
                    raw += GameClimateSpan(
                        kind = slot.kind.climateDisplay(),
                        status = slot.status,
                        start = zonedStart,
                        end = clipEnd.atZone(zone),
                        frameKey = frameKeyAt(windows, zonedStart.toInstant()),
                        detail = "",
                    )
                }
            }
            date = date.plusDays(1)
        }
        raw.sortBy { it.start.toInstant() }
        return merge(raw)
    }

    fun conditionAt(table: SkyTable, instant: Instant, zone: ZoneId): SkySlot? {
        val local = instant.atZone(zone)
        val day = table.on(local.toLocalDate()) ?: return null
        return day.slots.firstOrNull { slot ->
            val (start, end) = localRange(local.toLocalDate(), slot, zone)
            !instant.isBefore(start.toInstant()) && instant.isBefore(end.toInstant())
        }
    }

    fun barSegmentsForGameDay(table: SkyTable, localGameDay: LocalDate, zone: ZoneId): List<SkyBarSegment> {
        val merged = ArrayList<SkyBarSegment>()
        for (span in spansForGameDay(table, localGameDay, zone)) {
            val minutes = Duration.between(span.start.toInstant(), span.end.toInstant()).toMinutes().toInt()
            val piece = SkyBarSegment(span.kind, minutes.coerceAtLeast(1))
            val last = merged.lastOrNull()
            if (last != null && last.kind == piece.kind) {
                merged[merged.lastIndex] = last.copy(weight = last.weight + piece.weight)
            } else {
                merged += piece
            }
        }
        return merged
    }

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

    private fun localRange(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<ZonedDateTime, ZonedDateTime> {
        val start = ZonedDateTime.of(date, slot.start, zone)
        val end = if (slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start)) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, zone)
        } else {
            ZonedDateTime.of(date, slot.end, zone)
        }
        return start to end
    }

    private fun frameKeyAt(windows: List<LocalGameWindow>, instant: Instant): String =
        windows.firstOrNull { it.contains(instant) }?.slot?.key ?: windows.first().slot.key

    private fun merge(spans: List<GameClimateSpan>): List<GameClimateSpan> {
        if (spans.isEmpty()) return spans
        val out = ArrayList<GameClimateSpan>()
        for (span in spans) {
            val prev = out.lastOrNull()
            if (
                prev != null &&
                prev.label() == span.label() &&
                prev.kind == span.kind &&
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
