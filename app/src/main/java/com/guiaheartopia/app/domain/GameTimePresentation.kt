package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class GameTimeRow(
    val slotKey: String,
    val title: String,
    val hours: String,
    val isCurrent: Boolean,
)

data class GameTimeCardModel(
    val currentTitle: String,
    val untilLabel: String,
    val localClock: String,
    val rows: List<GameTimeRow>,
)

object GameTimePresentation {
    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    /** Estado de la tarjeta «Franja del juego» (Inicio / Clima) para pruebas y UI. */
    fun cardModel(zoneId: String, instant: Instant): GameTimeCardModel {
        val zone = DexSchedule.zoneOf(zoneId)
        val now = instant.atZone(zone)
        val current = DexSchedule.currentWindow(zone, instant)
        val windows = DexSchedule.windowsAround(zone, instant)
        return GameTimeCardModel(
            currentTitle = DexSchedule.titled(current.slot),
            untilLabel = DexSchedule.untilLabel(current, now),
            localClock = now.format(clock),
            rows = windows.map { window ->
                GameTimeRow(
                    slotKey = window.slot.key,
                    title = DexSchedule.titled(window.slot),
                    hours = DexSchedule.hoursLabel(window),
                    isCurrent = window.slot.key == current.slot.key,
                )
            },
        )
    }
}
