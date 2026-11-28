package com.guiaheartopia.app

import com.guiaheartopia.app.share.CommunityCardFactory
import com.guiaheartopia.app.share.CommunityImageFilename
import com.guiaheartopia.app.share.CommunityImageKind
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.ResourceCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CommunityImageFilenameTest {
    @Test
    fun sanitizeCollapsesAndTrims() {
        assertEquals("hola-mundo", CommunityImageFilename.sanitize("  Hola  Mundo!! "))
        assertEquals("a-b", CommunityImageFilename.sanitize("a---b"))
        assertEquals("imagen", CommunityImageFilename.sanitize("!!!"))
        assertFalse(CommunityImageFilename.sanitize("x").contains(" "))
    }

    @Test
    fun forCardNamesAreSafePng() {
        val country = Countries.byId("CL")
        val zone = Countries.zone(country.id, "America/Santiago", "")
        val zid = ZoneId.of(zone.zoneId)
        val cal = CommunityCardFactory.calendarCard(
            country, zone, zid, YearMonth.of(2026, 11),
            LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 1), ZonedDateTime.now(zid),
        )
        val weather = CommunityCardFactory.weatherCard(
            country, zone, zid, com.guiaheartopia.app.data.SkyTable("", "", emptyMap()),
            LocalDate.of(2026, 11, 28), ZonedDateTime.now(zid),
        )
        assertEquals("fluorita-liuliu-calendario-2026-11.png", CommunityImageFilename.forCard(cal))
        assertEquals("clima-fluorita-liuliu-2026-11-28.png", CommunityImageFilename.forCard(weather))
        assertEquals(CommunityImageKind.CALENDAR, cal.kind)
        // No referencia accidental a Flurita.
        assertFalse(CommunityImageFilename.forCard(cal).contains("flurita"))
    }
}
