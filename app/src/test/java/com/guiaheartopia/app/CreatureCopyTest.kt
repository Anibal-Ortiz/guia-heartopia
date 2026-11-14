package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureCopy
import com.guiaheartopia.app.domain.StoreCopy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CreatureCopyTest {
    @Test
    fun blankAndFillerDescriptionsStayHidden() {
        assertEquals("", CreatureCopy.visible(""))
        assertEquals("", CreatureCopy.visible("   "))
        assertEquals(
            "",
            CreatureCopy.visible(
                "Insecto de la guía. Se anota en Isla del bosque. Horario y clima van en los datos, no en este texto.",
            ),
        )
        assertTrueFiller("Pez de la guía. Se anota en Mar antiguo. Horario y clima van en los datos, no en este texto.")
        assertTrueFiller("Ave de la guía. Se anota en Plaza. Horario y clima van en los datos, no en este texto.")
        assertTrueFiller("Animal de la guía. Se anota en Suburbio. Horario y clima van en los datos, no en este texto.")
    }

    @Test
    fun realDescriptionIsKept() {
        assertEquals("Nada cerca de la orilla.", CreatureCopy.visible("  Nada cerca de la orilla.  "))
        assertFalse(CreatureCopy.isFiller("Nada cerca de la orilla."))
    }

    @Test
    fun unofficialNoticeIsTheSettingsText() {
        assertEquals(
            "App no oficial, no afiliada ni respaldada por los creadores de Heartopia.",
            StoreCopy.UNOFFICIAL,
        )
    }

    private fun assertTrueFiller(text: String) {
        org.junit.Assert.assertTrue(CreatureCopy.isFiller(text))
        assertEquals("", CreatureCopy.visible(text))
    }
}
