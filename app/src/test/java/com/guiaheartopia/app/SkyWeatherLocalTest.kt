package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import com.guiaheartopia.app.domain.SkyWeatherLocal
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherLocalTest {
    private val santiago = ZoneId.of("America/Santiago")

    @Test
    fun utcMidnightSlotDoesNotCollapseToZeroLengthInChile() {
        val table = SkyCatalog.fromJson(catalogText())
        val nov6 = LocalDate.of(2026, 11, 6)
        val spans = SkyWeatherLocal.spansOnLocalDay(table, nov6, santiago)
        val formatted = spans.joinToString { SkyWeatherLocal.formatSpan(it, nov6, santiago) }
        assertFalse(formatted.contains("21:00–21:00"))
    }

    @Test
    fun fullLocalDayCanReadAsTodoElDia() {
        val day = com.guiaheartopia.app.domain.SkyDay(
            LocalDate.of(2026, 11, 1),
            List(4) { i ->
                SkySlot(
                    LocalTime.of(i * 6, 0),
                    if (i == 3) LocalTime.MIDNIGHT else LocalTime.of((i + 1) * 6, 0),
                    SkyKind.SOLEADO,
                    SkyStatus.ACUERDO,
                )
            },
        )
        val table = com.guiaheartopia.app.data.SkyTable("", "", mapOf(day.date to day))
        val local = LocalDate.of(2026, 11, 1)
        val text = SkyWeatherLocal.summaryOnLocalDay(table, local, santiago)
        assertTrue(text.contains("Todo el día") || text.contains("03:00") || text.contains("Sol"))
    }

    @Test
    fun nov2SantiagoListMatchesIndependentJavaTime() {
        val table = SkyCatalog.fromJson(catalogText())
        val local = LocalDate.of(2026, 11, 2)
        val summary = SkyWeatherLocal.summaryOnLocalDay(table, local, santiago)
        val rainStart = ZonedDateTime.of(2026, 11, 2, 6, 0, 0, 0, ZoneOffset.UTC).withZoneSameInstant(santiago)
        val rainEnd = ZonedDateTime.of(2026, 11, 2, 12, 0, 0, 0, ZoneOffset.UTC).withZoneSameInstant(santiago)
        assertEquals(LocalTime.of(3, 0), rainStart.toLocalTime())
        assertEquals(LocalTime.of(9, 0), rainEnd.toLocalTime())
        assertTrue(summary.contains("Lluvia 03:00–09:00"))
        assertTrue(summary.contains("Sol (por confirmar) 00:00–03:00"))
        assertTrue(summary.contains("15:00–24:00"))
        assertFalse(summary.contains("21:00–21:00"))
        assertFalse(summary.contains("15:00–00:00"))
        assertFalse(summary.contains("☀"))
    }

    @Test
    fun consecutiveRainMergesInChileLocalTime() {
        val table = SkyCatalog.fromJson(catalogText())
        val local = LocalDate.of(2026, 11, 7)
        val summary = SkyWeatherLocal.summaryOnLocalDay(table, local, santiago)
        assertTrue(summary.contains("Lluvia"))
        assertTrue(summary.contains("03:00"))
    }

  private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
