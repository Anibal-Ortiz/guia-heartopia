package com.guiaheartopia.app

import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.ResourceCalendar
import com.guiaheartopia.app.share.CommunityCalendarGrid
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityCalendarGridTest {
    @Test
    fun shortSpotMapsBosqueRuinasAndNumbers() {
        assertNull(CommunityCalendarGrid.shortSpot(null))
        assertEquals("B", CommunityCalendarGrid.shortSpot("Bosque"))
        assertEquals("R", CommunityCalendarGrid.shortSpot("Ruinas"))
        assertEquals("8", CommunityCalendarGrid.shortSpot("8"))
    }

    @Test
    fun november2026GridHasFortyTwoCellsAndSundayLead() {
        val month = YearMonth.of(2026, 11)
        val cells = CommunityCalendarGrid.cells(
            month = month,
            today = LocalDate.of(2026, 11, 28),
            selected = LocalDate.of(2026, 11, 1),
        )
        assertEquals(42, cells.size)
        // 1 de noviembre de 2026 es domingo -> 6 celdas de relleno antes.
        assertTrue(cells.take(6).all { it.date == null })
        val first = cells[6]
        assertEquals(1, first.dayNumber)
        assertEquals(LocalDate.of(2026, 11, 1), first.date)
        assertTrue(first.isSelected)
        assertFalse(first.isToday)
        assertTrue(first.hasData)
        // Fuente única sigue siendo ResourceCalendar.
        val raw = ResourceCalendar.on(LocalDate.of(2026, 11, 1))
        assertEquals(raw.certainty, DayCertainty.PUBLICADO)
    }

    @Test
    fun outsideNovemberHasNoDataButKeepsCells() {
        val month = YearMonth.of(2026, 12)
        val cells = CommunityCalendarGrid.cells(month, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 1))
        assertTrue(cells.filter { it.date != null }.all { !it.hasData })
        assertTrue(cells.any { it.date != null && it.dayNumber == 1 })
    }
}
