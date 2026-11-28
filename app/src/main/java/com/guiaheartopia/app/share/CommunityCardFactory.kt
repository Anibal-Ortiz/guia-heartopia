package com.guiaheartopia.app.share

import com.guiaheartopia.app.data.SkyTable
import com.guiaheartopia.app.domain.Country
import com.guiaheartopia.app.domain.CountryZone
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import com.guiaheartopia.app.domain.climateDisplay
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Funciones pura para construir [CommunityCard]. */
object CommunityCardFactory {
    private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.forLanguageTag("es"))
    private val DAY_TITLE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
    private val HM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    const val FOOTER = "Datos de la comunidad, no oficiales"
    const val SOURCE =
        "Fluorita y Liuliu: ciclo diario de 50 días (noviembre 2026); clima: ciclo anual. Sin datos fuera de lo publicado."

    val CALENDAR_LEGEND = listOf(
        "Número verde: Liuliu",
        "Número naranja: Fluorita",
        "B bosque · R ruinas",
        "Sin datos = sin información publicada",
    )

    val WEATHER_LEGEND = listOf(
        "Franjas del día del servidor: Noche, Amanecer, Día, Tarde",
        "Despejado ☀️ · Lluvia 🌧️ · Arcoíris 🌈 · Nieve ❄️ · Meteoritos ☄️ · Sin datos",
        "Número verde: Liuliu · Número naranja: Fluorita",
        "B bosque · R ruinas · Sin datos = sin información publicada",
    )

    fun calendarCard(
        country: Country,
        chosenZone: CountryZone,
        zone: ZoneId,
        month: YearMonth,
        today: LocalDate,
        selected: LocalDate,
        now: ZonedDateTime,
        eventDays: Set<LocalDate> = emptySet(),
    ): CommunityCard {
        val zoneLabel = Countries.zoneLabel(zone.id).ifBlank { chosenZone.label }
        val monthTitle = CommunityCalendarGrid.monthTitle(month)
        return CommunityCard(
            kind = CommunityImageKind.CALENDAR,
            title = "Calendario Heartopia",
            subtitle = "Imagen para comunidades · $monthTitle",
            countryName = country.name,
            zoneLabel = zoneLabel,
            zoneId = zone.id,
            clockLabel = now.format(CLOCK),
            selectedDay = day(selected, title = selected.format(DAY_TITLE).replaceFirstChar { it.titlecase() }),
            month = month,
            monthTitle = monthTitle,
            cells = CommunityCalendarGrid.cells(month, today, selected, eventDays),
            legend = CALENDAR_LEGEND,
            footer = FOOTER,
            sourceLabel = SOURCE,
        )
    }

    fun weatherCard(
        country: Country,
        chosenZone: CountryZone,
        zone: ZoneId,
        sky: SkyTable,
        date: LocalDate,
        now: ZonedDateTime,
    ): CommunityCard {
        val zoneLabel = Countries.zoneLabel(zone.id).ifBlank { chosenZone.label }
        val dayTitle = date.format(DAY_TITLE).replaceFirstChar { it.titlecase() }
        val windows = SkyWeatherGameDay.windowsForLocalDawn(date, zone)
        val frameSpans = SkyWeatherGameDay.frameClimatesForGameDay(sky, date, zone).associateBy { it.frameKey }
        val barSegments = SkyWeatherGameDay.barFrameSegmentsForGameDay(sky, date, zone)
        val frames = windows.mapIndexed { index, window ->
            val span = frameSpans[window.slot.key]
            val kind = span?.kind?.climateDisplay() ?: SkyKind.SIN_DATOS
            CommunityFrame(
                frameKey = window.slot.key,
                title = window.slot.label,
                timeRange = rangeLabel(window.start.toLocalTime(), window.end.toLocalTime()),
                label = kind.label,
                emoji = kind.emoji,
                kind = kind,
                detail = span?.detail.orEmpty(),
                hasData = span != null,
                segments = barSegments.getOrNull(index).orEmpty().map {
                    CommunityClimateSegment(it.kind.climateDisplay(), it.weight)
                },
            )
        }
        val summary = SkyWeatherGameDay.compactLinesForGameDay(sky, date, zone)
            .joinToString(" · ") { it.text }
            .ifBlank { "Sin datos de clima para este día." }
        return CommunityCard(
            kind = CommunityImageKind.WEATHER,
            title = "Clima Heartopia",
            subtitle = "Imagen para comunidades · $dayTitle",
            countryName = country.name,
            zoneLabel = zoneLabel,
            zoneId = zone.id,
            clockLabel = now.format(CLOCK),
            selectedDay = day(date, title = dayTitle),
            frames = frames,
            weatherSummary = summary,
            legend = WEATHER_LEGEND,
            footer = FOOTER,
            sourceLabel = SOURCE,
        )
    }

    fun day(date: LocalDate, title: String): CommunityDay {
        val data = ResourceCalendar.on(date)
        return CommunityDay(
            date = date,
            title = title,
            liuliuLabel = ResourceCalendar.label(data.liuliu),
            fluritaLabel = ResourceCalendar.label(data.flurita),
            certaintyLabel = data.certainty.label.ifBlank { DayCertainty.PUBLICADO.name },
            hasData = data.certainty == DayCertainty.PUBLICADO,
        )
    }

    fun rangeLabel(start: LocalTime, end: LocalTime): String {
        val t1 = start.format(HM)
        val t2 = end.format(HM)
        return "$t1–$t2"
    }
}
