package com.guiaheartopia.app

import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.CollectionProgress
import com.guiaheartopia.app.domain.Countries
import com.guiaheartopia.app.domain.Greeting
import com.guiaheartopia.app.domain.HobbyProgress
import com.guiaheartopia.app.pixel.ArgbImage
import com.guiaheartopia.app.pixel.BackgroundFilter
import com.guiaheartopia.app.pixel.ColorMath
import com.guiaheartopia.app.pixel.ConvertOptions
import com.guiaheartopia.app.pixel.DefaultPalette
import com.guiaheartopia.app.pixel.FitMode
import com.guiaheartopia.app.pixel.GuideViewport
import com.guiaheartopia.app.pixel.PaletteColor
import com.guiaheartopia.app.pixel.PixelConverter
import com.guiaheartopia.app.pixel.PixelProject
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PixelAndProgressTest {
    @Test
    fun whiteIsFartherInLightnessThanBlack() {
        val white = ColorMath.argbToLab(0xFFFFFFFF.toInt())
        val black = ColorMath.argbToLab(0xFF000000.toInt())
        assertTrue(white[0] > black[0])
        assertEquals(0f, ColorMath.dist2(white, ColorMath.argbToLab(0xFFFFFFFF.toInt())))
        assertTrue(PixelConverter.isNearWhite(white))
    }

    @Test
    fun solidRedBecomesOnePaletteColor() {
        val red = 0xFFD02F48.toInt()
        val image = ArgbImage(8, 8, IntArray(64) { red })
        val project = PixelConverter.convertImage(
            image,
            DefaultPalette.colors,
            ConvertOptions(cols = 2, rows = 2, crispLines = true, skipWhiteBackground = false),
        )
        assertEquals(1, project.colors.size)
        assertTrue(project.cells.all { it == 0 })
        assertEquals(4, project.paintableCount)
    }

    @Test
    fun whiteBorderIsClearedAndCenterRemains() {
        val white = 0xFFFFFFFF.toInt()
        val red = 0xFFD02F48.toInt()
        val pixels = IntArray(16) { white }
        // Bloque rojo de 2×2, no un puntito suelto: el conversor conserva las líneas y quita el blanco del borde.
        pixels[5] = red
        pixels[6] = red
        pixels[9] = red
        pixels[10] = red
        val project = PixelConverter.convertImage(
            ArgbImage(4, 4, pixels),
            DefaultPalette.colors,
            ConvertOptions(cols = 4, rows = 4, crispLines = true, skipWhiteBackground = true, fitMode = FitMode.STRETCH),
        )
        assertEquals(-1, project.colorAt(0, 0))
        assertTrue(project.colorAt(1, 1) >= 0)
        assertNotEquals("#FFFFFF", project.colors[project.colorAt(1, 1)].hex)
    }

    @Test
    fun maxColorsKeepsASingleIndex() {
        val pixels = IntArray(16) { index -> if (index < 8) 0xFFD02F48.toInt() else 0xFF005BA5.toInt() }
        val project = PixelConverter.convertImage(
            ArgbImage(4, 4, pixels),
            DefaultPalette.colors,
            ConvertOptions(cols = 2, rows = 2, maxColors = 1, crispLines = false, dithering = false, skipWhiteBackground = false),
        )
        assertEquals(1, project.colors.size)
    }

    @Test
    fun enclosedLightCellIsNotTreatedAsBackground() {
        val white = PaletteColor("#FFFFFF", "blanco", 1, 1)
        val red = PaletteColor("#D02F48", "rojo", 2, 1)
        val cells = MutableList(9) { 0 }
        cells[4] = 0
        // Borde rojo, centro blanco: el blanco no toca el borde.
        for (i in cells.indices) cells[i] = 1
        cells[4] = 0
        val project = PixelProject(3, 3, listOf(white, red), cells)
        val skip = BackgroundFilter.skipMask(project, skipBackground = true)
        assertTrue(skip.none { it })
    }

    @Test
    fun viewportTapHitsTheCenterCell() {
        val hit = GuideViewport.cellAt(100f, 100f, 2, 2, 1f, 0f, 0f, 75f, 75f)
        assertEquals(1 to 1, hit)
        val outside = GuideViewport.cellAt(100f, 100f, 2, 2, 1f, 0f, 0f, -10f, 10f)
        assertEquals(null, outside)
    }

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
