package com.guiaheartopia.app

import com.guiaheartopia.app.domain.ImageZoom
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageDialogTest {
    @Test
    fun doubleTapZoomsInAndBack() {
        assertEquals(ImageZoom.DOUBLE_TAP, ImageZoom.doubleTap(ImageZoom.MIN))
        assertEquals(ImageZoom.MIN, ImageZoom.doubleTap(ImageZoom.DOUBLE_TAP))
    }

    @Test
    fun pinchStaysInsideTheLimits() {
        assertEquals(ImageZoom.MAX, ImageZoom.pinch(3f, 2f))
        assertEquals(ImageZoom.MIN, ImageZoom.pinch(1f, 0.2f))
        assertEquals(2f, ImageZoom.pinch(1f, 2f))
    }

    @Test
    fun enlargedImageLabelUsesTheSpanishName() {
        assertEquals("Ver imagen ampliada de Pez payaso", ImageZoom.enlargedDescription("Pez payaso"))
    }
}
