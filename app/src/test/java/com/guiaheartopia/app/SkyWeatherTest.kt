package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyStatus
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
    private val santiago = ZoneId.of("America/Santiago")

    @Test
    fun sourcesAgreeOnMostSlotsAndNovemberFirstFollowsThePatch() {
        val table = SkyCatalog.fromJson(catalogText())
        val slots = table.days.values.flatMap { it.slots }
        val agreed = slots.count { it.status == SkyStatus.ACUERDO }
        assertEquals(244, slots.size)
        assertEquals(226, agreed)
        val first = table.on(LocalDate.of(2026, 11, 1))!!
        assertTrue(first.slots.all { it.kind == SkyKind.NUBLADO && it.status == SkyStatus.POR_CONFIRMAR })
    }

    @Test
    fun consecutiveRainMergesInChileLocalTime() {
        val day = SkyCatalog.fromJson(catalogText()).on(LocalDate.of(2026, 11, 7))!!
        val spans = SkyWeather.merged(day, santiago)
        val rain = spans.first { it.kind == SkyKind.LLUVIA }
        assertEquals(LocalTime.of(3, 0), rain.start.toLocalTime())
        assertEquals(LocalTime.of(15, 0), rain.end.toLocalTime())
        assertTrue(SkyWeather.summary(day, santiago).contains("Lluvia 03:00–15:00"))
    }

    @Test
    fun rainbowCoversSunAndRainButRainDoesNotCoverRainbow() {
        assertTrue(SkyWeather.allows(listOf("Sunny"), SkyKind.ARCOIRIS))
        assertTrue(SkyWeather.allows(listOf("Rainy"), SkyKind.ARCOIRIS))
        assertTrue(SkyWeather.allows(listOf("Rainy"), SkyKind.LLUVIA))
        assertFalse(SkyWeather.allows(listOf("Rainbow"), SkyKind.LLUVIA))
        assertFalse(SkyWeather.allows(listOf("Sunny"), SkyKind.LLUVIA))
        assertTrue(SkyWeather.allows(listOf("Sunny", "Rainy", "Rainbow"), SkyKind.METEORO))
        val noon = LocalDate.of(2026, 11, 6).atTime(12, 0).atZone(ZoneOffset.UTC).toInstant()
        val day = SkyCatalog.fromJson(catalogText()).on(LocalDate.of(2026, 11, 6))
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Rainy"), noon, day).not())
        assertTrue(SkyWeather.availableNow(listOf("Day"), listOf("Sunny"), noon, day))
        assertFalse(SkyWeather.availableNow(listOf("Night"), listOf("Sunny"), noon, day))
    }

    private fun catalogText(): String = listOf(
        File("src/main/assets/calendar/clima-2026.json"),
        File("app/src/main/assets/calendar/clima-2026.json"),
    ).first { it.exists() }.readText()
}
