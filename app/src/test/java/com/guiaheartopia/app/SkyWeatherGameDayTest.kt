package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherGameDayTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val madrid = ZoneId.of("Europe/Madrid")

    @Test
    fun nov12ChileShowsFourGameFramesWithAcceptanceTexts() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val spans = SkyWeatherGameDay.spansForGameDay(table, day, santiago)
        assertEquals(4, spans.size)
        assertEquals(
            listOf(SkyKind.SOLEADO, SkyKind.LLUVIA, SkyKind.ARCOIRIS, SkyKind.SOLEADO),
            spans.map { it.kind },
        )
        val lines = spans.map { SkyWeatherGameDay.formatSpan(it, day) }
        assertEquals("Sol 02:00–08:00", lines[0])
        assertEquals("Lluvia 08:00–14:00", lines[1])
        assertEquals("Arcoíris 14:00–20:00", lines[2])
        assertEquals("Sol 20:00–02:00 del día siguiente", lines[3])
        assertEquals(LocalTime.of(2, 0), spans[0].start.toLocalTime())
        assertEquals(LocalTime.of(2, 0), spans[3].end.toLocalTime())
    }

    @Test
    fun duskFrameCrossesToNextCalendarDay() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val dusk = SkyWeatherGameDay.spansForGameDay(table, day, santiago).last()
        assertEquals("Dusk", dusk.frameKey)
        assertTrue(dusk.end.toLocalDate().isAfter(day))
    }

    @Test
    fun otherZoneUsesSameCatalogOverlapButDifferentLocalClock() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val cl = SkyWeatherGameDay.spansForGameDay(table, day, santiago)
        val eu = SkyWeatherGameDay.spansForGameDay(table, day, madrid)
        assertEquals(cl.map { it.kind }, eu.map { it.kind })
        assertEquals(LocalTime.of(2, 0), cl[0].start.toLocalTime())
        assertEquals(LocalTime.of(6, 0), eu[0].start.toLocalTime())
    }

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
