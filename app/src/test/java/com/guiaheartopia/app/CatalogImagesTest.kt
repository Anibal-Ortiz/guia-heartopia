package com.guiaheartopia.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogImagesTest {
    @Test
    fun everyCatalogImageIsLocalAndPresent() {
        val root = listOf(File("src/main/assets"), File("app/src/main/assets")).first { it.exists() }
        val json = File(root, "catalog/heartodex.json").readText()
        val urls = Regex(""""imageUrl"\s*:\s*"([^"]*)"""").findAll(json).map { it.groupValues[1] }.toList()
        assertTrue(urls.size >= 361)
        assertEquals(urls.size, urls.toSet().size)
        urls.forEach { url ->
            assertFalse(url.startsWith("http"))
            assertTrue(url.startsWith("asset:catalog/images/"))
            val file = File(root, url.removePrefix("asset:"))
            assertTrue("falta $url", file.exists() && file.length() > 32)
            val header = file.inputStream().use { it.readNBytes(12) }
            assertTrue(header.copyOfRange(0, 4).contentEquals("RIFF".toByteArray()))
            assertTrue(header.copyOfRange(8, 12).contentEquals("WEBP".toByteArray()))
        }
    }

    @Test
    fun uiSourcesDoNotRequestRemoteCreatureImages() {
        val src = listOf(File("src/main/java"), File("app/src/main/java")).first { it.exists() }
        val hits = src.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if ("heartodex.com/_astro" in line || "https://www.heartodex.com" in line && "image" in line.lowercase()) {
                        "${file.name}:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()
        assertEquals(emptyList<String>(), hits)
    }
}
