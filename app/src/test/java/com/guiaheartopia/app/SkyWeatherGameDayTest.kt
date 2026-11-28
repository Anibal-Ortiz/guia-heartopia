package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyWeatherGameDay
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherGameDayTest {
    private val santiago = ZoneId.of("America/Santiago")
    private val madrid = ZoneId.of("Europe/Madrid")

    @Test
    fun nov12ChileStartsAtNightAndSplitsInsideEachFrame() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        val day = LocalDate.of(2026, 11, 12)
        val detail = detailTexts(table, day, santiago)
        val grouped = groupedTexts(table, day, santiago)
        assertEquals(
            listOf(
                "Noche 02:00–08:00 del día siguiente",
                "Despejado 02:00–08:00 del día siguiente",
                "Amanecer 08:00–14:00",
                "Despejado 08:00–14:00",
                "Día 14:00–20:00",
                "Lluvia 14:00–20:00",
                "Tarde 20:00–02:00 del día siguiente",
                "Arcoíris 20:00–02:00 del día siguiente",
            ),
            grouped,
        )
        assertEquals(
            listOf(
                "Lluvia 14:00–20:00",
                "Arcoíris 20:00–02:00 del día siguiente",
            ),
            detail,
        )
        val frames = SkyWeatherGameDay.barFrameSegmentsForGameDay(table, day, santiago)
        assertEquals(4, frames.size)
        assertEquals(
            listOf("DESPEJADO", "DESPEJADO", "LLUVIA", "ARCOIRIS"),
            frames.map { it.single().kind.name },
        )
    }

    @Test
    fun nov10ChileIsClearAcrossTheFourFrames() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        assertEquals(
            listOf("Despejado todo el día"),
            detailTexts(table, LocalDate.of(2026, 11, 10), santiago),
        )
        val frames = SkyWeatherGameDay.barFrameSegmentsForGameDay(table, LocalDate.of(2026, 11, 10), santiago)
        assertEquals(4, frames.size)
        assertTrue(frames.all { it.size == 1 && it.first().kind.name == "DESPEJADO" })
    }

    @Test
    fun beforeDawnChileTodayIsTheServerDayThatStartedYesterday() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        val marks = listOf(
            ZonedDateTime.of(2026, 11, 12, 0, 30, 0, 0, santiago),
            ZonedDateTime.of(2026, 11, 12, 3, 0, 0, 0, santiago),
            ZonedDateTime.of(2026, 11, 12, 7, 59, 0, 0, santiago),
        )
        marks.forEach { mark ->
            assertEquals(LocalDate.of(2026, 11, 11), SkyWeatherGameDay.serverDawnLocalDate(mark.toInstant(), santiago))
        }
        val atDawn = ZonedDateTime.of(2026, 11, 12, 8, 0, 0, 0, santiago)
        assertEquals(LocalDate.of(2026, 11, 12), SkyWeatherGameDay.serverDawnLocalDate(atDawn.toInstant(), santiago))
        val detail = detailTexts(table, LocalDate.of(2026, 11, 11), santiago)
        assertEquals("Despejado todo el día", detail.single())
    }

    @Test
    fun chileAnnualOffsetMovesTheServerDayWithoutHardcodingUtcMinus3() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        val july = groupedTexts(table, LocalDate.of(2026, 7, 15), santiago)
        assertEquals("Noche 01:00–07:00 del día siguiente", july.first())
        assertEquals(
            LocalTime.of(7, 0),
            SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 7, 15), santiago).first { it.slot.key == "Dawn" }.start.toLocalTime(),
        )
        assertEquals(
            LocalTime.of(8, 0),
            SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 11, 12), santiago).first { it.slot.key == "Dawn" }.start.toLocalTime(),
        )

        val beforeChange = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 4, 4), santiago)
        val afterChange = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 4, 5), santiago)
        assertEquals(LocalTime.of(8, 0), beforeChange.first { it.slot.key == "Dawn" }.start.toLocalTime())
        assertEquals(LocalTime.of(7, 0), afterChange.first { it.slot.key == "Dawn" }.start.toLocalTime())
        assertEquals("Tarde 20:00–01:00 del día siguiente", frameTitle(table, LocalDate.of(2026, 4, 4), "Tarde"))
        assertEquals("Noche 02:00–08:00 del día siguiente", groupedTexts(table, LocalDate.of(2026, 9, 6), santiago).first())
        assertEquals("Noche 02:00–08:00 del día siguiente", groupedTexts(table, LocalDate.of(2026, 9, 5), santiago).first())
    }

    @Test
    fun climateClockStaysLocalWhileFramesConvertFromUtc() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        val detail = detailTexts(table, LocalDate.of(2026, 11, 12), santiago)
        assertTrue(detail.contains("Lluvia 14:00–20:00"))
        val madridDawn = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 11, 12), madrid).first { it.slot.key == "Dawn" }
        assertEquals(LocalTime.of(12, 0), madridDawn.start.toLocalTime())
        assertEquals("Noche 06:00–12:00 del día siguiente", groupedTexts(table, LocalDate.of(2026, 11, 12), madrid).first())
    }

    @Test
    fun annualCycleRepeatsEveryYear() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2025, 11, 12), santiago).isNotEmpty())
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2027, 11, 12), santiago).isNotEmpty())
        assertTrue(SkyWeatherGameDay.compactLinesForGameDay(table, LocalDate.of(2027, 11, 12), santiago).isNotEmpty())
        val raw = catalogText()
        assertTrue(!raw.contains("por_confirmar"))
        assertTrue(!raw.contains("matchingSlots"))
        assertEquals(366, table.cycle?.days?.size)
    }

    private fun detailTexts(
        table: com.guiaheartopia.app.data.SkyTable,
        day: LocalDate,
        zone: ZoneId,
    ): List<String> = SkyWeatherGameDay.compactLinesForGameDay(table, day, zone).map { it.text }

    private fun groupedTexts(
        table: com.guiaheartopia.app.data.SkyTable,
        day: LocalDate,
        zone: ZoneId,
    ): List<String> = SkyWeatherGameDay.linesForGameDay(table, day, zone).map { it.text }

    private fun frameTitle(
        table: com.guiaheartopia.app.data.SkyTable,
        day: LocalDate,
        label: String,
    ): String = groupedTexts(table, day, santiago).first { it.startsWith(label) }

    private fun catalogText(): String =
        File("src/test/resources/calendar/clima.json").readText()
}
