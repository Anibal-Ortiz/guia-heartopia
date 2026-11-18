package com.guiaheartopia.app

import com.guiaheartopia.app.domain.PlannedNotice
import com.guiaheartopia.app.domain.ReminderIds
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.domain.ReminderSync
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderCollisionTest {
    private val zone = ZoneId.of("UTC")
    private val date = LocalDate.of(2026, 11, 14)

    @Test
    fun twoAlarmsAtTheSameHourStayDistinct() {
        val notices = coinciding(includeBirds = false)
        assertEquals(2, notices.size)
        assertEquals(2, notices.map { it.id }.distinct().size)
        assertEquals(2, notices.map { ReminderIds.notificationId(it.id) }.distinct().size)
        assertTrue(notices.any { it.id.startsWith("clima-") })
        assertTrue(notices.any { it.id.startsWith("aficion-") })
    }

    @Test
    fun threeAlarmsAtTheSameHourStayDistinct() {
        val notices = coinciding(includeBirds = true)
        assertEquals(3, notices.size)
        assertEquals(3, notices.map { it.id }.distinct().size)
        assertEquals(3, notices.map { ReminderIds.notificationId(it.id) }.distinct().size)
    }

    @Test
    fun rescheduleKeepsTheOthersAndDoesNotDuplicate() {
        val two = coinciding(includeBirds = false)
        val first = ReminderSync.plan(emptySet(), emptySet(), two)
        assertEquals(two.map { it.id }.toSet(), first.enqueue.map { it.id }.toSet())
        assertTrue(first.cancel.isEmpty())

        val again = ReminderSync.plan(first.enqueue.map { it.id }.toSet(), emptySet(), two)
        assertEquals(two.map { it.id }.toSet(), again.keep)
        assertTrue(again.enqueue.isEmpty())
        assertTrue(again.cancel.isEmpty())

        val three = coinciding(includeBirds = true)
        val added = ReminderSync.plan(again.keep, emptySet(), three)
        assertEquals(again.keep, added.keep)
        assertEquals(1, added.enqueue.size)
        assertTrue(added.cancel.isEmpty())
    }

    @Test
    fun aLateRainAlertIsNotDroppedWhenTheFishWaveIsStillPending() {
        val now = ZonedDateTime.of(date, LocalTime.of(19, 50), zone)
        val notices = coinciding(includeBirds = false, now = now)
        assertEquals(2, notices.size)
        assertTrue(notices.all { it.fireAt.toInstant().isBefore(now.toInstant()) })
    }

    @Test
    fun postedNoticeIsNotEnqueuedAgain() {
        val notices = coinciding(includeBirds = false)
        val posted = setOf(notices.first().id)
        val plan = ReminderSync.plan(emptySet(), posted, notices)
        assertTrue(plan.enqueue.none { it.id in posted })
        assertEquals(1, plan.enqueue.size)
    }

    private fun coinciding(includeBirds: Boolean, now: ZonedDateTime = ZonedDateTime.of(date, LocalTime.of(12, 0), zone)): List<PlannedNotice> {
        val start = ZonedDateTime.of(date, LocalTime.of(20, 0), zone).toInstant().toEpochMilli().toString()
        val weather = ReminderPlan.weatherNotices(
            listOf(
                SkyDay(
                    date,
                    listOf(
                        SkySlot(
                            start = LocalTime.of(20, 0),
                            end = LocalTime.of(21, 0),
                            kind = SkyKind.LLUVIA,
                            status = SkyStatus.ACUERDO,
                            rawKind = "lluvia",
                        ),
                    ),
                ),
            ),
            zone,
            now,
            sourceZone = zone,
        )
        val hobbies = ReminderPlan.eventNotices(
            ReminderPlan.parseEvents(events(includeBirds)),
            ReminderPrefs(pescaEnabled = true, avesEnabled = true, weatherEnabled = true, eventsEnabled = true),
            zone,
            now,
        )
        return (weather + hobbies).filter { it.id.endsWith("-$start") }
    }

    private fun events(includeBirds: Boolean): String {
        val birds = if (!includeBirds) {
            ""
        } else {
            """,{"id":"oleada-aves","tipo":"aficion","categoria":"aves","nombre":"Oleada de aves","repeticion":"diaria","horas":["20:00"],"avisoMinAntes":15}"""
        }
        return """
            {"zonaFuente":"UTC","eventos":[
              {"id":"oleada-peces","tipo":"aficion","categoria":"pesca","nombre":"Oleada de peces","repeticion":"diaria","horas":["20:00"],"avisoMinAntes":15}$birds
            ]}
        """.trimIndent()
    }
}
