package com.guiaheartopia.app.share

import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.ResourceCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Construcción pura del mes. La fuente única sigue siendo [ResourceCalendar]. */
object CommunityCalendarGrid {
    val WEEK_LABELS: List<String> = listOf("L", "M", "X", "J", "V", "S", "D")
    private val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("es"))

    fun monthTitle(month: YearMonth): String =
        month.format(MONTH_FORMAT).replaceFirstChar { it.titlecase() }

    fun shortSpot(raw: String?): String? = when (raw) {
        null -> null
        "Bosque" -> "B"
        "Ruinas" -> "R"
        else -> raw
    }

    fun cells(
        month: YearMonth,
        today: LocalDate,
        selected: LocalDate,
        eventDays: Set<LocalDate> = emptySet(),
    ): List<CommunityCalendarCell> {
        val first = month.atDay(1)
        val lead = (first.dayOfWeek.value + 6) % 7
        val total = ((lead + month.lengthOfMonth() + 6) / 7) * 7
        return (0 until total).map { index ->
            val dayNumber = index - lead + 1
            if (dayNumber !in 1..month.lengthOfMonth()) {
                CommunityCalendarCell(null, 0, false, false, null, null, false, false)
            } else {
                val date = month.atDay(dayNumber)
                val data = ResourceCalendar.on(date)
                CommunityCalendarCell(
                    date = date,
                    dayNumber = dayNumber,
                    isToday = date == today,
                    isSelected = date == selected,
                    liuliuShort = shortSpot(data.liuliu),
                    fluritaShort = shortSpot(data.flurita),
                    hasEvent = date in eventDays,
                    hasData = data.certainty == DayCertainty.PUBLICADO,
                )
            }
        }
    }
}
