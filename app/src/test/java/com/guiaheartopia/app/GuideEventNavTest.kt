package com.guiaheartopia.app

import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.domain.EventosDocument
import com.guiaheartopia.app.domain.GuideEventNav
import com.guiaheartopia.app.domain.GuideSections
import com.guiaheartopia.app.domain.RemoteEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class GuideEventNavTest {
    @Test
    fun insectEventOpensInsectsTab() {
        val doc = EventosDocument(
            eventos = listOf(
                RemoteEvent(id = "evt-ins", categoria = "Insectos", nombre = "Insectos de evento"),
            ),
        )
        val catalog = listOf(
            DexEntry(id = "i1", name = "Mariquita", category = "Insectos", event = "Insectos de evento"),
        )
        val tab = GuideEventNav.dexTabIndex(doc, catalog, "evt-ins")
        assertEquals(GuideSections.dexCategories.indexOf("Insectos"), tab)
    }
}