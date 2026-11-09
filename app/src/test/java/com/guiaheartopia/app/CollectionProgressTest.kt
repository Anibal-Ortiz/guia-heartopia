package com.guiaheartopia.app

import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.CollectionProgress
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.Greeting
import com.guiaheartopia.app.domain.HobbyProgress
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionProgressTest {
    @Test
    fun categoryProgressAndHobbyClamp() {
        val items = listOf(
            Collectible("1", "a", "Naturaleza", ""),
            Collectible("2", "b", "Naturaleza", ""),
            Collectible("3", "c", "Objetos", ""),
        )
        val progress = CollectionProgress.byCategory(items, setOf("1", "3"))
        assertEquals(2, progress.size)
        assertEquals(0.5f, progress.first { it.category == "Naturaleza" }.fraction)
        assertEquals(1f, progress.first { it.category == "Objetos" }.fraction)
        assertEquals(5 to 5, HobbyProgress.clamp(9, 5))
        assertEquals(0f, HobbyProgress.fraction(0, 10))
    }

    @Test
    fun countryResolverDoesNotInventAZone() {
        assertEquals("CL" to "America/Santiago", Countries.resolve("CL", ZoneId.of("America/Santiago")))
        assertEquals("CL" to "Pacific/Easter", Countries.resolve("CL", ZoneId.of("Pacific/Easter")))
        assertEquals("JP" to "Asia/Tokyo", Countries.resolve("", ZoneId.of("Asia/Tokyo")))
        assertEquals("CL" to "America/Santiago", Countries.resolve("ZZ", ZoneId.of("Etc/GMT+7")))
    }

    @Test
    fun greetingFollowsTheHour() {
        assertTrue(Greeting.line(8, "Ana").startsWith("Buenos días"))
        assertTrue(Greeting.line(15, "Ana").startsWith("Buenas tardes"))
        assertTrue(Greeting.line(22, "").startsWith("Buenas noches"))
    }
}
