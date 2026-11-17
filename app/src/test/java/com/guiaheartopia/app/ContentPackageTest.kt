package com.guiaheartopia.app

import com.guiaheartopia.app.domain.ContentFileEntry
import com.guiaheartopia.app.domain.ContentLibrary
import com.guiaheartopia.app.domain.ContentManifest
import com.guiaheartopia.app.domain.ContentPackage
import com.guiaheartopia.app.domain.ContentSyncResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPackageTest {
    private val baseUrl = "https://anibalortizdev.github.io/guia-heartopia-contenido/"
    private val validSha = "a".repeat(64)

    @Test
    fun validManifestParsesAndLowercasesHashes() {
        val manifest = ContentManifest(
            contentVersion = 3,
            minAppVersionCode = 23,
            publishedOn = "2026-12-01",
            files = listOf(
                ContentFileEntry("catalog/creatures.json", 12, "ABCDEF".lowercase() + "0".repeat(58)),
                ContentFileEntry("calendar/eventos.json", 0, "A".repeat(64), minAppVersionCode = 24),
            ),
        )

        val parsed = ContentPackage.parseManifest(ContentPackage.encodeManifest(manifest))

        assertNotNull(parsed)
        assertEquals(3, parsed!!.contentVersion)
        assertEquals(23, parsed.minAppVersionCode)
        assertEquals("2026-12-01", parsed.publishedOn)
        assertEquals(listOf("catalog/creatures.json", "calendar/eventos.json"), parsed.files.map { it.path })
        assertTrue(parsed.files.all { it.sha256 == it.sha256.lowercase() })
        assertEquals("a".repeat(64), parsed.files.last().sha256)
        assertEquals(24, parsed.files.last().minAppVersionCode)
    }

    @Test
    fun parseManifestRejectsMalformedOrIncompleteJson() {
        assertNull(ContentPackage.parseManifest(""))
        assertNull(ContentPackage.parseManifest("{"))
        assertNull(ContentPackage.parseManifest("null"))
        assertNull(ContentPackage.parseManifest("""{"contentVersion":1}"""))
    }

    @Test
    fun parseManifestRejectsInvalidVersionsOrDates() {
        assertNull(ContentPackage.parseManifest(manifestJson(contentVersion = 0)))
        assertNull(ContentPackage.parseManifest(manifestJson(minAppVersionCode = -1)))
        assertNull(ContentPackage.parseManifest(manifestJson(publishedOn = " ")))
        assertNull(ContentPackage.parseManifest(manifestJson(files = """[{"path":"a.json","size":1,"sha256":"$validSha","minAppVersionCode":-2}]""")))
    }

    @Test
    fun parseManifestRejectsUnsafePaths() {
        val unsafe = listOf(
            "",
            " ",
            "/etc/passwd",
            "catalog\\creatures.json",
            "../manifest.json",
            "catalog/../../secreto.json",
            "catalog//creatures.json",
            "./catalog/creatures.json",
            "catalog/./creatures.json",
            "catalog/",
        )
        unsafe.forEach { path ->
            assertNull("debería rechazar la ruta \"$path\"", ContentPackage.parseManifest(manifestJson(path = path)))
        }
    }

    @Test
    fun parseManifestRejectsBrokenFileEntries() {
        assertNull(ContentPackage.parseManifest(manifestJson(size = -1)))
        assertNull(ContentPackage.parseManifest(manifestJson(sha = "nope")))
        assertNull(ContentPackage.parseManifest(manifestJson(sha = "A".repeat(63))))
        assertNull(ContentPackage.parseManifest(manifestJson(sha = "G".repeat(64))))
        assertNull(
            ContentPackage.parseManifest(
                manifestJson(
                    files = """[{"path":"a.json","size":1,"sha256":"$validSha"},""" +
                        """{"path":"a.json","size":2,"sha256":"${"b".repeat(64)}"}]""",
                ),
            ),
        )
    }

    @Test
    fun safePathsAreAccepted() {
        listOf(
            "manifest.json",
            "catalog/creatures.json",
            "catalog/images/trucha.png",
            "calendar/eventos.json",
            "a.b.c",
        ).forEach { path ->
            assertTrue("debería aceptar la ruta \"$path\"", ContentPackage.isSafePath(path))
        }
    }

    @Test
    fun unsafePathsAreRejected() {
        listOf(
            "",
            " ",
            "/absoluta.png",
            "carpeta\\archivo.json",
            "..",
            "../archivo.json",
            "a/../../b.json",
            "a//b.json",
            "./a.json",
            "a/./b.json",
            "a/",
        ).forEach { path ->
            assertFalse("debería rechazar la ruta \"$path\"", ContentPackage.isSafePath(path))
        }
    }

    @Test
    fun sha256MatchesKnownVectors() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            ContentPackage.sha256("abc".toByteArray()),
        )
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            ContentPackage.sha256(ByteArray(0)),
        )
        assertEquals(64, ContentPackage.sha256("cualquier contenido".toByteArray()).length)
    }

    @Test
    fun sha256DistinguishesDifferentContent() {
        val original = """{"id":"trucha","name":"Trucha"}""".toByteArray()
        val changed = """{"id":"trucha","name":"Trucha nueva"}""".toByteArray()

        assertNotEquals(ContentPackage.sha256(original), ContentPackage.sha256(changed))
        assertEquals(ContentPackage.sha256(original), ContentPackage.sha256(original.copyOf()))
    }

    @Test
    fun syncAppliesWhenSha256MatchesIgnoringHashCase() {
        val bytes = """{"id":"trucha","name":"Trucha"}""".toByteArray()
        val manifest = ContentManifest(
            contentVersion = 2,
            minAppVersionCode = 23,
            publishedOn = "2026-12-01",
            files = listOf(ContentFileEntry("catalog/creatures.json", bytes.size.toLong(), ContentPackage.sha256(bytes).uppercase())),
        )
        val library = FakeLibrary()
        val served = mapOf(
            ContentPackage.manifestUrl(baseUrl) to ContentPackage.encodeManifest(manifest).toByteArray(),
            ContentPackage.fileUrl(baseUrl, "catalog/creatures.json") to bytes,
        )

        val report = ContentPackage.sync(library, { served[it] }, appVersionCode = 23, baseUrl = baseUrl)

        assertEquals(ContentSyncResult.APPLIED, report.result)
        assertEquals(2, ContentPackage.currentVersion(library))
        assertTrue(bytes.contentEquals(library.overlayBytes("catalog/creatures.json")))
    }

    @Test
    fun syncRejectsDownloadedFileWhoseSha256DoesNotMatch() {
        val expected = """{"id":"trucha","name":"Trucha"}""".toByteArray()
        val servedBytes = """{"id":"trucha","name":"Otra trucha"}""".toByteArray()
        val manifest = ContentManifest(
            contentVersion = 2,
            minAppVersionCode = 23,
            publishedOn = "2026-12-01",
            files = listOf(ContentFileEntry("catalog/creatures.json", servedBytes.size.toLong(), ContentPackage.sha256(expected))),
        )
        val library = FakeLibrary()
        val served = mapOf(
            ContentPackage.manifestUrl(baseUrl) to ContentPackage.encodeManifest(manifest).toByteArray(),
            ContentPackage.fileUrl(baseUrl, "catalog/creatures.json") to servedBytes,
        )

        val report = ContentPackage.sync(library, { served[it] }, appVersionCode = 23, baseUrl = baseUrl)

        assertEquals(ContentSyncResult.VERIFY_FAILED, report.result)
        assertNull(library.overlayBytes("catalog/creatures.json"))
        assertEquals(1, ContentPackage.currentVersion(library))
    }

    private fun manifestJson(
        contentVersion: Int = 1,
        minAppVersionCode: Int = 23,
        publishedOn: String = "2026-12-01",
        path: String = "catalog/creatures.json",
        size: Long = 1,
        sha: String = validSha,
        files: String = """[{"path":"$path","size":$size,"sha256":"$sha"}]""",
    ): String =
        """{"contentVersion":$contentVersion,"minAppVersionCode":$minAppVersionCode,""" +
            """"publishedOn":"$publishedOn","files":$files}"""

    private class FakeLibrary : ContentLibrary {
        private var installed: ContentManifest? = null
        private val overlay = linkedMapOf<String, ByteArray>()
        private var needsNewerApp = false

        override fun bundledManifest(): ContentManifest =
            ContentManifest(contentVersion = 1, minAppVersionCode = 23, publishedOn = "2026-11-01")

        override fun installedManifest(): ContentManifest? = installed

        override fun effectiveBytes(path: String): ByteArray? = overlay[path]

        override fun overlayBytes(path: String): ByteArray? = overlay[path]

        override fun publish(manifest: ContentManifest, overlay: Map<String, ByteArray>) {
            this.overlay.clear()
            this.overlay.putAll(overlay)
            installed = manifest
        }

        override fun needsNewerApp(): Boolean = needsNewerApp

        override fun setNeedsNewerApp(value: Boolean) {
            needsNewerApp = value
        }
    }
}
