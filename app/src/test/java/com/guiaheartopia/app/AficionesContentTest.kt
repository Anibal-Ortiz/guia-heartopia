package com.guiaheartopia.app

import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.RecetaIngrediente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AficionesContentTest {
  private val siembrasJson = """
      [
        {
          "id": "tomate",
          "nombre": "Tomate",
          "nivelSiembra": 2,
          "precioSemilla": 30,
          "fuenteSemilla": "Tienda del pueblo",
          "tiempoCrecimientoMinutos": 90,
          "precioVentaEstrellas": [10, 20, 30, 40, 50],
          "categoria": "Verduras"
        }
      ]
  """.trimIndent()

    private val floresJson = """
      {"items":[
        {"id": "rosa","nombre": "Rosa","nivelSiembra":1,
         "variantes":[{"color":"Roja","origen":"tienda","precioVenta":12}]}
      ]}
    """.trimIndent()

    private val recetasJson = """
      [
        {
          "id": "ensalada",
          "nombre": "Ensalada",
          "nivelCocina": 1,
          "energiaEstrellas": [5,6,7,8,9],
          "precioVentaEstrellas": [11,12,13,14,15],
          "ingredientes": [
            {"idSiembra":"tomate","cantidad":2},
            {"elegir":2,"opciones":[{"nombre":"Lechuga"},{"nombre":"Espinaca"}]}
          ],
          "categoria": "Ensaladas"
        }
      ]
    """.trimIndent()

    @Test
    fun parsesSiembrasFloresAndRecetas() {
        val siembras = AficionesContent.parseSiembras(siembrasJson)
        assertEquals(1, siembras.size)
        assertEquals("Tomate", siembras[0].nombre)
        assertEquals(90, siembras[0].tiempoCrecimientoMinutos)

        val flores = AficionesContent.parseFlores(floresJson)
        assertEquals("Rosa", flores.single().nombre)

        val recetas = AficionesContent.parseRecetas(recetasJson)
        assertEquals("Ensalada", recetas.single().nombre)
        assertEquals(2, recetas.single().ingredientes.size)
    }

    @Test
    fun formatGrowthAndRecipeLinks() {
        assertEquals("1 h 30 min", AficionesContent.formatGrowthMinutes(90))
        val siembra = AficionesContent.parseSiembras(siembrasJson).single()
        val recetas = AficionesContent.parseRecetas(recetasJson)
        val linked = AficionesContent.recetasQueUsanSiembra(recetas, siembra)
        assertEquals(listOf("ensalada"), linked.map { it.id })
        val line = AficionesContent.ingredienteLine(
            RecetaIngrediente(elegir = 2, opciones = listOf(
                com.guiaheartopia.app.domain.IngredienteOpcion(nombre = "Lechuga"),
                com.guiaheartopia.app.domain.IngredienteOpcion(nombre = "Espinaca"),
            )),
            siembras = siembra.let { listOf(it) },
        )
        assertTrue(line.startsWith("Elige 2 de:"))
    }
}