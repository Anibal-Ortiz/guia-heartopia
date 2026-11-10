package com.guiaheartopia.app

import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import com.guiaheartopia.app.domain.climateDisplay
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun climateDisplayCollapsesToFiveKinds() {
        assertEquals(SkyKind.DESPEJADO, SkyKind.NUBLADO.climateDisplay())
        assertEquals(SkyKind.DESPEJADO, SkyKind.SOLEADO.climateDisplay())
        assertEquals(SkyKind.METEORO, SkyKind.METEORO.climateDisplay())
        assertEquals(SkyKind.ARCOIRIS, SkyKind.ARCOIRIS_LUNA.climateDisplay())
        assertEquals(SkyKind.LLUVIA, SkyKind.LLUVIA.climateDisplay())
        assertEquals(SkyKind.NIEVE, SkyKind.NIEVE.climateDisplay())
        val cloudy = SkySlot(LocalTime.NOON, LocalTime.MIDNIGHT, SkyKind.NUBLADO, SkyStatus.ACUERDO)
        assertEquals("Despejado", cloudy.plainTitle())
    }

    @Test
    fun barSegmentsMergeSameWeather() {
        val day = com.guiaheartopia.app.domain.SkyDay(
            java.time.LocalDate.of(2026, 11, 1),
            List(4) {
                SkySlot(
                    LocalTime.of(it * 6, 0),
                    LocalTime.of((it + 1) * 6 % 24, 0),
                    SkyKind.LLUVIA,
                    SkyStatus.ACUERDO,
                )
            },
        )
        val table = com.guiaheartopia.app.data.SkyTable("", "", mapOf(day.date to day))
        val segments = com.guiaheartopia.app.domain.SkyWeatherGameDay.barSegmentsForGameDay(
            table,
            day.date,
            java.time.ZoneId.of("America/Santiago"),
        )
        assertEquals(listOf(6 * 60, 6 * 60, 4 * 60), segments.map { it.weight })
        assertTrue(segments.all { it.kind == SkyKind.LLUVIA })
    }

    @Test
    fun summarySpanOmitsEmoji() {
        val span = com.guiaheartopia.app.domain.SkySpan(
            SkyKind.DESPEJADO,
            SkyStatus.ACUERDO,
            null,
            null,
            java.time.ZonedDateTime.parse("2026-11-06T08:00-03:00"),
            java.time.ZonedDateTime.parse("2026-11-06T14:00-03:00"),
        )
        assertEquals("Despejado 08:00–14:00", span.text())
        assertFalse(span.text().contains("☀"))
    }
}
