package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyTable
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.share.CommunityCardFactory
import com.guiaheartopia.app.share.CommunityImageKind
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityCardFactoryTest {
    private val country = Countries.byId("CL")
    private val chosenZone = Countries.zone(country.id, "America/Santiago", "")
    private val zone: ZoneId = ZoneId.of(chosenZone.zoneId)
    private val now: ZonedDateTime = ZonedDateTime.now(zone)

    @Test
    fun calendarCardIsCalendarAndIncludesFluoritaAndLiuliu() {
        val card = CommunityCardFactory.calendarCard(
            country = country,
            chosenZone = chosenZone,
            zone = zone,
            month = YearMonth.of(2026, 11),
            today = LocalDate.of(2026, 11, 28),
            selected = LocalDate.of(2026, 11, 1),
            now = now,
        )
        assertEquals(CommunityImageKind.CALENDAR, card.kind)
        assertEquals("Calendario Heartopia", card.title)
        assertEquals(YearMonth.of(2026, 11), card.month)
        assertEquals(42, card.cells.size)
        assertTrue(card.legend.any { it.contains("Fluorita") })
        assertTrue(card.legend.any { it.contains("Liuliu") })
        assertEquals("Datos de la comunidad, no oficiales", card.footer)
        // 1 nov 2026: Liuliu parcela 2, Fluorita parcela 8.
        assertEquals("Parcela 2", card.selectedDay.liuliuLabel)
        assertEquals("Parcela 8", card.selectedDay.fluritaLabel)
        assertTrue(card.selectedDay.hasData)
    }

    @Test
    fun weatherCardHasFourFramesAndSinDatosWhenEmpty() {
        val sky = SkyTable("", "", emptyMap())
        val card = CommunityCardFactory.weatherCard(
            country = country,
            chosenZone = chosenZone,
            zone = zone,
            sky = sky,
            date = LocalDate.of(2026, 11, 28),
            now = now,
        )
        assertEquals(CommunityImageKind.WEATHER, card.kind)
        assertEquals("Clima Heartopia", card.title)
        assertEquals(4, card.frames.size)
        assertEquals(listOf("Night", "Dawn", "Day", "Dusk"), card.frames.map { it.frameKey })
        assertTrue(card.frames.all { !it.hasData })
        assertTrue(card.frames.all { it.kind == SkyKind.SIN_DATOS })
        assertEquals("Sin datos", card.frames.first().label)
        assertEquals("Sin datos de clima para este día.", card.weatherSummary)
        assertTrue(card.legend.any { it.contains("Fluorita") })
        assertTrue(card.legend.any { it.contains("Liuliu") })
        // Fuorita/Liuliu del día seleccionado.
        assertFalse(card.selectedDay.liuliuLabel.isBlank())
        assertFalse(card.selectedDay.fluritaLabel.isBlank())
    }
}
