package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherGameDayTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val madrid = ZoneId.of("Europe/Madrid")

    @Test
    fun nov12ChileShowsHourlyChangesInsideTheGameDay() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val lines = SkyWeatherGameDay.spansForGameDay(table, day, santiago)
            .map { SkyWeatherGameDay.formatSpan(it, day) }
        assertEquals(
            listOf(
                "Despejado 02:00–12:00",
                "Lluvia 12:00–18:00",
                "Arcoíris 18:00–00:00 del día siguiente",
                "Despejado 00:00–02:00 del día siguiente",
            ),
            lines,
        )
        assertTrue(lines.none { it.contains("por confirmar") || it.contains("confirmar") })
    }

    @Test
    fun duskUsesTheNextLocalMorningWhenTheZoneDemandsIt() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val madridLines = SkyWeatherGameDay.spansForGameDay(table, day, madrid)
            .map { SkyWeatherGameDay.formatSpan(it, day) }
        assertEquals("Despejado 06:00–12:00", madridLines.first())
        assertEquals("Despejado 00:00–06:00 del día siguiente", madridLines.last())
        assertTrue(madridLines.last().contains("del día siguiente"))
    }

    @Test
    fun chileWinterDoesNotKeepTheNovemberUtcMinus3Labels() {
        val table = SkyCatalog.fromJson(catalogText())
        val july = LocalDate.of(2026, 7, 15)
        val first = SkyWeatherGameDay.spansForGameDay(table, july, santiago).first()
        assertEquals(LocalTime.of(1, 0), first.start.toLocalTime())
    }

    @Test
    fun otherYearsStayEmpty() {
        val table = SkyCatalog.fromJson(catalogText())
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2025, 11, 12), santiago).isEmpty())
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2027, 11, 12), santiago).isEmpty())
        val raw = catalogText()
        assertTrue(!raw.contains("por_confirmar"))
        assertTrue(!raw.contains("matchingSlots"))
        assertEquals(365, table.days.size)
    }

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
