package com.guiaheartopia.app

import com.guiaheartopia.app.data.PhotoCreditEntry
import com.guiaheartopia.app.data.PhotoCredits
import com.guiaheartopia.app.data.PhotoCreditsFile
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoCreditsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesWithUnknownKeysAndBuildsCaption() {
        val raw = """
            {
              "entries": [
                {
                  "id": "fish-trout",
                  "category": "Peces",
                  "name": "Trucha",
                  "scientificName": "Salmo trutta",
                  "author": "Jane Doe",
                  "license": "CC BY 4.0",
                  "licenseUrl": "https://creativecommons.org/licenses/by/4.0/",
                  "sourcePage": "https://commons.wikimedia.org/wiki/File:Trout.jpg",
                  "extra": "ignored"
                }
              ]
            }
        """.trimIndent()
        val file = json.decodeFromString(PhotoCreditsFile.serializer(), raw)
        assertEquals(1, file.entries.size)
        assertEquals("Jane Doe", file.entries.single().author)
        assertEquals("Foto: Jane Doe · CC BY 4.0", PhotoCredits.caption(file.entries.single()))
    }

    @Test
    fun emptyAuthorUsesDefaultLabel() {
        val entry = PhotoCreditEntry(id = "x", author = "  ", license = "CC0 1.0")
        assertEquals("sin atribución requerida", PhotoCredits.authorLabel(entry.author))
        assertEquals("Foto: sin atribución requerida · CC0 1.0", PhotoCredits.caption(entry))
    }

    @Test
    fun groupsBirdsUnderAvesAndKeepsOrder() {
        val grouped = PhotoCredits.grouped(
            listOf(
                PhotoCreditEntry(id = "1", category = "Fauna", name = "Zorro"),
                PhotoCreditEntry(id = "2", category = "Pájaros", name = "Arrendajo"),
                PhotoCreditEntry(id = "3", category = "Peces", name = "Atún"),
                PhotoCreditEntry(id = "4", category = "Insectos", name = "Mariposa"),
            ),
        )
        assertEquals(listOf("Peces", "Aves", "Insectos", "Fauna"), grouped.map { it.first })
        assertEquals("Arrendajo", grouped[1].second.single().name)
    }

    @Test
    fun bundledCreditsFileExistsInAssets() {
        val root = listOf(File("src/main/assets"), File("app/src/main/assets")).first { it.exists() }
        val file = File(root, PhotoCredits.PATH)
        assertTrue(file.isFile)
        val parsed = json.decodeFromString(PhotoCreditsFile.serializer(), file.readText())
        assertTrue(parsed.entries.size > 100)
        val sample = parsed.entries.first { it.id == "fish-blue-sea-angel" }
        assertNotNull(sample.scientificName)
        assertTrue(sample.licenseUrl.startsWith("https://"))
    }
}
