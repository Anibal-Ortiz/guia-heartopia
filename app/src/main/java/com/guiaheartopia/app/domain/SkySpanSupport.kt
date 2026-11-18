package com.guiaheartopia.app.domain

import java.time.Instant

/**
 * Helper compartido para unir tramos de clima contiguos del mismo tipo.
 * Lo usan SkyWeatherLocal y SkyWeatherGameDay para no duplicar la lógica.
 */
object SkySpanSupport {
    fun mergeAdjacent(sorted: List<SkySpan>): List<SkySpan> {
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

    fun mergeInsideFrame(spans: List<SkyWeatherGameDay.GameClimateSpan>): List<SkyWeatherGameDay.GameClimateSpan> {
        if (spans.isEmpty()) return spans
        val out = ArrayList<SkyWeatherGameDay.GameClimateSpan>()
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

    /** Último instante del día civil inclusivo en la zona de origen. */
    fun inclusiveEndOfDay(date: java.time.LocalDate, zone: java.time.ZoneId): Instant =
        date.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
}
