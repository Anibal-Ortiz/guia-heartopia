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
 * Las horas del JSON son hora civil de la zona elegida ([WeatherSource.zoneFor]);
 * la tabla se indexa con esa misma fecha. Las franjas del juego, en cambio, son UTC
 * fijo y se convierten con [DexSchedule] a la zona del jugador.
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
        val opening = windowsForLocalDawn(dawnDate, zone)
            .firstOrNull { it.slot.key == "Dawn" }?.start?.toInstant()
            ?: return dawnDate.atStartOfDay(zone).toInstant()
        val following = windowsForLocalDawn(dawnDate.plusDays(1), zone)
            .firstOrNull { it.slot.key == "Dawn" }?.start?.toInstant()
            ?: return dawnDate.plusDays(1).atStartOfDay(zone).toInstant()
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
        // El JSON es hora civil de la zona elegida: tabla y relojes comparten esa zona.
        val source = instant.atZone(WeatherSource.zoneFor(zone))
        val day = table.on(source.toLocalDate()) ?: return null
        return day.slots.firstOrNull { slot ->
            val (start, end) = clockRange(source.toLocalDate(), slot, zone)
            !instant.isBefore(start) && instant.isBefore(end)
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
        // Las horas del JSON son hora civil de la zona elegida; el recorte a la
        // franja se hace en instantes ya convertidos.
        for (date in SkyWeatherLocal.utcDatesBetween(window.start.toInstant(), window.end.toInstant(), zone)) {
            val day = table.on(date) ?: continue
            for (slot in day.slots) {
                val (start, end) = clockRange(date, slot, zone)
                val clipStart = maxOf(start, window.start.toInstant())
                val clipEnd = minOf(end, window.end.toInstant())
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
        raw.sortBy { it.start.toInstant() }
        return SkySpanSupport.mergeInsideFrame(raw)
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
     * Las horas del archivo son hora civil de [date] en la zona elegida [zone].
     * Un fin a medianoche o un tramo que no avanza cae en el día siguiente.
     */
    private fun clockRange(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<Instant, Instant> {
        val source = WeatherSource.zoneFor(zone)
        val start = ZonedDateTime.of(date, slot.start, source).toInstant()
        val end = if (slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start)) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, source).toInstant()
        } else {
            ZonedDateTime.of(date, slot.end, source).toInstant()
        }
        return start to end
    }
}
