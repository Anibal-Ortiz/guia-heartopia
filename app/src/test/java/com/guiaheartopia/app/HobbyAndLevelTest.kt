package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureAvailability
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HobbyAndLevelTest {
    @Test
    fun migrationKeepsPescaAndMapsJardineriaToSiembra() {
        val saved = listOf(
            Hobby("hob-pesca", "Pesca", level = 4, note = "río"),
            Hobby("hob-jardin", "Jardinería", level = 2, note = "sur"),
            Hobby("hob-mascotas", "Mascotas", level = 9, note = "fuera"),
            Hobby("hob-manual", "Manualidades", level = 7),
        )
        val next = HobbyCatalog.migrate(saved)
        assertEquals(listOf("Cocina", "Siembra", "Pesca", "Observación de aves", "Captura de insectos"), next.map { it.name })
        assertEquals(0, next.first { it.name == "Cocina" }.level)
        assertEquals(2, next.first { it.name == "Siembra" }.level)
        assertEquals("sur", next.first { it.name == "Siembra" }.note)
        assertEquals(4, next.first { it.name == "Pesca" }.level)
        assertEquals("río", next.first { it.name == "Pesca" }.note)
        assertTrue(next.none { it.name == "Mascotas" || it.name == "Manualidades" || it.name == "Jardinería" })
    }

    @Test
    fun levelFilterHidesCreaturesAboveTheChosenCap() {
        assertTrue(CreatureAvailability.levelAllowed(3, 5))
        assertTrue(CreatureAvailability.levelAllowed(5, 5))
        assertFalse(CreatureAvailability.levelAllowed(6, 5))
        assertTrue(CreatureAvailability.levelAllowed(null, 1))
        assertTrue(CreatureAvailability.levelAllowed(9, null))
    }

    @Test
    fun availableNowAlsoRequiresTheMatchingHobbyLevel() {
        val hobbies = HobbyCatalog.migrate(
            listOf(
                Hobby("hob-pesca", "Pesca", level = 2),
                Hobby("hob-aves", "Observación de aves", level = 4),
            ),
        )
        assertFalse(CreatureAvailability.availableNow("Peces", 3, hobbies, timeAndWeatherOk = true))
        assertTrue(CreatureAvailability.availableNow("Peces", 2, hobbies, timeAndWeatherOk = true))
        assertFalse(CreatureAvailability.availableNow("Peces", 1, hobbies, timeAndWeatherOk = false))
        assertTrue(CreatureAvailability.availableNow("Pájaros", 4, hobbies, timeAndWeatherOk = true))
        assertFalse(CreatureAvailability.availableNow("Insectos", 1, hobbies, timeAndWeatherOk = true))
        assertTrue(CreatureAvailability.availableNow("Fauna", 99, hobbies, timeAndWeatherOk = true))
        assertNull(HobbyCatalog.forCategory("Fauna"))
    }
}
