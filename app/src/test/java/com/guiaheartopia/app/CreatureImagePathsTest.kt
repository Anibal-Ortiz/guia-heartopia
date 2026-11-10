package com.guiaheartopia.app

import com.guiaheartopia.app.domain.CreatureImagePaths
import com.guiaheartopia.app.domain.GuideSections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatureImagePathsTest {
    @Test
    fun faunaDoubledWildlifePrefixAlsoLooksUpTheRealFile() {
        val paths = CreatureImagePaths.candidates(
            "asset:catalog/images/wildlife-wildlife-panda.webp",
            "wildlife-panda",
        )
        assertEquals("catalog/images/wildlife-wildlife-panda.webp", paths.first())
        assertTrue(paths.contains("catalog/images/wildlife-panda.webp"))
    }

    @Test
    fun plainWildlifePathStaysFirst() {
        val paths = CreatureImagePaths.candidates(
            "asset:catalog/images/wildlife-bunny.webp",
            "wildlife-bunny",
        )
        assertEquals("catalog/images/wildlife-bunny.webp", paths.first())
    }

    @Test
    fun faunaIsAGuideSectionLikeTheOtherCategories() {
        assertEquals(listOf("Peces", "Pájaros", "Insectos", "Fauna"), GuideSections.dexCategories)
    }
}
