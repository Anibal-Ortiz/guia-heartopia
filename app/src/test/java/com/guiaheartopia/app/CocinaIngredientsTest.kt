package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CocinaIngredients
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
}