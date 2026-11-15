package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.SkyTable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object CreatureAppearing {
    data class NextWindow(
        val start: ZonedDateTime,
        val end: ZonedDateTime,
        val slot: GameSlot,
    )

    fun nextWindow(
        schedule: List<String>,
        weather: List<String>,
        sky: SkyTable,
        zone: ZoneId,
        from: Instant = Instant.now(),
        horizonDays: Long = 14,
    ): NextWindow? {
        if (schedule.isEmpty()) return null
        var cursor = from
        val limit = from.plus(horizonDays, ChronoUnit.DAYS)
        var hops = 0
        while (cursor.isBefore(limit) && hops < 400) {
            hops++
            val windows = DexSchedule.windowsAround(zone, cursor)
                .filter { it.slot.key in schedule }
                .sortedBy { it.start.toInstant() }
            for (window in windows) {
                val start = window.start.toInstant()
                if (!start.isAfter(from)) continue
                val sample = start.plusSeconds(120)
                if (SkyWeather.availableNow(schedule, weather, sample, sky, zone)) {
                    return NextWindow(window.start, window.end, window.slot)
                }
            }
            val current = DexSchedule.currentWindow(zone, cursor)
            cursor = current.end.toInstant()
        }
        return null
    }

    fun userLabel(window: NextWindow, zone: ZoneId): String {
        val dayClock = DateTimeFormatter.ofPattern("EEE d MMM HH:mm", Locale.forLanguageTag("es"))
        val inZone = window.start.withZoneSameInstant(zone)
        return "${DexSchedule.titled(window.slot)} · ${inZone.format(dayClock)}"
    }
}