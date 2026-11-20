package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureImagePaths
import com.guiaheartopia.app.domain.GuideSections
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatureImagePathsTest {
    @Test
    fun publishedImagesAreUsedAsCandidates() {
        assertEquals(
            listOf(
                "catalog/images/wildlife-bunny.webp",
                "catalog/images/wildlife-wildlife-bunny.webp",
            ),
            CreatureImagePaths.candidates("asset:catalog/images/wildlife-bunny.webp", "wildlife-bunny"),
        )
    }

    @Test
    fun faunaIsAGuideSectionLikeTheOtherCategories() {
        assertEquals(listOf("Peces", "Pájaros", "Insectos", "Fauna"), GuideSections.dexCategories)
    }
}
