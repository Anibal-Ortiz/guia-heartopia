package com.guiaheartopia.app

import com.guiaheartopia.app.data.DexFile
import com.guiaheartopia.app.domain.TextMatch
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanishCatalogTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun searchIgnoresAccentsAndStillMatchesTheEnglishName() {
        assertTrue(TextMatch.contains("Ángel de mar azul", "angel"))
        assertTrue(TextMatch.contains("Ángel de mar azul", "azul"))
        assertTrue(TextMatch.contains("Blue Sea Angel", "blue sea"))
        assertFalse(TextMatch.contains("Ángel de mar azul", "trucha"))
        assertFalse(TextMatch.contains("Blue Sea Angel", "caballito"))
    }

    @Test
    fun savedIdsKeepTheirSpanishNameAndDoNotDropTheEnglishKey() {
        val angel = load().entries.first { it.id == "fish-blue-sea-angel" }
        assertEquals("Ángel de mar azul", angel.name)
        assertEquals("Blue Sea Angel", angel.nameEn)
        assertEquals(listOf("Mar antiguo"), angel.location)
        assertEquals(1, angel.level)
        assertEquals("Pequeño", angel.shadow)
        assertFalse(angel.name.contains("Blue"))
        assertEquals("fish-trout", load().entries.first { it.nameEn == "Trout" }.id)
    }

    @Test
    fun everyCategoryHasASpanishName() {
        val entries = load().entries
        val counts = entries.groupingBy { it.category }.eachCount()
        val missing = entries.filter { it.name.isBlank() }.map { it.id }
        assertEquals(emptyList<String>(), missing)
        assertEquals(130, counts["Peces"])
        assertEquals(114, counts["Pájaros"])
        assertEquals(106, counts["Insectos"])
        assertEquals(11, counts["Fauna"])
    }

    private fun load(): DexFile {
        val file = listOf(
            File("src/main/assets/catalog/heartodex.json"),
            File("app/src/main/assets/catalog/heartodex.json"),
        ).first { it.exists() }
        return json.decodeFromString(DexFile.serializer(), file.readText())
    }
}
