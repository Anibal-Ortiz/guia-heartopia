package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import com.guiaheartopia.app.domain.SkyTable
import com.guiaheartopia.app.domain.SkyWeather
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherTest {
    @Test
    fun annualCycleRepeatsEveryYear() {
        val table = SkyCatalog.fromJson(catalogText(), "America/Santiago")
        assertTrue(table.on(LocalDate.of(2026, 11, 12)) != null)
        assertTrue(table.on(LocalDate.of(2027, 1, 1)) != null)
        assertEquals(366, table.cycle?.days?.size)
    }

    @Test
    fun rainbowCoversSunAndRainButRainDoesNotCoverRainbow() {
        assertTrue(SkyWeather.allows(listOf("Sunny"), SkyKind.ARCOIRIS))
        assertTrue(SkyWeather.allows(listOf("Rainy"), SkyKind.ARCOIRIS))
        assertTrue(SkyWeather.allows(listOf("Rainy"), SkyKind.LLUVIA))
        assertFalse(SkyWeather.allows(listOf("Rainbow"), SkyKind.LLUVIA))
        assertFalse(SkyWeather.allows(listOf("Sunny"), SkyKind.LLUVIA))
        assertTrue(SkyWeather.allows(listOf("Sunny", "Rainy", "Rainbow"), SkyKind.METEORO))
        val duringDay = LocalDate.of(2026, 11, 6).atTime(18, 0).atZone(ZoneOffset.UTC).toInstant()
        val day = LocalDate.of(2026, 11, 6)
        val table = table(
            day,
            slot(0, 6, SkyKind.LLUVIA),
            slot(6, 12, SkyKind.LLUVIA),
            slot(12, 18, SkyKind.LLUVIA),
            slot(18, 24, SkyKind.SOLEADO),
        )
        val sky = table.on(day)
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Rainy"), duringDay, sky).not())
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Sunny"), duringDay, sky))
        assertFalse(SkyWeather.availableNow(listOf("Night"), listOf("Sunny"), duringDay, sky))
    }

    @Test
    fun gameDayRainStartsInTheNightFrameNotCivilMidnight() {
        val date = LocalDate.of(2026, 11, 7)
        val table = table(
            date,
            slot(0, 6, SkyKind.DESPEJADO),
            slot(6, 12, SkyKind.LLUVIA),
            slot(12, 18, SkyKind.LLUVIA),
            slot(18, 24, SkyKind.DESPEJADO),
        )
        val spans = com.guiaheartopia.app.domain.SkyWeatherGameDay.spansForGameDay(
            table,
            date,
            ZoneId.of("America/Santiago"),
        )
        val rain = spans.filter { it.kind == SkyKind.LLUVIA }
        assertEquals(LocalTime.of(6, 0), rain.first().start.toLocalTime())
        assertEquals(LocalTime.of(18, 0), rain.last().end.toLocalTime())
    }

    private fun table(date: LocalDate, vararg slots: SkySlot): SkyTable =
        SkyTable("", "", mapOf(date to SkyDay(date, slots.toList())))

    private fun slot(from: Int, to: Int, kind: SkyKind): SkySlot = SkySlot(
        start = LocalTime.of(from % 24, 0),
        end = if (to == 24) LocalTime.MIDNIGHT else LocalTime.of(to, 0),
        kind = kind,
        status = SkyStatus.ACUERDO,
        rawKind = kind.name.lowercase(),
    )

    private fun catalogText(): String =
        File("src/test/resources/calendar/clima.json").readText()
}
