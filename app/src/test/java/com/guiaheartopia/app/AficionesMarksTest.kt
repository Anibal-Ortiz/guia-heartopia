package com.guiaheartopia.app

import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.SiembraEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AficionesMarksTest {
    @Test
    fun certifiedRequiresFound() {
        val after = AficionesMarks.toggleCertified(AficionesMarks.Marks(emptySet(), emptySet()), "a", true)
        assertEquals(setOf("a"), after.found)
        assertEquals(setOf("a"), after.certified)
    }

    @Test
    fun eventEntriesAreNotCertifiable() {
        val siembra = SiembraEntry(id = "x", nombre = "X", categoria = "Evento")
        val receta = RecetaEntry(id = "r", nombre = "R", certificable = false)
        val flor = FlorEntry(id = "f", nombre = "F", categoria = "Evento")
        assertFalse(AficionesMarks.certifiableSiembra(siembra))
        assertFalse(AficionesMarks.certifiableReceta(receta))
        assertFalse(AficionesMarks.certifiableFlor(flor))
    }
}