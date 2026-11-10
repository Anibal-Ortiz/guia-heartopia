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
    fun nov12ChileStartsAtDawnAndSplitsInsideEachFrame() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        val detail = detailTexts(table, day, santiago)
        val grouped = groupedTexts(table, day, santiago)
        assertEquals(
            listOf(
                "Amanecer 08:00–14:00",
                "Despejado 08:00–12:00",
                "Lluvia 12:00–14:00",
                "Día 14:00–20:00",
                "Lluvia 14:00–18:00",
                "Arcoíris 18:00–20:00",
                "Tarde 20:00–02:00 del día siguiente",
                "Arcoíris 20:00–00:00 del día siguiente",
                "Despejado 00:00–02:00 del día siguiente",
                "Noche 02:00–08:00 del día siguiente",
                "Despejado 02:00–08:00 del día siguiente",
            ),
            grouped,
        )
        assertEquals(
            listOf(
                "Lluvia 12:00–14:00",
                "Lluvia 14:00–18:00",
                "Arcoíris 18:00–20:00",
                "Arcoíris 20:00–00:00 del día siguiente",
            ),
            detail,
        )
        val frames = SkyWeatherGameDay.barFrameSegmentsForGameDay(table, day, santiago)
        assertEquals(4, frames.size)
        assertEquals(listOf(2, 2, 2, 1), frames.map { it.size })
    }

    @Test
    fun nov10ChileIsClearAcrossTheFourFrames() {
        val table = SkyCatalog.fromJson(catalogText())
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
        val table = SkyCatalog.fromJson(catalogText())
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
        val table = SkyCatalog.fromJson(catalogText())
        val july = groupedTexts(table, LocalDate.of(2026, 7, 15), santiago)
        assertEquals("Amanecer 07:00–13:00", july.first())
        assertEquals(LocalTime.of(7, 0), SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 7, 15), santiago).first().start.toLocalTime())
        assertEquals(LocalTime.of(8, 0), SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 11, 12), santiago).first().start.toLocalTime())

        val beforeChange = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 4, 4), santiago)
        val afterChange = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 4, 5), santiago)
        assertEquals(LocalTime.of(8, 0), beforeChange.first().start.toLocalTime())
        assertEquals(LocalTime.of(7, 0), afterChange.first().start.toLocalTime())
        assertEquals("Tarde 20:00–01:00 del día siguiente", frameTitle(table, LocalDate.of(2026, 4, 4), "Tarde"))
        assertEquals("Amanecer 08:00–14:00", groupedTexts(table, LocalDate.of(2026, 9, 6), santiago).first())
        assertEquals("Amanecer 07:00–13:00", groupedTexts(table, LocalDate.of(2026, 9, 5), santiago).first())
    }

    @Test
    fun climateClockStaysLocalWhileFramesConvertFromUtc() {
        val table = SkyCatalog.fromJson(catalogText())
        val detail = detailTexts(table, LocalDate.of(2026, 11, 12), santiago)
        assertTrue(detail.contains("Lluvia 12:00–14:00"))
        val madridDawn = SkyWeatherGameDay.windowsForLocalDawn(LocalDate.of(2026, 11, 12), madrid).first()
        assertEquals(LocalTime.of(12, 0), madridDawn.start.toLocalTime())
        assertEquals("Amanecer 12:00–18:00", groupedTexts(table, LocalDate.of(2026, 11, 12), madrid).first())
    }

    @Test
    fun otherYearsStayEmpty() {
        val table = SkyCatalog.fromJson(catalogText())
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2025, 11, 12), santiago).isEmpty())
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, LocalDate.of(2027, 11, 12), santiago).isEmpty())
        assertTrue(SkyWeatherGameDay.compactLinesForGameDay(table, LocalDate.of(2025, 11, 12), santiago).isEmpty())
        val raw = catalogText()
        assertTrue(!raw.contains("por_confirmar"))
        assertTrue(!raw.contains("matchingSlots"))
        assertEquals(365, table.days.size)
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

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
