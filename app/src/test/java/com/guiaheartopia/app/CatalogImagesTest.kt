package com.guiaheartopia.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogImagesTest {
    @Test
    fun apkSourcesDoNotShipCatalogImages() {
        val root = listOf(File("src/main/assets"), File("app/src/main/assets")).first { it.exists() }
        val images = File(root, "catalog/images")
        val shipped = if (images.isDirectory) {
            images.walkTopDown().filter { it.isFile }.map { it.relativeTo(root).path }.toList()
        } else {
            emptyList()
        }
        assertEquals(emptyList<String>(), shipped)

        val rasters = root.walkTopDown().filter { file ->
            file.isFile && file.extension.lowercase() in setOf("webp", "png", "jpg", "jpeg", "gif")
        }.map { it.relativeTo(root).path }.toList()
        assertEquals(emptyList<String>(), rasters)

        val manifest = File(root, "content/manifest.json").readText()
        assertFalse(manifest.contains("catalog/images/"))

        val json = File(root, "catalog/creatures.json").readText()
        assertFalse(json.contains("imageNote"))
        assertFalse(json.contains("https://"))
        assertTrue(json.contains("\"imageUrl\": \"\""))
    }

    @Test
    fun uiSourcesDoNotRequestRemoteCreatureImages() {
        val src = listOf(File("src/main/java"), File("app/src/main/java")).first { it.exists() }
        val paths = src.walkTopDown().first { it.name == "ContentPaths.kt" }.readText()
        assertTrue(paths.contains("https://"))
        assertTrue(paths.contains("return null"))
        val model = src.walkTopDown().first { it.name == "CreatureImage.kt" }.readText()
        assertTrue(model.contains("return null"))
        assertFalse(model.contains("file:///android_asset/"))
    }
}
