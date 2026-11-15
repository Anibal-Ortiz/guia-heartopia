package com.guiaheartopia.app

import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.domain.DexCategoryFilters
import com.guiaheartopia.app.domain.GuideLaunch
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderIds
import com.guiaheartopia.app.domain.CreatureWindowReminders
import com.guiaheartopia.app.domain.CreatureWindowAlarm
import com.guiaheartopia.app.domain.dexEntryMatchesEvent
import com.guiaheartopia.app.domain.mergeGuideLaunch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideDexPart2Test {
    @Test
    fun unredeemedFiltersRedeemedCodes() {
        val today = LocalDate.of(2026, 11, 14)
        val codes = RedeemCodes.parse(
            """[{"code":"A","recompensa":"x"},{"code":"B","recompensa":"y"}]""",
        )
        val active = RedeemCodes.active(codes, today)
        assertEquals(listOf("B"), RedeemCodes.unredeemed(active, setOf("A")).map { it.code })
    }

    @Test
    fun mergeGuideLaunchAppliesShortcuts() {
        val base = DexCategoryFilters(registered = "todos", onlyNow = false)
        val merged = mergeGuideLaunch(base, GuideLaunch(onlyNow = true, registered = "no"))
        assertTrue(merged.onlyNow)
        assertEquals("no", merged.registered)
    }

    @Test
    fun creatureWindowIdsStayStable() {
        val a = ReminderIds.creatureWindow("pez-1", 1_700_000_000_000)
        val b = ReminderIds.creatureWindow("pez-1", 1_700_000_000_000)
        assertEquals(a, b)
    }

    @Test
    fun eventCalendarDaysMarksSeasonSpan() {
        val raw = """
            {"zonaFuente":"Etc/GMT+5","eventos":[
              {"id":"evt","tipo":"temporada","nombre":"Fiesta",
               "inicio":"2026-11-10T00:00","fin":"2026-11-12T23:59"}
            ]}
        """.trimIndent()
        val doc = ReminderPlan.parseEvents(raw)!!
        val zone = ZoneId.of("America/Santiago")
        val days = ReminderPlan.eventCalendarDays(doc, zone)
        assertTrue(days.contains(LocalDate.of(2026, 11, 10)))
        assertTrue(days.contains(LocalDate.of(2026, 11, 11)))
    }

    @Test
    fun dexEntryMatchesEventByName() {
        val entry = DexEntry(id = "x", name = "Pez", category = "Peces", event = "Fiesta")
        assertTrue(dexEntryMatchesEvent(entry, "evt", "Fiesta"))
    }

    @Test
    fun creatureWindowRemindersSkipsPastAlarms() {
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, zone)
        val alarms = listOf(
            CreatureWindowAlarm("a", "Pez", now.minusHours(1).toInstant().toEpochMilli()),
            CreatureWindowAlarm("b", "Ave", now.plusHours(1).toInstant().toEpochMilli()),
        )
        val notices = CreatureWindowReminders.notices(alarms, zone, now)
        assertEquals(1, notices.size)
        assertEquals("Ya puedes buscar Ave.", notices[0].body)
    }
}