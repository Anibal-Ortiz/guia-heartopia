package com.guiaheartopia.app

import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPlanTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val server = ZoneId.of("Etc/GMT+5")

    @Test
    fun rainWarnsThirtyMinutesBeforeInChileLocalTime() {
        val day = sky(
            slot(10, 12, SkyKind.LLUVIA, "lluvia"),
            slot(14, 16, SkyKind.DESPEJADO, "despejado"),
        )
        val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, santiago)
        val notices = ReminderPlan.weatherNotices(listOf(day), santiago, now)
        assertEquals(1, notices.size)
        assertEquals("En 30 min: Lluvia (11:00)", notices[0].body)
        assertEquals(10, notices[0].fireAt.hour)
        assertEquals(30, notices[0].fireAt.minute)
        val serverStart = ReminderPlan.serverRange(
            day.date,
            day.slots.first(),
            server,
            santiago,
        ).first
        assertEquals(serverStart.minusMinutes(30).toInstant(), notices[0].fireAt.toInstant())
    }

    @Test
    fun nubladoDoesNotSchedule() {
        val day = sky(slot(10, 12, SkyKind.NUBLADO, "nublado"))
        val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, santiago)
        assertTrue(ReminderPlan.weatherNotices(listOf(day), santiago, now).isEmpty())
    }

    @Test
    fun arcoirisAndMeteoritosScheduleAlerts() {
        val day = sky(
            slot(18, 19, SkyKind.ARCOIRIS, "arcoiris"),
            slot(20, 21, SkyKind.METEORO, "meteoro"),
        )
        val now = ZonedDateTime.of(2026, 6, 2, 12, 0, 0, 0, santiago)
        val notices = ReminderPlan.weatherNotices(listOf(day), santiago, now).sortedBy { it.fireAt.toInstant() }
        assertEquals(2, notices.size)
        assertEquals("En 30 min: Arcoíris (19:00)", notices[0].body)
        assertEquals("En 30 min: Lluvia de meteoritos (21:00)", notices[1].body)
    }

    @Test
    fun adjacentRainBandsBecomeOneNotice() {
        val day = sky(
            slot(10, 12, SkyKind.LLUVIA, "lluvia"),
            slot(12, 14, SkyKind.LLUVIA, "lluvia"),
        )
        val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, santiago)
        val notices = ReminderPlan.weatherNotices(listOf(day), santiago, now)
        assertEquals(listOf("En 30 min: Lluvia (11:00)"), notices.map { it.body })
    }

    @Test
    fun serverNoonLandsAnHourEarlierInSantiagoAfterTheAprilChange() {
        val before = ReminderPlan.serverToUser(LocalDateTime.of(2026, 4, 4, 12, 0), server, santiago)
        val after = ReminderPlan.serverToUser(LocalDateTime.of(2026, 4, 6, 12, 0), server, santiago)
        assertEquals(14, before.hour)
        assertEquals(13, after.hour)
        assertEquals(santiago, before.zone)
        assertEquals(santiago, after.zone)
    }

    @Test
    fun hobbyEventStaysInsideItsWindow() {
        val raw = """
            {"zonaFuente":"Etc/GMT+5","servidor":"America","eventos":[
              {"id":"pesca-altamar","tipo":"aficion","categoria":"pesca","nombre":"Pesca de altamar",
               "repeticion":"diaria","horas":["12:00"],"inicio":"2026-04-04T00:00","fin":"2026-04-04T12:00","avisoMinAntes":15}
            ]}
        """.trimIndent()
        val prefs = ReminderPrefs(pescaEnabled = true)
        val now = ZonedDateTime.of(2026, 4, 4, 8, 0, 0, 0, santiago)
        val notices = ReminderPlan.eventNotices(ReminderPlan.parseEvents(raw), prefs, santiago, now)
        assertEquals(1, notices.size)
        assertEquals("En 15 min: Pesca de altamar", notices[0].body)
        assertEquals(13, notices[0].fireAt.hour)
        assertEquals(45, notices[0].fireAt.minute)
        val later = ZonedDateTime.of(2026, 4, 5, 8, 0, 0, 0, santiago)
        assertTrue(ReminderPlan.eventNotices(ReminderPlan.parseEvents(raw), prefs, santiago, later).isEmpty())
    }

    @Test
    fun seasonWarnsOneDayBeforeTheEnd() {
        val raw = """
            {"zonaFuente":"Etc/GMT+5","eventos":[
              {"id":"halloween-noche-grimkin","tipo":"temporada","categoria":"temporada",
               "nombre":"Noche grimkin","inicio":"2026-10-30T06:00","fin":"2026-11-16T05:59"}
            ]}
        """.trimIndent()
        val doc = ReminderPlan.parseEvents(raw)
        val end = ReminderPlan.serverToUser(LocalDateTime.of(2026, 11, 16, 5, 59), server, santiago)
        val now = end.minusHours(30)
        val notices = ReminderPlan.eventNotices(doc, ReminderPrefs(temporadaEnabled = true), santiago, now)
        val day = notices.single { it.body == "Queda 1 día" }
        assertEquals(end.minusHours(24).toInstant(), day.fireAt.toInstant())
        val active = ReminderPlan.activeSeasons(doc, santiago, now)
        assertEquals("Noche grimkin", active.single().name)
        assertEquals("Quedan 1 día y 6 h", active.single().remaining)
    }

    @Test
    fun missingOrBrokenEventsFileSchedulesNothing() {
        val now = ZonedDateTime.of(2026, 6, 2, 8, 0, 0, 0, santiago)
        val prefs = ReminderPrefs(pescaEnabled = true, temporadaEnabled = true)
        assertNull(ReminderPlan.parseEvents(null))
        assertNull(ReminderPlan.parseEvents("{"))
        assertTrue(ReminderPlan.eventNotices(null, prefs, santiago, now).isEmpty())
        assertTrue(ReminderPlan.activeSeasons(null, santiago, now).isEmpty())
    }

    private fun sky(vararg slots: SkySlot) = SkyDay(LocalDate.of(2026, 6, 2), slots.toList())

    private fun slot(from: Int, to: Int, kind: SkyKind, rawKind: String = "") = SkySlot(
        start = LocalTime.of(from, 0),
        end = LocalTime.of(to, 0),
        kind = kind,
        status = SkyStatus.ACUERDO,
        rawKind = rawKind,
    )
}
