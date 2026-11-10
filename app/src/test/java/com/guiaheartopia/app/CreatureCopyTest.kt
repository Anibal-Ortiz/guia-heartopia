package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureCopy
import com.guiaheartopia.app.domain.StoreCopy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CreatureCopyTest {
    @Test
    fun fishBlurbUsesThePlaceAndOriginalWording() {
        val text = CreatureCopy.line("Peces", listOf("Mar antiguo"))
        assertEquals(
            "Pez de la guía. Se anota en Mar antiguo. Horario y clima van en los datos, no en este texto.",
            text,
        )
    }

    @Test
    fun birdBlurb() {
        assertEquals(
            "Ave de la guía. Se anota en Plaza. Horario y clima van en los datos, no en este texto.",
            CreatureCopy.line("Pájaros", listOf("Plaza")),
        )
    }

    @Test
    fun insectBlurbJoinsPlaces() {
        assertEquals(
            "Insecto de la guía. Se anota en Bosque, Ruinas. Horario y clima van en los datos, no en este texto.",
            CreatureCopy.line("Insectos", listOf("Bosque", "Ruinas")),
        )
    }

    @Test
    fun faunaBlurb() {
        assertEquals(
            "Animal de la guía. Se anota en Suburbio. Horario y clima van en los datos, no en este texto.",
            CreatureCopy.line("Fauna", listOf("Suburbio")),
        )
    }

    @Test
    fun unknownCategoryAndEmptyPlaceStayGeneric() {
        assertEquals(
            "Criatura de la guía. Se anota en varios puntos del mapa. Horario y clima van en los datos, no en este texto.",
            CreatureCopy.line("Otro", emptyList()),
        )
        assertFalse(CreatureCopy.line("Peces", listOf("Mar")).contains("heartodex"))
    }

    @Test
    fun unofficialNoticeIsTheSettingsText() {
        assertEquals(
            "App no oficial creada por fans. No está afiliada, asociada ni respaldada por los creadores de Heartopia. Heartopia y sus marcas pertenecen a sus respectivos dueños.",
            StoreCopy.UNOFFICIAL,
        )
    }
}
