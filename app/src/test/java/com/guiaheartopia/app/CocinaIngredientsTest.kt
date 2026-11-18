package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CocinaIngredients
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.FlorVariante
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.RecetaIngrediente
import com.guiaheartopia.app.domain.ReminderIds
import com.guiaheartopia.app.domain.SiembraEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class CocinaIngredientsTest {
    @Test
    fun resolveImagen_prefersRemoteSlugWhenOnlyNombre() {
        val ref = CocinaIngredients.refForIngrediente(
            RecetaIngrediente(nombre = "Tomate Cherry"),
            siembras = emptyList(),
            recetas = emptyList(),
        )
        val path = CocinaIngredients.resolveImagen(ref, emptyList(), emptyList())
        assertEquals("cocina/images/ingredientes/${ReminderIds.slug("Tomate Cherry")}.webp", path)
    }

    @Test
    fun recetasConIngrediente_matchesCatalogKey() {
        val siembra = SiembraEntry(id = "s1", nombre = "Zanahoria")
        val ing = RecetaIngrediente(idSiembra = "s1")
        val receta = RecetaEntry(id = "r1", nombre = "Sopa", ingredientes = listOf(ing))
        val key = CocinaIngredients.refForIngrediente(ing, listOf(siembra), listOf(receta)).key
        val hits = CocinaIngredients.recetasConIngrediente(listOf(receta), listOf(siembra), key)
        assertEquals(1, hits.size)
        assertEquals("r1", hits.single().id)
    }

    @Test
    fun catalogUnique_onlyKeepsRawIngredientsNotInOtherCatalogs() {
        val siembra = SiembraEntry(id = "s1", nombre = "Zanahoria")
        val flor = FlorEntry(
            id = "f1",
            nombre = "Rosa roja",
            variantes = listOf(FlorVariante(color = "Blanca", origen = "Semilla", nombre = "Margarita blanca")),
        )
        val plato = RecetaEntry(id = "r2", nombre = "Salsa de Tomate")
        val sopa = RecetaEntry(
            id = "r1",
            nombre = "Sopa",
            ingredientes = listOf(
                RecetaIngrediente(nombre = "Tomate Cherry"),
                RecetaIngrediente(idReceta = "r2"),           // plato
                RecetaIngrediente(idSiembra = "s1"),         // huerto/siembra
                RecetaIngrediente(nombre = "Camarón azul"),  // pez
                RecetaIngrediente(nombre = "Cualquier Pez"), // comodín de pez
                RecetaIngrediente(nombre = "Rosa roja"),     // flor
                RecetaIngrediente(nombre = "Margarita blanca"), // variante de flor
            ),
        )
        val recetas = listOf(sopa, plato)

        val catalog = CocinaIngredients.catalogUnique(
            recetas = recetas,
            siembras = listOf(siembra),
            flores = listOf(flor),
            pecesNombres = setOf("Camarón azul"),
        )

        assertEquals(listOf("Tomate Cherry"), catalog.map { it.displayName })
    }

    @Test
    fun linesForReceta_stillKeepsAllIngredientsForRecipeDetail() {
        val plato = RecetaEntry(id = "r2", nombre = "Salsa de Tomate")
        val sopa = RecetaEntry(
            id = "r1",
            nombre = "Sopa",
            ingredientes = listOf(
                RecetaIngrediente(nombre = "Tomate Cherry"),
                RecetaIngrediente(idReceta = "r2"),
            ),
        )
        val lines = CocinaIngredients.linesForReceta(sopa, emptyList(), listOf(sopa, plato))
        assertEquals(2, lines.size)
    }
}