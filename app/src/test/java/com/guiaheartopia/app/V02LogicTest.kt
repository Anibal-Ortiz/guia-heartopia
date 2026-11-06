package com.guiaheartopia.app

import com.guiaheartopia.app.data.VerifiedCatalog
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.EventTime
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPlanner
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V02LogicTest {
    @Test
    fun backupRoundTripKeepsProfileAndFoundItems() {
        val original = GuideSnapshot(
            profile = PlayerProfile(onboarded = true, name = "Nico", countryId = "CL", zoneId = "America/Santiago"),
            foundIds = listOf("fish-blue-sea-angel"),
        )
        val raw = BackupCodec.export(original)
        val imported = BackupCodec.import(raw)
        assertTrue(imported is ImportResult.Ok)
        val snapshot = (imported as ImportResult.Ok).snapshot
        assertEquals("Nico", snapshot.profile.name)
        assertEquals(listOf("fish-blue-sea-angel"), snapshot.foundIds)
        assertEquals(original.profile.zoneId, snapshot.profile.zoneId)
    }

    @Test
    fun backupRejectsAnotherFormat() {
        val raw = """{"format":9,"app":"guia-heartopia","snapshot":{}}"""
        val result = BackupCodec.import(raw)
        assertTrue(result is ImportResult.Invalid)
    }

    @Test
    fun reminderUsesTheSelectedZoneAndSkipsToTomorrowAfterTheHour() {
        val santiago = ZoneId.of("America/Santiago")
        val morning = ZonedDateTime.of(2026, 1, 15, 7, 30, 0, 0, santiago)
        val later = ReminderPlanner.next(morning, 9 * 60)
        assertEquals(santiago, later.zone)
        assertEquals(LocalDate.of(2026, 1, 15), later.toLocalDate())
        assertEquals(9, later.hour)

        val after = ZonedDateTime.of(2026, 1, 15, 10, 0, 0, 0, santiago)
        val nextDay = ReminderPlanner.next(after, 9 * 60)
        assertEquals(LocalDate.of(2026, 1, 16), nextDay.toLocalDate())
    }

    @Test
    fun reminderSurvivesASpringForwardGap() {
        val newYork = ZoneId.of("America/New_York")
        val beforeGap = ZonedDateTime.of(2026, 3, 8, 1, 0, 0, 0, newYork)
        val next = ReminderPlanner.next(beforeGap, 2 * 60 + 30)
        assertTrue(next.toInstant().isAfter(beforeGap.toInstant()))
        assertEquals(newYork, next.zone)
    }

    @Test
    fun eventEndCanLandOnTheNextLocalDay() {
        val start = LocalDate.of(2026, 11, 20)
        val end = start
        val (from, to) = EventTime.localSpan(start, end, ZoneOffset.UTC, ZoneId.of("Asia/Tokyo"))
        assertEquals(LocalDate.of(2026, 11, 20), from)
        assertEquals(LocalDate.of(2026, 11, 21), to)
        assertTrue(EventTime.covers(start, end, ZoneOffset.UTC, ZoneId.of("Asia/Tokyo"), LocalDate.of(2026, 11, 21)))
    }

    @Test
    fun allDayScheduleCanBeMarkedNow() {
        assertTrue(DexSchedule.availableAllDay(listOf("Night", "Dawn", "Day", "Dusk")))
        assertTrue(!DexSchedule.availableAllDay(listOf("Dusk", "Night")))
    }

    @Test
    fun heartodexAssetHasTheFourCategories() {
        val file = listOf(
            File("src/main/assets/catalog/heartodex.json"),
            File("app/src/main/assets/catalog/heartodex.json"),
        ).first { it.exists() }
        val text = file.readText()
        assertTrue(text.contains("\"category\": \"Peces\""))
        assertTrue(text.contains("\"category\": \"Pájaros\""))
        assertTrue(text.contains("\"category\": \"Insectos\""))
        assertTrue(text.contains("\"category\": \"Fauna\""))
        assertTrue(text.contains("heartodex"))
    }

    @Test
    fun novemberSixthSpotsMatchTheTwoGuides() {
        val spot = VerifiedCatalog.novemberSpot(LocalDate.of(2026, 11, 6))
        assertEquals("8" to "8", spot)
        assertEquals(null, VerifiedCatalog.novemberSpot(LocalDate.of(2026, 12, 1)))
    }
}
