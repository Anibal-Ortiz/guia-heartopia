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

/**
 * Clima del día del servidor: Amanecer, Día, Tarde y Noche, hasta el amanecer siguiente.
 *
 * Los tramos salen de [DexSchedule] en UTC y se convierten con la zona IANA (el desfase
 * de cada límite, horario de verano incluido). No se fija Chile a UTC−3.
 *
 * Las horas del JSON son el reloj local de Tidebell (la fecha y la hora del lector),
 * no UTC ni el desfase de un servidor regional. Se colocan en la zona elegida y después
 * se recortan a cada franja ya convertida. No se vuelven a desplazar.
 */
object SkyWeatherGameDay {
    private val frameOrder = listOf("Dawn", "Day", "Dusk", "Night")
    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

    data class ClimateLine(val text: String, val frame: Boolean)

    /** Inicio del próximo amanecer del servidor, cuando se reinicia el día. */
    fun nextServerDawn(instant: Instant, zone: ZoneId): Instant {
        val dawnDate = serverDawnLocalDate(instant, zone)
        val opening = windowsForLocalDawn(dawnDate, zone).first { it.slot.key == "Dawn" }.start.toInstant()
        val following = windowsForLocalDawn(dawnDate.plusDays(1), zone).first { it.slot.key == "Dawn" }.start.toInstant()
        return if (instant.isBefore(opening)) opening else following
    }

    /** Fecha local del amanecer que abre el día del servidor que contiene [instant]. */
    fun serverDawnLocalDate(instant: Instant, zone: ZoneId): LocalDate {
        val current = DexSchedule.currentWindow(zone, instant)
        val utcStart = current.start.withZoneSameInstant(ZoneOffset.UTC).toLocalDate()
        val dawnUtc = if (current.slot.key == "Night") utcStart.minusDays(1) else utcStart
        return DexSchedule.window("Dawn", dawnUtc, zone).start.toLocalDate()
    }

    fun windowsForLocalDawn(localDawnDate: LocalDate, zone: ZoneId): List<LocalGameWindow> {
        val dawnUtc = dawnUtcDate(localDawnDate, zone)
        return frameOrder.map { key ->
            val utcDate = if (key == "Night") dawnUtc.plusDays(1) else dawnUtc
            DexSchedule.window(key, utcDate, zone)
        }
    }

    fun spansForGameDay(table: SkyTable, localDawnDate: LocalDate, zone: ZoneId): List<GameClimateSpan> =
        windowsForLocalDawn(localDawnDate, zone).flatMap { window -> spansInWindow(table, window, zone) }

    fun linesForGameDay(table: SkyTable, localDawnDate: LocalDate, zone: ZoneId): List<ClimateLine> {
        val windows = windowsForLocalDawn(localDawnDate, zone)
        val grouped = windows.map { window -> window to spansInWindow(table, window, zone) }
        if (grouped.all { it.second.isEmpty() }) return emptyList()
        val lines = ArrayList<ClimateLine>()
        for ((window, spans) in grouped) {
            lines += ClimateLine(frameTitle(window, localDawnDate), frame = true)
            spans.forEach { span ->
                lines += ClimateLine(formatSpan(span, localDawnDate), frame = false)
            }
        }
        return lines
    }

    /**
     * Un clima por franja del juego (el que más dura). La leyenda omite el despejado,
     * salvo que las cuatro franjas lo sean.
     */
    fun frameClimatesForGameDay(
        table: SkyTable,
        localDawnDate: LocalDate,
        zone: ZoneId,
    ): List<GameClimateSpan> =
        windowsForLocalDawn(localDawnDate, zone).mapNotNull { window ->
            val spans = spansInWindow(table, window, zone)
            if (spans.isEmpty()) return@mapNotNull null
            val kind = dominantKind(spans)
            spans.first().copy(
                kind = kind,
                start = window.start,
                end = window.end,
                frameKey = window.slot.key,
            )
        }

    fun compactLinesForGameDay(table: SkyTable, localDawnDate: LocalDate, zone: ZoneId): List<ClimateLine> {
        val frames = frameClimatesForGameDay(table, localDawnDate, zone)
        if (frames.isEmpty()) return emptyList()
        if (frames.all { it.kind == SkyKind.DESPEJADO }) {
            return listOf(ClimateLine("Despejado todo el día", frame = false))
        }
        return frames
            .filter { it.kind != SkyKind.DESPEJADO }
            .map { ClimateLine(formatSpan(it, localDawnDate), frame = false) }
    }

    fun conditionAt(table: SkyTable, instant: Instant, zone: ZoneId): SkySlot? {
        val local = instant.atZone(zone)
        val day = table.on(local.toLocalDate()) ?: return null
        return day.slots.firstOrNull { slot ->
            val (start, end) = localRange(local.toLocalDate(), slot, zone)
            !instant.isBefore(start.toInstant()) && instant.isBefore(end.toInstant())
        }
    }

    fun barSegmentsForGameDay(table: SkyTable, localDawnDate: LocalDate, zone: ZoneId): List<SkyBarSegment> =
        spansForGameDay(table, localDawnDate, zone).map { span ->
            val minutes = Duration.between(span.start.toInstant(), span.end.toInstant()).toMinutes().toInt()
            SkyBarSegment(span.kind, minutes.coerceAtLeast(1))
        }

    /** Cuatro columnas, una por franja del juego y un solo clima en cada una. */
    fun barFrameSegmentsForGameDay(
        table: SkyTable,
        localDawnDate: LocalDate,
        zone: ZoneId,
    ): List<List<SkyBarSegment>> =
        windowsForLocalDawn(localDawnDate, zone).map { window ->
            val spans = spansInWindow(table, window, zone)
            val kind = if (spans.isEmpty()) SkyKind.SIN_DATOS else dominantKind(spans)
            listOf(SkyBarSegment(kind, 1))
        }

    private fun dominantKind(spans: List<GameClimateSpan>): SkyKind {
        val rank = listOf(SkyKind.DESPEJADO, SkyKind.LLUVIA, SkyKind.ARCOIRIS, SkyKind.NIEVE, SkyKind.METEORO)
        return spans
            .groupBy { it.kind }
            .maxWith(
                compareBy<Map.Entry<SkyKind, List<GameClimateSpan>>>(
                    { entry -> entry.value.sumOf { Duration.between(it.start.toInstant(), it.end.toInstant()).toMinutes() } },
                    { entry -> rank.indexOf(entry.key) },
                ),
            )
            .key
    }

    fun formatSpan(span: GameClimateSpan, focusLocalDate: LocalDate): String =
        "${span.label()} ${rangeLabel(span.start, span.end, focusLocalDate)}"

    fun summaryForGameDay(table: SkyTable, localDawnDate: LocalDate, zone: ZoneId): String =
        linesForGameDay(table, localDawnDate, zone)
            .joinToString(" · ") { it.text }
            .ifBlank { "Sin datos de clima" }

    private fun spansInWindow(table: SkyTable, window: LocalGameWindow, zone: ZoneId): List<GameClimateSpan> {
        val raw = ArrayList<GameClimateSpan>()
        var date = window.start.toLocalDate().minusDays(1)
        val last = window.end.toLocalDate()
        while (!date.isAfter(last)) {
            val day = table.on(date)
            if (day != null) {
                for (slot in day.slots) {
                    val (start, end) = localRange(date, slot, zone)
                    val clipStart = maxOf(start.toInstant(), window.start.toInstant())
                    val clipEnd = minOf(end.toInstant(), window.end.toInstant())
                    if (!clipStart.isBefore(clipEnd)) continue
                    raw += GameClimateSpan(
                        kind = slot.kind.climateDisplay(),
                        status = slot.status,
                        start = clipStart.atZone(zone),
                        end = clipEnd.atZone(zone),
                        frameKey = window.slot.key,
                    )
                }
            }
            date = date.plusDays(1)
        }
        raw.sortBy { it.start.toInstant() }
        return mergeInsideFrame(raw)
    }

    private fun dawnUtcDate(localDawnDate: LocalDate, zone: ZoneId): LocalDate {
        for (delta in -1L..1L) {
            val candidate = localDawnDate.plusDays(delta)
            val start = DexSchedule.window("Dawn", candidate, zone).start
            if (start.toLocalDate() == localDawnDate) return candidate
        }
        return localDawnDate
    }

    private fun frameTitle(window: LocalGameWindow, focusLocalDate: LocalDate): String =
        "${window.slot.label} ${rangeLabel(window.start, window.end, focusLocalDate)}"

    private fun rangeLabel(start: ZonedDateTime, end: ZonedDateTime, focusLocalDate: LocalDate): String {
        val t1 = start.toLocalTime().format(clock)
        val t2 = end.toLocalTime().format(clock)
        val nextDay = start.toLocalDate().isAfter(focusLocalDate) || end.toLocalDate().isAfter(focusLocalDate)
        return if (nextDay) "$t1–$t2 del día siguiente" else "$t1–$t2"
    }

    /**
     * Las horas del archivo son hora civil de [date] en [zone]. Un fin a medianoche
     * o un tramo que no avanza cae en el día siguiente.
     */
    private fun localRange(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<ZonedDateTime, ZonedDateTime> {
        val start = ZonedDateTime.of(date, slot.start, zone)
        val end = if (slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start)) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, zone)
        } else {
            ZonedDateTime.of(date, slot.end, zone)
        }
        return start to end
    }

    private fun mergeInsideFrame(spans: List<GameClimateSpan>): List<GameClimateSpan> {
        if (spans.isEmpty()) return spans
        val out = ArrayList<GameClimateSpan>()
        for (span in spans) {
            val prev = out.lastOrNull()
            if (
                prev != null &&
                prev.frameKey == span.frameKey &&
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
