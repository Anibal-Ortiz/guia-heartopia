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
    fun listWithoutOnlyNowIgnoresHobbyLevelCap() {
        val hobbies = HobbyCatalog.migrate(listOf(Hobby("hob-pesca", "Pesca", level = 0)))
        assertTrue(CreatureAvailability.levelAllowed(9, null))
        assertFalse(CreatureAvailability.availableNow("Peces", 9, hobbies, timeAndWeatherOk = true))
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

    @Test
    fun fixedCapsAreTheVerifiedOnes() {
        assertEquals(13, HobbyCatalog.CAP_COCINA)
        assertEquals(13, HobbyCatalog.CAP_SIEMBRA)
        assertEquals(14, HobbyCatalog.CAP_PESCA)
        assertEquals(14, HobbyCatalog.CAP_AVES)
        assertEquals(14, HobbyCatalog.CAP_INSECTOS)
        assertEquals(13, HobbyCatalog.fixed.first { it.id == "hob-cocina" }.maxLevel)
        assertEquals(14, HobbyCatalog.fixed.first { it.id == "hob-insectos" }.maxLevel)
    }

    @Test
    fun savedCapOfTenBecomesTheRealCapAndKeepsTheLevel() {
        val next = HobbyCatalog.migrate(
            listOf(Hobby("hob-pesca", "Pesca", level = 8, maxLevel = 10, note = "río")),
        )
        val pesca = next.first { it.id == "hob-pesca" }
        assertEquals(14, pesca.maxLevel)
        assertEquals(8, pesca.level)
        assertEquals("río", pesca.note)
    }

    @Test
    fun levelAboveTheCapIsClamped() {
        val next = HobbyCatalog.migrate(
            listOf(Hobby("hob-cocina", "Cocina", level = 20, maxLevel = 10)),
        )
        val cocina = next.first { it.id == "hob-cocina" }
        assertEquals(13, cocina.maxLevel)
        assertEquals(13, cocina.level)
    }

    @Test
    fun remoteOverrideAppliesAndAMissingFileKeepsTheConstants() {
        val parsed = HobbyCatalog.parseOverrides("""{"hob-pesca":16,"hob-cocina":11}""")
        val raised = HobbyCatalog.migrate(emptyList(), parsed)
        assertEquals(16, raised.first { it.id == "hob-pesca" }.maxLevel)
        assertEquals(11, raised.first { it.id == "hob-cocina" }.maxLevel)
        assertEquals(emptyMap<String, Int>(), HobbyCatalog.parseOverrides(null))
        assertEquals(emptyMap<String, Int>(), HobbyCatalog.parseOverrides("{"))
        val plain = HobbyCatalog.migrate(emptyList(), HobbyCatalog.parseOverrides(null))
        assertEquals(14, plain.first { it.id == "hob-pesca" }.maxLevel)
    }

    @Test
    fun guideFilterReachesTheRealHobbyCap() {
        assertEquals(14, HobbyCatalog.guideCap("Peces"))
        assertEquals(14, HobbyCatalog.guideCap("Pájaros"))
        assertEquals(14, HobbyCatalog.guideCap("Insectos"))
        assertNull(HobbyCatalog.guideCap("Fauna"))
        val raw = java.io.File("src/main/assets/catalog/creatures.json").readText()
        val snail = Regex(""""name": "Caracol verde"[\s\S]*?"level": (\d+)""").find(raw)
        assertEquals("14", snail?.groupValues?.get(1))
        assertTrue(raw.contains(""""category": "Insectos""""))
    }
}
