package com.guiaheartopia.app.domain

import java.time.LocalDate
import java.time.ZoneId

data class GameEvent(
    val id: String,
    val title: String,
    val summary: String,
    val start: LocalDate?,
    val end: LocalDate?,
    val verified: Boolean,
    val sources: String,
)

object EventTime {
    /**
     * Fechas civiles inclusivas en la zona de origen, vistas como días locales.
     * Si el cierre cae tarde, en una zona más oriental puede pasar al día siguiente.
     */
    fun localSpan(start: LocalDate, end: LocalDate, origin: ZoneId, viewer: ZoneId): Pair<LocalDate, LocalDate> {
        val from = start.atStartOfDay(origin).withZoneSameInstant(viewer).toLocalDate()
        val to = end.atTime(23, 59).atZone(origin).withZoneSameInstant(viewer).toLocalDate()
        return from to to
    }

    fun covers(start: LocalDate, end: LocalDate, origin: ZoneId, viewer: ZoneId, day: LocalDate): Boolean {
        val (from, to) = localSpan(start, end, origin, viewer)
        return !day.isBefore(from) && !day.isAfter(to)
    }
}
