package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyWeather
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkyWeatherTest {
    @Test
    fun shippedClimateFileHasNoDatedSlots() {
        val table = SkyCatalog.fromJson(catalogText())
        assertTrue(table.days.isEmpty())
        assertEquals(null, table.on(LocalDate.of(2026, 11, 1)))
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
        val day = SkyCatalog.fromJson(
            """
            {"days":[{"date":"2026-11-06","slots":[
              {"start":"00:00","end":"06:00","kind":"lluvia","status":"acuerdo"},
              {"start":"06:00","end":"12:00","kind":"lluvia","status":"acuerdo"},
              {"start":"12:00","end":"18:00","kind":"lluvia","status":"acuerdo"},
              {"start":"18:00","end":"24:00","kind":"soleado","status":"acuerdo"}
            ]}]}
            """.trimIndent(),
        ).on(LocalDate.of(2026, 11, 6))
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Rainy"), duringDay, day).not())
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Sunny"), duringDay, day))
        assertFalse(SkyWeather.availableNow(listOf("Night"), listOf("Sunny"), duringDay, day))
    }

    @Test
    fun gameDayRainUsesDawnFrameNotCivilMidnight() {
        val table = SkyCatalog.fromJson(
            """
            {"days":[{"date":"2026-11-07","slots":[
              {"start":"00:00","end":"06:00","kind":"despejado","status":"acuerdo"},
              {"start":"06:00","end":"12:00","kind":"lluvia","status":"acuerdo"},
              {"start":"12:00","end":"18:00","kind":"lluvia","status":"acuerdo"},
              {"start":"18:00","end":"24:00","kind":"despejado","status":"acuerdo"}
            ]}]}
            """.trimIndent(),
        )
        val spans = com.guiaheartopia.app.domain.SkyWeatherGameDay.spansForGameDay(
            table,
            LocalDate.of(2026, 11, 7),
            java.time.ZoneId.of("America/Santiago"),
        )
        val dawnRain = spans.first { it.frameKey == "Dawn" && it.kind == SkyKind.LLUVIA }
        assertEquals(LocalTime.of(8, 0), dawnRain.start.toLocalTime())
        assertEquals(LocalTime.of(14, 0), dawnRain.end.toLocalTime())
    }

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
