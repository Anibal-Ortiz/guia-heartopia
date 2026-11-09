package com.guiaheartopia.app

import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SkyWeatherLabelTest {
    @Test
    fun plainTitleOmitsEmojiButKeepsLabel() {
        val slot = SkySlot(LocalTime.NOON, LocalTime.MIDNIGHT, SkyKind.LLUVIA, SkyStatus.ACUERDO)
        assertEquals("🌧️ Lluvia", slot.title())
        assertEquals("Lluvia", slot.plainTitle())
        assertFalse(slot.plainTitle().contains("🌧"))
    }

    @Test
    fun summarySpanOmitsEmoji() {
        val span = com.guiaheartopia.app.domain.SkySpan(
            SkyKind.SOLEADO,
            SkyStatus.ACUERDO,
            null,
            null,
            java.time.ZonedDateTime.parse("2026-11-06T08:00-03:00"),
            java.time.ZonedDateTime.parse("2026-11-06T14:00-03:00"),
        )
        assertEquals("Sol 08:00–14:00", span.text())
        assertFalse(span.text().contains("☀"))
    }
}
