package com.guiaheartopia.app

import com.guiaheartopia.app.domain.HobbyContent
import com.guiaheartopia.app.domain.HobbyEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HobbyContentSearchTest {
    private val items = listOf(
        HobbyEntry(
            id = "cafe",
            nombre = "Café con leche",
            ingredientes = listOf("Café", "Leche"),
            temporada = "Invierno",
        ),
        HobbyEntry(
            id = "pina",
            nombre = "Piña colada",
            ingredientes = listOf("Piña"),
            temporada = "Verano",
        ),
        HobbyEntry(
            id = "ensalada",
            nombre = "Ensalada de tomate",
            ingredientes = listOf("Tomate", "Lechuga"),
            temporada = "Verano",
        ),
        HobbyEntry(
            id = "te",
            nombre = "Té verde",
            ingredientes = listOf("Té"),
        ),
    )

    @Test
    fun searchIgnoresAccents() {
        assertEquals(listOf("cafe"), names(HobbyContent.search(items, "cafe")))
        assertEquals(listOf("pina"), names(HobbyContent.search(items, "pina")))
        assertEquals(listOf("te"), names(HobbyContent.search(items, "verde")))
    }

    @Test
    fun searchMatchesAccentedQueriesToo() {
        assertEquals(listOf("cafe"), names(HobbyContent.search(items, "CAFÉ")))
        assertEquals(listOf("pina"), names(HobbyContent.search(items, "Piña")))
        assertEquals(listOf("te"), names(HobbyContent.search(items, "TÉ VERDE")))
    }

    @Test
    fun searchIgnoresUppercaseAndLowercase() {
        assertEquals(listOf("ensalada"), names(HobbyContent.search(items, "ENSALADA")))
        assertEquals(listOf("ensalada"), names(HobbyContent.search(items, "ensalada")))
        assertEquals(listOf("ensalada"), names(HobbyContent.search(items, "EnSaLaDa")))
    }

    @Test
    fun emptyOrBlankQueryReturnsEverything() {
        assertEquals(items, HobbyContent.search(items, ""))
        assertEquals(items, HobbyContent.search(items, "   "))
        assertEquals(items, HobbyContent.search(items, "\n\t "))
        assertEquals(emptyList<HobbyEntry>(), HobbyContent.search(emptyList(), ""))
    }

    @Test
    fun queryAlsoMatchesIngredientsWithoutAccents() {
        assertEquals(listOf("cafe"), names(HobbyContent.search(items, "leche")))
        assertEquals(listOf("cafe"), names(HobbyContent.search(items, "CAFE")))
        assertEquals(listOf("ensalada"), names(HobbyContent.search(items, "lechuga")))
        assertEquals(listOf("ensalada"), names(HobbyContent.search(items, "tomate")))
    }

    @Test
    fun queryAlsoMatchesSeason() {
        assertEquals(listOf("cafe"), names(HobbyContent.search(items, "invierno")))
        assertEquals(listOf("pina", "ensalada"), names(HobbyContent.search(items, "VERANO")))
    }

    @Test
    fun searchWithNoMatchesReturnsEmptyList() {
        assertTrue(HobbyContent.search(items, "dragón").isEmpty())
        assertTrue(HobbyContent.search(items, "zzz").isEmpty())
    }

    @Test
    fun searchKeepsTheOriginalOrder() {
        assertEquals(listOf("cafe", "pina", "ensalada"), names(HobbyContent.search(items, "a")))
    }

    private fun names(entries: List<HobbyEntry>): List<String> = entries.map { it.id }
}
