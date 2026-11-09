package com.guiaheartopia.app

import com.guiaheartopia.app.domain.GameTimePresentation
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTimeCardTest {
   private val santiago = ZoneId.of("America/Santiago")

    @Test
    fun chileNov9At1831ShowsDayUntil2000() {
        val instant = ZonedDateTime.of(2026, 11, 9, 18, 31, 0, 0, santiago).toInstant()
        val model = GameTimePresentation.cardModel(santiago.id, instant)

        assertEquals("Día", model.currentTitle)
        assertFalse(model.currentTitle.contains("☀"))
        assertEquals("hasta las 20:00", model.untilLabel)
        assertEquals("18:31", model.localClock)

        val byKey = model.rows.associateBy { it.slotKey }
        assertEquals("Amanecer", byKey.getValue("Dawn").title)
        assertFalse(byKey.getValue("Dawn").title.contains("🌅"))
        assertEquals("08:00–14:00", byKey.getValue("Dawn").hours)
        assertEquals("Día", byKey.getValue("Day").title)
        assertEquals("14:00–20:00", byKey.getValue("Day").hours)
        assertTrue(byKey.getValue("Day").isCurrent)
        assertEquals("Tarde", byKey.getValue("Dusk").title)
        assertEquals("20:00–02:00", byKey.getValue("Dusk").hours)
        assertEquals("Noche", byKey.getValue("Night").title)
        assertFalse(byKey.values.any { it.title.contains("☀") || it.title.contains("🌆") })
        assertEquals("02:00–08:00", byKey.getValue("Night").hours)
    }

    @Test
    fun matchesDexScheduleSlotAtSameInstant() {
        val instant = Instant.parse("2026-11-09T21:31:00Z")
        val model = GameTimePresentation.cardModel("America/Santiago", instant)
        assertEquals("Day", model.rows.first { it.isCurrent }.slotKey)
    }
}
