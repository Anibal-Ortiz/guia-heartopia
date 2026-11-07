package com.guiaheartopia.app

import com.guiaheartopia.app.domain.DexSchedule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DexScheduleTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val mexico = ZoneId.of("America/Mexico_City")
    private val madrid = ZoneId.of("Europe/Madrid")
    private val buenosAires = ZoneId.of("America/Argentina/Buenos_Aires")
    private val losAngeles = ZoneId.of("America/Los_Angeles")

    @Test
    fun utcSlotsMatchChileOn6Nov2026() {
        val slots = DexSchedule.UTC_SLOTS.associateBy { it.key }
        assertEquals(LocalTime.of(5, 0), slots.getValue("Dawn").start)
        assertEquals(LocalTime.of(11, 0), slots.getValue("Dawn").end)
        assertEquals(LocalTime.of(11, 0), slots.getValue("Day").start)
        assertEquals(LocalTime.of(17, 0), slots.getValue("Day").end)
        assertEquals(LocalTime.of(17, 0), slots.getValue("Dusk").start)
        assertEquals(LocalTime.of(23, 0), slots.getValue("Dusk").end)
        assertEquals(LocalTime.of(23, 0), slots.getValue("Night").start)
        assertEquals(LocalTime.of(5, 0), slots.getValue("Night").end)
        assertEquals("Tarde", slots.getValue("Dusk").label)
        assertTrue(slots.getValue("Night").crossesMidnight)

        val day = LocalDate.of(2026, 11, 6)
        assertWindow(santiago, day, "Dawn", "2026-11-06T02:00-03:00", "2026-11-06T08:00-03:00")
        assertWindow(santiago, day, "Day", "2026-11-06T08:00-03:00", "2026-11-06T14:00-03:00")
        assertWindow(santiago, day, "Dusk", "2026-11-06T14:00-03:00", "2026-11-06T20:00-03:00")
        assertWindow(santiago, day, "Night", "2026-11-06T20:00-03:00", "2026-11-07T02:00-03:00")
        assertEquals(ZoneOffset.ofHours(-3), santiago.rules.getOffset(Instant.parse("2026-11-06T12:00:00Z")))
    }

    @Test
    fun localHoursFollowEachCountryZone() {
        val november = LocalDate.of(2026, 11, 6)
        val july = LocalDate.of(2026, 7, 15)

        assertWindow(mexico, november, "Dawn", "2026-11-05T23:00-06:00", "2026-11-06T05:00-06:00")
        assertWindow(mexico, november, "Day", "2026-11-06T05:00-06:00", "2026-11-06T11:00-06:00")
        assertWindow(mexico, november, "Dusk", "2026-11-06T11:00-06:00", "2026-11-06T17:00-06:00")
        assertWindow(mexico, november, "Night", "2026-11-06T17:00-06:00", "2026-11-06T23:00-06:00")
        assertWindow(mexico, july, "Day", "2026-07-15T05:00-06:00", "2026-07-15T11:00-06:00")

        assertWindow(buenosAires, november, "Dawn", "2026-11-06T02:00-03:00", "2026-11-06T08:00-03:00")
        assertWindow(buenosAires, november, "Day", "2026-11-06T08:00-03:00", "2026-11-06T14:00-03:00")
        assertWindow(buenosAires, november, "Dusk", "2026-11-06T14:00-03:00", "2026-11-06T20:00-03:00")
        assertWindow(buenosAires, november, "Night", "2026-11-06T20:00-03:00", "2026-11-07T02:00-03:00")
        assertWindow(buenosAires, july, "Day", "2026-07-15T08:00-03:00", "2026-07-15T14:00-03:00")

        assertWindow(madrid, november, "Dawn", "2026-11-06T06:00+01:00", "2026-11-06T12:00+01:00")
        assertWindow(madrid, november, "Day", "2026-11-06T12:00+01:00", "2026-11-06T18:00+01:00")
        assertWindow(madrid, november, "Dusk", "2026-11-06T18:00+01:00", "2026-11-07T00:00+01:00")
        assertWindow(madrid, november, "Night", "2026-11-07T00:00+01:00", "2026-11-07T06:00+01:00")
        assertWindow(madrid, july, "Dawn", "2026-07-15T07:00+02:00", "2026-07-15T13:00+02:00")
        assertWindow(madrid, july, "Day", "2026-07-15T13:00+02:00", "2026-07-15T19:00+02:00")
        assertWindow(madrid, july, "Dusk", "2026-07-15T19:00+02:00", "2026-07-16T01:00+02:00")
        assertWindow(madrid, july, "Night", "2026-07-16T01:00+02:00", "2026-07-16T07:00+02:00")

        assertWindow(losAngeles, november, "Dawn", "2026-11-05T21:00-08:00", "2026-11-06T03:00-08:00")
        assertWindow(losAngeles, november, "Day", "2026-11-06T03:00-08:00", "2026-11-06T09:00-08:00")
        assertWindow(losAngeles, november, "Dusk", "2026-11-06T09:00-08:00", "2026-11-06T15:00-08:00")
        assertWindow(losAngeles, november, "Night", "2026-11-06T15:00-08:00", "2026-11-06T21:00-08:00")
        assertWindow(losAngeles, july, "Dawn", "2026-07-14T22:00-07:00", "2026-07-15T04:00-07:00")
        assertWindow(losAngeles, july, "Day", "2026-07-15T04:00-07:00", "2026-07-15T10:00-07:00")
        assertWindow(losAngeles, july, "Dusk", "2026-07-15T10:00-07:00", "2026-07-15T16:00-07:00")
        assertWindow(losAngeles, july, "Night", "2026-07-15T16:00-07:00", "2026-07-15T22:00-07:00")

        assertWindow(santiago, july, "Dawn", "2026-07-15T01:00-04:00", "2026-07-15T07:00-04:00")
        assertWindow(santiago, july, "Day", "2026-07-15T07:00-04:00", "2026-07-15T13:00-04:00")
        assertWindow(santiago, july, "Dusk", "2026-07-15T13:00-04:00", "2026-07-15T19:00-04:00")
        assertWindow(santiago, july, "Night", "2026-07-15T19:00-04:00", "2026-07-16T01:00-04:00")
    }

    @Test
    fun nightCrossesUtcMidnightAndEndsAtFive() {
        val late = Instant.parse("2026-11-06T23:30:00Z")
        val afterMidnight = Instant.parse("2026-11-07T00:30:00Z")
        val lastMinute = Instant.parse("2026-11-07T04:59:00Z")
        val dawn = Instant.parse("2026-11-07T05:00:00Z")
        assertEquals("Night", DexSchedule.slotAt(late).key)
        assertEquals("Night", DexSchedule.slotAt(afterMidnight).key)
        assertEquals("Night", DexSchedule.slotAt(lastMinute).key)
        assertEquals("Dawn", DexSchedule.slotAt(dawn).key)

        val window = DexSchedule.currentWindow(santiago, afterMidnight)
        assertEquals("Night", window.slot.key)
        assertEquals(Instant.parse("2026-11-06T23:00:00Z"), window.start.toInstant())
        assertEquals(Instant.parse("2026-11-07T05:00:00Z"), window.end.toInstant())
        assertEquals(LocalTime.of(2, 0), window.end.toLocalTime())
        assertTrue(window.end.toLocalDate().isAfter(window.start.toLocalDate()))
        assertTrue(DexSchedule.availableNow(listOf("Night"), afterMidnight))
        assertFalse(DexSchedule.availableNow(listOf("Day"), afterMidnight))

        val losAngelesNow = afterMidnight.atZone(losAngeles)
        assertEquals("hasta las 21:00", DexSchedule.untilLabel(DexSchedule.currentWindow(losAngeles, afterMidnight), losAngelesNow))
        val santiagoNow = afterMidnight.atZone(santiago)
        assertEquals(LocalTime.of(21, 30), santiagoNow.toLocalTime())
        assertEquals("hasta mañana a las 02:00", DexSchedule.untilLabel(window, santiagoNow))
    }

    @Test
    fun slotBoundariesAreExclusiveAtTheEnd() {
        assertEquals("Dawn", DexSchedule.slotAt(Instant.parse("2026-11-06T05:00:00Z")).key)
        assertEquals("Dawn", DexSchedule.slotAt(Instant.parse("2026-11-06T10:59:59Z")).key)
        assertEquals("Day", DexSchedule.slotAt(Instant.parse("2026-11-06T11:00:00Z")).key)
        assertEquals("Dusk", DexSchedule.slotAt(Instant.parse("2026-11-06T17:00:00Z")).key)
        assertEquals("Night", DexSchedule.slotAt(Instant.parse("2026-11-06T23:00:00Z")).key)
    }

    @Test
    fun daylightSavingChangesLocalHoursWithoutMovingTheUtcSlot() {
        val madridSpring = DexSchedule.window("Night", LocalDate.of(2026, 3, 28), madrid)
        assertEquals(LocalTime.MIDNIGHT, madridSpring.start.toLocalTime())
        assertEquals(LocalTime.of(7, 0), madridSpring.end.toLocalTime())
        assertEquals(ZoneOffset.ofHours(1), madridSpring.start.offset)
        assertEquals(ZoneOffset.ofHours(2), madridSpring.end.offset)
        val madridWinterNight = DexSchedule.window("Night", LocalDate.of(2026, 1, 15), madrid)
        assertEquals(LocalTime.MIDNIGHT, madridWinterNight.start.toLocalTime())
        assertEquals(LocalTime.of(6, 0), madridWinterNight.end.toLocalTime())
        assertEquals(madridWinterNight.start.offset, madridWinterNight.end.offset)

        val chileSpring = DexSchedule.window("Night", LocalDate.of(2026, 9, 5), santiago)
        assertEquals(LocalTime.of(19, 0), chileSpring.start.toLocalTime())
        assertEquals(LocalTime.of(2, 0), chileSpring.end.toLocalTime())
        assertEquals(ZoneOffset.ofHours(-4), chileSpring.start.offset)
        assertEquals(ZoneOffset.ofHours(-3), chileSpring.end.offset)
        val chileFall = DexSchedule.window("Night", LocalDate.of(2026, 4, 4), santiago)
        assertEquals(LocalTime.of(20, 0), chileFall.start.toLocalTime())
        assertEquals(LocalTime.of(1, 0), chileFall.end.toLocalTime())
        assertEquals(ZoneOffset.ofHours(-3), chileFall.start.offset)
        assertEquals(ZoneOffset.ofHours(-4), chileFall.end.offset)

        val laSpring = DexSchedule.window("Dawn", LocalDate.of(2026, 3, 8), losAngeles)
        assertEquals(LocalTime.of(21, 0), laSpring.start.toLocalTime())
        assertEquals(LocalTime.of(4, 0), laSpring.end.toLocalTime())
        assertNotEquals(laSpring.start.offset, laSpring.end.offset)
        val jump = Instant.parse("2026-03-08T10:00:00Z")
        assertEquals("Dawn", DexSchedule.slotAt(jump.minusSeconds(30)).key)
        assertEquals("Dawn", DexSchedule.slotAt(jump.plusSeconds(30)).key)

        val julyNoon = Instant.parse("2026-07-15T12:00:00Z")
        val novemberNoon = Instant.parse("2026-11-06T12:00:00Z")
        assertEquals("Day", DexSchedule.slotAt(julyNoon).key)
        assertEquals("Day", DexSchedule.slotAt(novemberNoon).key)
        assertNotEquals(
            DexSchedule.window("Day", julyNoon.atZone(ZoneOffset.UTC).toLocalDate(), madrid).start.toLocalTime(),
            DexSchedule.window("Day", novemberNoon.atZone(ZoneOffset.UTC).toLocalDate(), madrid).start.toLocalTime(),
        )
        assertNotEquals(
            DexSchedule.window("Day", julyNoon.atZone(ZoneOffset.UTC).toLocalDate(), losAngeles).start.toLocalTime(),
            DexSchedule.window("Day", novemberNoon.atZone(ZoneOffset.UTC).toLocalDate(), losAngeles).start.toLocalTime(),
        )
        assertNotEquals(
            DexSchedule.window("Day", julyNoon.atZone(ZoneOffset.UTC).toLocalDate(), santiago).start.toLocalTime(),
            DexSchedule.window("Day", novemberNoon.atZone(ZoneOffset.UTC).toLocalDate(), santiago).start.toLocalTime(),
        )
        assertEquals(
            DexSchedule.window("Day", julyNoon.atZone(ZoneOffset.UTC).toLocalDate(), mexico).start.toLocalTime(),
            DexSchedule.window("Day", novemberNoon.atZone(ZoneOffset.UTC).toLocalDate(), mexico).start.toLocalTime(),
        )
        assertEquals(
            DexSchedule.window("Day", julyNoon.atZone(ZoneOffset.UTC).toLocalDate(), buenosAires).start.toLocalTime(),
            DexSchedule.window("Day", novemberNoon.atZone(ZoneOffset.UTC).toLocalDate(), buenosAires).start.toLocalTime(),
        )
    }

    @Test
    fun nowUsesTheTimeSlotAndIgnoresWeather() {
        val noon = Instant.parse("2026-11-06T12:00:00Z")
        assertTrue(DexSchedule.availableNow(listOf("Day"), noon))
        assertTrue(DexSchedule.availableNow(listOf("Rainy", "Day"), noon))
        assertFalse(DexSchedule.availableNow(listOf("Dusk", "Night"), noon))
        assertFalse(DexSchedule.availableNow(emptyList(), noon))
        assertTrue(DexSchedule.availableNow(listOf("Night", "Dawn", "Day", "Dusk"), noon))
        assertEquals("Tarde", DexSchedule.scheduleEs("Dusk"))
        assertEquals("🌤️ Tarde", DexSchedule.scheduleLabel("Dusk"))
    }

    @Test
    fun windowsAroundUseTheZoneOffsetInForce() {
        val winter = Instant.parse("2026-11-06T18:00:00Z")
        val santiagoHours = DexSchedule.windowsAround(santiago, winter).associate { it.slot.key to DexSchedule.hoursLabel(it) }
        assertEquals("02:00–08:00", santiagoHours.getValue("Dawn"))
        assertEquals("08:00–14:00", santiagoHours.getValue("Day"))
        assertEquals("14:00–20:00", santiagoHours.getValue("Dusk"))
        assertEquals("20:00–02:00", santiagoHours.getValue("Night"))
        assertEquals("Dusk", DexSchedule.currentWindow(santiago, winter).slot.key)
        assertEquals("hasta las 20:00", DexSchedule.untilLabel(DexSchedule.currentWindow(santiago, winter), winter.atZone(santiago)))

        val summer = Instant.parse("2026-07-15T15:00:00Z")
        val summerHours = DexSchedule.windowsAround(santiago, summer).associate { it.slot.key to DexSchedule.hoursLabel(it) }
        assertEquals("01:00–07:00", summerHours.getValue("Dawn"))
        assertEquals("07:00–13:00", summerHours.getValue("Day"))
        assertEquals("13:00–19:00", summerHours.getValue("Dusk"))
        assertEquals("19:00–01:00", summerHours.getValue("Night"))
    }

    private fun assertWindow(zone: ZoneId, utcDate: LocalDate, key: String, start: String, end: String) {
        val window = DexSchedule.window(key, utcDate, zone)
        assertEquals(ZonedDateTime.parse(start).toInstant(), window.start.toInstant())
        assertEquals(ZonedDateTime.parse(end).toInstant(), window.end.toInstant())
        assertEquals(ZonedDateTime.parse(start).toLocalDateTime(), window.start.toLocalDateTime())
        assertEquals(ZonedDateTime.parse(end).toLocalDateTime(), window.end.toLocalDateTime())
    }
}
