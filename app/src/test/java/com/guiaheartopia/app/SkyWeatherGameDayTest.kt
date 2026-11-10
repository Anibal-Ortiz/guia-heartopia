package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyKind
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

    /**
     * La lógica de franjas se comprueba con un día sintético.
     * El archivo publicado no trae el 12 de noviembre: heartodex no lista el clima por fecha.
     */
    @Test
    fun syntheticDayKeepsChileGameFramesAndNextDayDusk() {
        val table = SkyCatalog.fromJson(syntheticDay("2026-11-12"))
        val day = LocalDate.of(2026, 11, 12)
        val spans = SkyWeatherGameDay.spansForGameDay(table, day, santiago)
        assertEquals(
            listOf(SkyKind.SOLEADO, SkyKind.LLUVIA, SkyKind.ARCOIRIS, SkyKind.SOLEADO),
            spans.map { it.kind },
        )
        val lines = spans.map { SkyWeatherGameDay.formatSpan(it, day) }
        assertEquals("Sol 02:00–08:00", lines[0])
        assertEquals("Lluvia 08:00–14:00", lines[1])
        assertEquals("Arcoíris 14:00–20:00", lines[2])
        assertEquals("Sol 20:00–02:00 del día siguiente", lines[3])
        assertTrue(spans.last().end.toLocalDate().isAfter(day))
    }

    @Test
    fun otherZoneKeepsKindsAndShiftsTheClock() {
        val table = SkyCatalog.fromJson(syntheticDay("2026-11-12"))
        val day = LocalDate.of(2026, 11, 12)
        val cl = SkyWeatherGameDay.spansForGameDay(table, day, santiago)
        val eu = SkyWeatherGameDay.spansForGameDay(table, day, madrid)
        assertEquals(cl.map { it.kind }, eu.map { it.kind })
        assertEquals(LocalTime.of(2, 0), cl[0].start.toLocalTime())
        assertEquals(LocalTime.of(6, 0), eu[0].start.toLocalTime())
    }

    @Test
    fun shippedCalendarHasNoNovember12BecauseHeartodexDoesNotPublishIt() {
        val table = SkyCatalog.fromJson(catalogText())
        val day = LocalDate.of(2026, 11, 12)
        assertTrue(table.on(day) == null)
        assertTrue(SkyWeatherGameDay.spansForGameDay(table, day, santiago).isEmpty())
        val raw = catalogText()
        assertTrue(!raw.contains("por_confirmar"))
        assertTrue(!raw.contains("acuerdo"))
        assertTrue(!raw.contains("matchingSlots"))
    }

    private fun syntheticDay(date: String): String = """
        {
          "days": [
            {
              "date": "$date",
              "slots": [
                {"start":"00:00","end":"06:00","kind":"despejado","status":"acuerdo"},
                {"start":"06:00","end":"12:00","kind":"soleado","status":"acuerdo"},
                {"start":"12:00","end":"18:00","kind":"lluvia","status":"acuerdo"},
                {"start":"18:00","end":"24:00","kind":"arcoiris_luna","status":"acuerdo"}
              ]
            },
            {
              "date": "2026-11-13",
              "slots": [
                {"start":"00:00","end":"06:00","kind":"despejado","status":"acuerdo"},
                {"start":"06:00","end":"12:00","kind":"nublado","status":"acuerdo"},
                {"start":"12:00","end":"18:00","kind":"lluvia","status":"acuerdo"},
                {"start":"18:00","end":"24:00","kind":"lluvia","status":"acuerdo"}
              ]
            }
          ]
        }
    """.trimIndent()

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
