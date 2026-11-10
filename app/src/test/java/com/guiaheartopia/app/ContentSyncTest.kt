package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.data.mergeCreatures
import com.guiaheartopia.app.data.mergeSkyTables
import com.guiaheartopia.app.domain.ContentFileEntry
import com.guiaheartopia.app.domain.ContentLibrary
import com.guiaheartopia.app.domain.ContentManifest
import com.guiaheartopia.app.domain.ContentPackage
import com.guiaheartopia.app.domain.ContentSyncResult
import java.io.File
import java.nio.file.Files
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSyncTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://anibalortizdev.github.io/guia-heartopia-contenido/"

    @Test
    fun validManifestParsesAndRejectsUnsafeOrBrokenOnes() {
        val body = """{"id":"trucha","category":"peces","name":"Trucha"}""".toByteArray()
        val manifest = manifest(1, 23, "2026-11-10", mapOf("catalog/creatures.json" to body))
        val parsed = ContentPackage.parseManifest(ContentPackage.encodeManifest(manifest))
        assertEquals(1, parsed?.contentVersion)
        assertEquals(body.size.toLong(), parsed?.files?.single()?.size)
        assertNull(ContentPackage.parseManifest("""{"contentVersion":1,"minAppVersionCode":1,"publishedOn":"2026-11-10","files":[{"path":"../x","size":1,"sha256":"${"a".repeat(64)}"}]}"""))
        assertNull(ContentPackage.parseManifest("""{"contentVersion":1,"minAppVersionCode":1,"publishedOn":"2026-11-10","files":[{"path":"a.json","size":1,"sha256":"nope"}]}"""))
        assertNull(ContentPackage.parseManifest("{"))
    }

    @Test
    fun higherVersionAppliesAndLowerVersionIsIgnored() {
        val base = mapOf("catalog/creatures.json" to creatures("trucha", "Trucha"))
        val library = memory(base, version = 1)
        val newer = mapOf("catalog/creatures.json" to creatures("trucha", "Trucha nueva"))
        val remote = remoteFiles(manifest(2, 23, "2026-12-01", newer), keep = newer)
        assertEquals(ContentSyncResult.APPLIED, sync(library, remote))
        assertEquals("Trucha nueva", creatureName(library.overlayBytes("catalog/creatures.json")!!, "trucha"))
        assertEquals(2, ContentPackage.currentVersion(library))

        val older = remoteFiles(manifest(1, 23, "2026-11-01", base), keep = base)
        assertEquals(ContentSyncResult.OLDER, sync(library, older))
        assertEquals(2, ContentPackage.currentVersion(library))
        assertEquals("Trucha nueva", creatureName(library.overlayBytes("catalog/creatures.json")!!, "trucha"))
    }

    @Test
    fun incompatibleMinAppVersionDoesNotReplaceContent() {
        val base = mapOf("catalog/creatures.json" to creatures("trucha", "Trucha"))
        val library = memory(base, version = 1)
        val other = mapOf("catalog/creatures.json" to creatures("trucha", "Otra"))
        val remote = remoteFiles(manifest(4, 99, "2027-01-01", other), keep = other)
        assertEquals(ContentSyncResult.NEEDS_NEWER_APP, sync(library, remote, appVersionCode = 23))
        assertTrue(library.needsNewerApp())
        assertNull(library.overlayBytes("catalog/creatures.json"))
        assertEquals(1, ContentPackage.currentVersion(library))
    }

    @Test
    fun wrongHashAndIncompleteDownloadKeepThePreviousContent() {
        val base = mapOf("catalog/creatures.json" to creatures("trucha", "Trucha"))
        val library = memory(base, version = 1)
        val good = creatures("atun", "Atún")
        val listed = manifest(2, 23, "2026-12-01", mapOf("catalog/creatures.json" to good))
        val wrong = remoteFiles(listed, keep = mapOf("catalog/creatures.json" to good)).toMutableMap()
        wrong[ContentPackage.fileUrl(baseUrl, "catalog/creatures.json")] = "no-es-el-archivo".toByteArray()
        assertEquals(ContentSyncResult.VERIFY_FAILED, sync(library, wrong))
        assertNull(library.installedManifest())

        val incomplete = remoteFiles(listed, keep = mapOf("catalog/creatures.json" to good)).toMutableMap()
        incomplete.remove(ContentPackage.fileUrl(baseUrl, "catalog/creatures.json"))
        assertEquals(ContentSyncResult.NETWORK_ERROR, sync(library, incomplete))
        assertEquals(1, ContentPackage.currentVersion(library))
        assertNull(library.overlayBytes("catalog/creatures.json"))
    }

    @Test
    fun downloadedCreatureMergesWithTheBaseCatalog() {
        val base = creatures("trucha", "Trucha")
        val extra = creatures("atun", "Atún nuevo")
        val library = memory(mapOf("catalog/heartodex.json" to base), version = 1)
        val remote = remoteFiles(
            manifest(2, 23, "2026-12-02", mapOf("catalog/creatures.json" to extra)),
            keep = mapOf("catalog/creatures.json" to extra),
        )
        assertEquals(ContentSyncResult.APPLIED, sync(library, remote))
        val merged = mergeCreatures(parseCreatures(base), parseCreatures(library.overlayBytes("catalog/creatures.json")!!))
        assertEquals(listOf("trucha", "atun"), merged.map { it.id })
        assertEquals("Atún nuevo", merged.first { it.id == "atun" }.name)
        assertTrue(library.overlayBytes("catalog/heartodex.json") == null)
    }

    @Test
    fun downloadedYearShowsUpBesideTheBaseCalendar() {
        val year2026 = sky("2026-11-12", "despejado")
        val year2027 = sky("2027-03-01", "lluvia")
        val library = memory(mapOf("calendar/clima-2026.json" to year2026), version = 1)
        val remote = remoteFiles(
            manifest(3, 23, "2027-01-02", mapOf("calendar/clima-2027.json" to year2027)),
            keep = mapOf("calendar/clima-2027.json" to year2027),
        )
        assertEquals(ContentSyncResult.APPLIED, sync(library, remote))
        val table = mergeSkyTables(
            listOf(
                SkyCatalog.fromJson(year2026.decodeToString()),
                SkyCatalog.fromJson(library.overlayBytes("calendar/clima-2027.json")!!.decodeToString()),
            ),
        )
        assertNotNull(table.on(java.time.LocalDate.of(2026, 11, 12)))
        assertEquals(
            com.guiaheartopia.app.domain.SkyKind.LLUVIA,
            table.on(java.time.LocalDate.of(2027, 3, 1))?.slots?.single()?.kind,
        )
        assertNull(library.overlayBytes("calendar/clima-2026.json"))
    }

    @Test
    fun bundledManifestMatchesThePackagedFiles() {
        val root = listOf(File("src/main/assets"), File("app/src/main/assets")).first { it.exists() }
        val parsed = ContentPackage.parseManifest(File(root, "content/manifest.json").readText())
        assertNotNull(parsed)
        assertTrue(parsed!!.contentVersion >= 1)
        listOf("catalog/heartodex.json", "catalog/creditos.json", "calendar/clima-2026.json").forEach { path ->
            val entry = parsed.files.first { it.path == path }
            val bytes = File(root, path).readBytes()
            assertEquals(bytes.size.toLong(), entry.size)
            assertEquals(ContentPackage.sha256(bytes), entry.sha256)
        }
    }

    @Test
    fun generatedPackageIsReadByTheApp() {
        val repo = listOf(File(".."), File(".")).first { File(it, "tools/publicar_contenido.py").isFile }
        val output = Files.createTempDirectory("contenido-publicado").toFile()
        val process = ProcessBuilder(
            "python3",
            File(repo, "tools/publicar_contenido.py").absolutePath,
            "--inicial",
            "--fecha",
            "2026-11-10",
            "--salida",
            output.absolutePath,
        )
            .directory(repo)
            .redirectErrorStream(true)
            .start()
        val log = process.inputStream.bufferedReader().readText()
        assertEquals(log, 0, process.waitFor())

        val rootManifest = ContentPackage.parseManifest(File(output, "manifest.json").readText())
        val appManifest = ContentPackage.parseManifest(File(output, "content/manifest.json").readText())
        assertNotNull(rootManifest)
        assertEquals(rootManifest, appManifest)
        assertEquals(1, appManifest!!.contentVersion)
        assertEquals(31, appManifest.minAppVersionCode)
        assertTrue(appManifest.files.any { it.path == "catalog/creatures.json" })
        assertTrue(appManifest.files.any { it.path == "calendar/clima-2026.json" })
        assertTrue(appManifest.files.none { it.path.startsWith("catalog/images/") })

        val served = LinkedHashMap<String, ByteArray>()
        appManifest.files.forEach { file ->
            val packed = File(output, "content/${file.path}")
            val bytes = packed.readBytes()
            assertEquals(file.size, bytes.size.toLong())
            assertEquals(file.sha256, ContentPackage.sha256(bytes))
            served[ContentPackage.fileUrl(baseUrl, file.path)] = bytes
        }
        served[ContentPackage.manifestUrl(baseUrl)] = File(output, "content/manifest.json").readBytes()

        val library = MemoryContentLibrary(
            emptyMap(),
            ContentManifest(contentVersion = 0, minAppVersionCode = 23, publishedOn = "2026-01-01"),
        )
        assertEquals(ContentSyncResult.APPLIED, sync(library, served, appVersionCode = 31))

        val creatures = parseCreatures(library.overlayBytes("catalog/creatures.json")!!)
        assertTrue(creatures.size > 100)
        assertTrue(creatures.map { it.id }.distinct().size == creatures.size)
        assertTrue(creatures.all { it.description.contains("de la guía") })
        val weather = SkyCatalog.fromJson(library.overlayBytes("calendar/clima-2026.json")!!.decodeToString())
        assertNotNull(weather.on(java.time.LocalDate.of(2026, 11, 12)))
        output.deleteRecursively()
    }

    private fun sync(
        library: MemoryContentLibrary,
        remote: Map<String, ByteArray>,
        appVersionCode: Int = 31,
    ): ContentSyncResult = ContentPackage.sync(library, { remote[it] }, appVersionCode, baseUrl).result

    private fun remoteFiles(manifest: ContentManifest, keep: Map<String, ByteArray> = emptyMap()): Map<String, ByteArray> {
        val out = LinkedHashMap<String, ByteArray>()
        out[ContentPackage.manifestUrl(baseUrl)] = ContentPackage.encodeManifest(manifest).toByteArray()
        manifest.files.forEach { file ->
            val bytes = keep[file.path] ?: error(file.path)
            out[ContentPackage.fileUrl(baseUrl, file.path)] = bytes
        }
        return out
    }

    private fun memory(files: Map<String, ByteArray>, version: Int): MemoryContentLibrary {
        val manifest = manifest(version, 23, "2026-11-10", files)
        return MemoryContentLibrary(files, manifest)
    }

    private fun manifest(version: Int, minApp: Int, date: String, files: Map<String, ByteArray>): ContentManifest =
        ContentManifest(
            contentVersion = version,
            minAppVersionCode = minApp,
            publishedOn = date,
            files = files.map { (path, bytes) ->
                ContentFileEntry(path, bytes.size.toLong(), ContentPackage.sha256(bytes))
            },
        )

    private fun creatures(id: String, name: String): ByteArray =
        """{"entries":[{"id":"$id","category":"peces","name":"$name"}]}""".toByteArray()

    private fun sky(date: String, kind: String): ByteArray =
        """{"reviewedOn":"$date","days":[{"date":"$date","slots":[{"start":"08:00","end":"14:00","kind":"$kind","status":"incluido"}]}]}""".toByteArray()

    private fun parseCreatures(raw: ByteArray) =
        json.decodeFromString(com.guiaheartopia.app.data.DexFile.serializer(), raw.decodeToString()).entries

    private fun creatureName(raw: ByteArray, id: String): String =
        parseCreatures(raw).first { it.id == id }.name
}

private class MemoryContentLibrary(
    private val bundledFiles: Map<String, ByteArray>,
    private val bundled: ContentManifest,
) : ContentLibrary {
    var installed: ContentManifest? = null
    val overlay = LinkedHashMap<String, ByteArray>()
    private var needsApp = false

    override fun bundledManifest(): ContentManifest = bundled
    override fun installedManifest(): ContentManifest? = installed
    override fun effectiveBytes(path: String): ByteArray? = overlay[path] ?: bundledFiles[path]
    override fun overlayBytes(path: String): ByteArray? = overlay[path]
    override fun publish(manifest: ContentManifest, overlay: Map<String, ByteArray>) {
        this.overlay.clear()
        this.overlay.putAll(overlay)
        installed = manifest
    }
    override fun needsNewerApp(): Boolean = needsApp
    override fun setNeedsNewerApp(value: Boolean) {
        needsApp = value
    }
}
