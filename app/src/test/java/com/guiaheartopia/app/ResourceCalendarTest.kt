package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureMark
import com.guiaheartopia.app.domain.DayCertainty
import com.guiaheartopia.app.domain.Habitat
import com.guiaheartopia.app.domain.ResourceCalendar
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceCalendarTest {
    @Test
    fun novemberSixthAgreesOnPlotEight() {
        val day = ResourceCalendar.on(LocalDate.of(2026, 11, 6))
        assertEquals("8", day.liuliu)
        assertEquals("8", day.flurita)
        assertEquals(DayCertainty.CONSENSO, day.certainty)
        assertEquals(2, day.sources)
    }

    @Test
    fun decemberIsEstimatedFromTheFiftyDayLoop() {
        val day = ResourceCalendar.on(LocalDate.of(2026, 12, 1))
        assertEquals("7", day.liuliu)
        assertEquals("6", day.flurita)
        assertEquals(DayCertainty.ESTIMADO, day.certainty)
        assertTrue(ResourceCalendar.decemberFollowsTheNovemberLoop())
        val outside = ResourceCalendar.on(LocalDate.of(2026, 1, 15))
        assertEquals(DayCertainty.SIN_DATOS, outside.certainty)
        assertNull(outside.liuliu)
    }

    @Test
    fun habitatTintFollowsThePlaceBeforeTheLevel() {
        assertEquals(Habitat.MAR, CreatureMark.habitat(listOf("Mar antiguo"), 9))
        assertEquals(Habitat.BOSQUE, CreatureMark.habitat(listOf("Bosque de roble espiritual"), 1))
        assertEquals(Habitat.NIVEL, CreatureMark.habitat(emptyList(), 2))
    }
}
