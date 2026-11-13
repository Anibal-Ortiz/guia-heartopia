package com.guiaheartopia.app

import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountriesZoneTest {
    @Test
    fun americanCountriesExposeRealIanaZones() {
        val ids = Countries.all.map { it.id }
        listOf("CR", "PA", "GT", "HN", "SV", "NI", "CU", "DO", "PR", "BO", "PY", "UY", "VE", "EC", "CO", "PE", "AR", "MX", "BR", "US", "CA", "CL")
            .forEach { assertTrue(it, ids.contains(it)) }
        assertEquals("America/Costa_Rica", Countries.byId("CR").zones.single().zoneId)
        assertEquals("Pacific/Galapagos", Countries.byId("EC").zones.first { it.label == "Galápagos" }.zoneId)
        assertEquals("America/Punta_Arenas", Countries.byId("CL").zones.first { it.label == "Magallanes" }.zoneId)
        assertTrue(Countries.byId("AR").zones.any { it.zoneId == "America/Argentina/Ushuaia" })
        assertTrue(Countries.byId("AR").zones.map { it.label }.contains("Córdoba"))
        assertTrue(Countries.byId("MX").zones.size > 20)
        assertTrue(Countries.byId("US").zones.any { it.zoneId == "America/Phoenix" })
    }

    @Test
    fun offsetsFollowRealDaylightSaving() {
        assertEquals(-5, hours("America/Havana", 2026, 1, 15))
        assertEquals(-4, hours("America/Havana", 2026, 7, 15))
        assertEquals(-3, hours("America/Santiago", 2026, 1, 15))
        assertEquals(-4, hours("America/Santiago", 2026, 7, 15))
        assertEquals(-3, hours("America/Punta_Arenas", 2026, 1, 15))
        assertEquals(-3, hours("America/Punta_Arenas", 2026, 7, 15))
        assertEquals(-5, hours("Pacific/Easter", 2026, 1, 15))
        assertEquals(-6, hours("Pacific/Easter", 2026, 7, 15))
        assertEquals(-7, hours("America/Phoenix", 2026, 7, 15))
        assertEquals(-6, hours("America/Denver", 2026, 7, 15))
        assertEquals(-5, hours("America/Guayaquil", 2026, 6, 15))
        assertEquals(-6, hours("Pacific/Galapagos", 2026, 6, 15))
        assertEquals(-8, hours("America/Tijuana", 2026, 1, 15))
        assertEquals(-7, hours("America/Tijuana", 2026, 7, 15))
        assertEquals(-7, hours("America/Hermosillo", 2026, 7, 15))
        assertEquals(-6, hours("America/Mexico_City", 2026, 7, 15))
        assertEquals(-3, hours("America/Argentina/Buenos_Aires", 2026, 7, 15))
        val half = ZoneId.of("America/St_Johns").rules.getOffset(LocalDateTime.of(2026, 1, 15, 12, 0)).totalSeconds
        assertEquals(-3 * 3600 - 30 * 60, half)
    }

    @Test
    fun calendarUnlockEndsAtTheNextServerDawn() {
        val zone = ZoneId.of("America/Santiago")
        val now = ZonedDateTime.of(2026, 11, 12, 15, 0, 0, 0, zone).toInstant()
        val next = SkyWeatherGameDay.nextServerDawn(now, zone)
        assertTrue(next.isAfter(now))
        val local = next.atZone(zone)
        assertEquals(8, local.hour)
        assertEquals(13, local.dayOfMonth)
    }

    private fun hours(zone: String, year: Int, month: Int, day: Int): Int =
        ZoneId.of(zone).rules.getOffset(LocalDateTime.of(year, month, day, 12, 0)).totalSeconds / 3600
}
