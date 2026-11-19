package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureImagePaths
import com.guiaheartopia.app.domain.GuideSections
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatureImagePathsTest {
    @Test
    fun imagesAreDisabledUntilTheNewSetIsPublished() {
        assertEquals(
            emptyList<String>(),
            CreatureImagePaths.candidates("asset:catalog/images/wildlife-bunny.webp", "wildlife-bunny"),
        )
    }

    @Test
    fun faunaIsAGuideSectionLikeTheOtherCategories() {
        assertEquals(listOf("Peces", "Pájaros", "Insectos", "Fauna"), GuideSections.dexCategories)
    }
}
