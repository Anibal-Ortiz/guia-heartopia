package com.guiaheartopia.app

import com.guiaheartopia.app.data.SampleWeather
import com.guiaheartopia.app.domain.ResourceKeys
import com.guiaheartopia.app.domain.WeatherCalendar
import com.guiaheartopia.app.domain.WeatherTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherTimeTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val newYork = ZoneId.of("America/New_York")
    private val tokyo = ZoneId.of("Asia/Tokyo")

    @Test
    fun tokyoHasNoDstInTheCurrentRules() {
        assertNull(tokyo.rules.nextTransition(Instant.parse("2020-01-01T00:00:00Z")))
        val january = Instant.parse("2026-01-15T12:00:00Z")
        val july = Instant.parse("2026-07-15T12:00:00Z")
        assertEquals(tokyo.rules.getOffset(january), tokyo.rules.getOffset(july))
    }

    @Test
    fun newYorkChangesOffsetBetweenWinterAndSummer() {
        val winter = newYork.rules.getOffset(Instant.parse("2026-01-15T18:00:00Z"))
        val summer = newYork.rules.getOffset(Instant.parse("2026-07-15T18:00:00Z"))
        assertNotEquals(winter, summer)
    }

    @Test
    fun chileAndNewYorkDoNotShareTheJanuaryOffset() {
        val moment = Instant.parse("2026-01-15T18:00:00Z")
        assertNotEquals(santiago.rules.getOffset(moment), newYork.rules.getOffset(moment))
    }

    @Test
    fun santiagoTransitionFollowsZoneRules() {
        val transition = santiago.rules.nextTransition(Instant.parse("2024-01-01T00:00:00Z"))
            ?: santiago.rules.previousTransition(Instant.parse("2026-11-06T00:00:00Z"))
        assertNotNull(transition)
        val before = transition!!.instant.minusSeconds(30)
        val after = transition.instant.plusSeconds(30)
        assertNotEquals(santiago.rules.getOffset(before), santiago.rules.getOffset(after))
        for (mark in listOf(before, after)) {
            val originLocal = LocalDateTime.ofInstant(mark, tokyo)
            val viewed = WeatherTime.convert(originLocal, tokyo, santiago)
            assertEquals(mark, viewed.toInstant())
            assertEquals(santiago, viewed.zone)
            assertEquals(LocalDateTime.ofInstant(mark, santiago).toLocalDate(), viewed.toLocalDate())
        }
    }

    @Test
    fun lateEveningMovesToTheNextLocalDay() {
        val origin = LocalDateTime.of(2026, 1, 15, 23, 30)
        val viewed = WeatherTime.convert(origin, newYork, tokyo)
        val expected = origin.atZone(newYork).withZoneSameInstant(tokyo)
        assertEquals(expected.toInstant(), viewed.toInstant())
        assertTrue(viewed.toLocalDate().isAfter(origin.toLocalDate()))
    }

    @Test
    fun earlyMorningMovesToThePreviousLocalDay() {
        val origin = LocalDateTime.of(2026, 1, 15, 0, 30)
        val viewed = WeatherTime.convert(origin, tokyo, newYork)
        assertTrue(viewed.toLocalDate().isBefore(origin.toLocalDate()))
        assertEquals(origin.atZone(tokyo).withZoneSameInstant(newYork).toInstant(), viewed.toInstant())
    }

    @Test
    fun newYorkSpringForwardGapUsesZoneRules() {
        val gap = LocalDateTime.of(2026, 3, 8, 2, 30)
        val viewed = WeatherTime.convert(gap, newYork, santiago)
        val resolved = gap.atZone(newYork).withZoneSameInstant(santiago)
        assertEquals(resolved.toInstant(), viewed.toInstant())
        assertEquals(resolved.toLocalDate(), viewed.toLocalDate())
    }

    @Test
    fun newYorkFallBackOverlapUsesZoneRules() {
        val repeated = LocalDateTime.of(2026, 11, 1, 1, 30)
        val viewed = WeatherTime.convert(repeated, newYork, santiago)
        val resolved = repeated.atZone(newYork).withZoneSameInstant(santiago)
        assertEquals(resolved.toInstant(), viewed.toInstant())
        assertEquals(resolved.toLocalDate(), viewed.toLocalDate())
    }

    @Test
    fun bogotaKeepsAFixedOffsetThroughTheYear() {
        val bogota = ZoneId.of("America/Bogota")
        assertNull(bogota.rules.nextTransition(Instant.parse("2020-01-01T00:00:00Z")))
        val local = LocalDateTime.of(2026, 6, 1, 12, 0)
        val viewed = WeatherTime.convert(local, bogota, ZoneOffset.UTC)
        assertEquals(local.atZone(bogota).toInstant(), viewed.toInstant())
    }

    @Test
    fun sampleSlotAt0230UtcLandsOnThePreviousChileanDate() {
        val placed = WeatherCalendar.place(
            SampleWeather.slots,
            SampleWeather.origins,
            santiago,
            SampleWeather.origins.first().id,
        )
        val shifted = placed.first { it.slot.id == "s-cambio-dia" }
        val expected = shifted.slot.originLocal.atZone(ZoneId.of("UTC")).withZoneSameInstant(santiago)
        assertEquals(expected.toLocalDate(), shifted.localDate)
        assertTrue(expected.toLocalDate().isBefore(shifted.slot.originLocal.toLocalDate()))
        assertTrue(shifted.dayShifted)
        assertTrue(shifted.slot.sightings.any { it.key == ResourceKeys.FLURITA })
        assertTrue(shifted.slot.sightings.any { it.key == ResourceKeys.LIULIU })
        assertTrue(shifted.slot.sightings.all { it.location == null && !it.verified })
    }
}
