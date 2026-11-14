package com.guiaheartopia.app

import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.CatalogMarks
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlayerProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogMarksTest {
    private val stored = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun saveAndLoadKeepsCapturedAndCertified() {
        val original = GuideSnapshot(
            profile = PlayerProfile(onboarded = true, name = "Ana"),
            foundIds = listOf("pez-1", "ave-1"),
            certifiedIds = listOf("ave-1"),
            sightedIds = listOf("fauna-1"),
        )
        val raw = stored.encodeToString(GuideSnapshot.serializer(), original)
        val loaded = stored.decodeFromString(GuideSnapshot.serializer(), raw)
        assertEquals(listOf("pez-1", "ave-1"), loaded.foundIds)
        assertEquals(listOf("ave-1"), loaded.certifiedIds)
        assertEquals(listOf("fauna-1"), loaded.sightedIds)
    }

    @Test
    fun oldStoredSnapshotWithoutCertifiedKeepsTheRest() {
        val raw = """{"profile":{"onboarded":true,"name":"Ana"},"foundIds":["pez-1"],"sightedIds":["fauna-1"]}"""
        val loaded = stored.decodeFromString(GuideSnapshot.serializer(), raw)
        assertEquals(listOf("pez-1"), loaded.foundIds)
        assertEquals(emptyList<String>(), loaded.certifiedIds)
        assertEquals(listOf("fauna-1"), loaded.sightedIds)
    }

    @Test
    fun certifyingCapturesAndUncapturingClearsTheCertificate() {
        val empty = CatalogMarks.Marks(emptySet(), emptySet())
        val certified = CatalogMarks.toggleCertified(empty, "pez", "Peces")
        assertEquals(setOf("pez"), certified.found)
        assertEquals(setOf("pez"), certified.certified)

        val stillCaptured = CatalogMarks.toggleCertified(certified, "pez", "Peces")
        assertEquals(setOf("pez"), stillCaptured.found)
        assertTrue(stillCaptured.certified.isEmpty())

        val cleared = CatalogMarks.toggleFound(certified, "pez")
        assertTrue(cleared.found.isEmpty())
        assertTrue(cleared.certified.isEmpty())

        val bird = CatalogMarks.toggleCertified(empty, "ave", "Aves")
        assertEquals(setOf("ave"), bird.found)
        assertEquals(setOf("ave"), bird.certified)
    }

    @Test
    fun faunaCannotBeCertified() {
        val next = CatalogMarks.toggleCertified(CatalogMarks.Marks(emptySet(), emptySet()), "zorro", "Fauna")
        assertTrue(next.found.isEmpty())
        assertTrue(next.certified.isEmpty())
        assertTrue(!CatalogMarks.certifiable("Fauna"))
    }

    @Test
    fun summaryCountsCertifiedApartFromCaptured() {
        val fish = CatalogMarks.tally("Peces", listOf("a", "b", "c"), setOf("a", "b"), setOf("b"))
        val birds = CatalogMarks.tally("Pájaros", listOf("d"), emptySet(), emptySet())
        val insects = CatalogMarks.tally("Insectos", listOf("e", "f"), setOf("e", "f"), setOf("e", "f"))
        assertEquals("Peces: 2/3 capturados · 1 certificados", fish.summary())
        assertEquals("Pájaros: 0/1 capturados · 0 certificados", birds.summary())
        assertEquals("Insectos: 2/2 capturados · 2 certificados", insects.summary())
    }

    @Test
    fun oldBackupWithoutCertifiedImportsAsNotCertified() {
        val raw = """
            {"format":1,"app":"guia-heartopia","snapshot":{
              "profile":{"onboarded":true,"name":"Nico","countryId":"CL","zoneId":"America/Santiago"},
              "foundIds":["fish-blue-sea-angel"],
              "sightedIds":["animal-1"]
            }}
        """.trimIndent()
        val imported = BackupCodec.import(raw)
        assertTrue(imported is ImportResult.Ok)
        val snapshot = (imported as ImportResult.Ok).snapshot
        assertEquals("Nico", snapshot.profile.name)
        assertEquals(listOf("fish-blue-sea-angel"), snapshot.foundIds)
        assertEquals(listOf("animal-1"), snapshot.sightedIds)
        assertEquals(emptyList<String>(), snapshot.certifiedIds)
    }

    @Test
    fun exportRoundTripKeepsCertified() {
        val original = GuideSnapshot(
            profile = PlayerProfile(onboarded = true, name = "Nico"),
            foundIds = listOf("fish-1", "bird-1"),
            certifiedIds = listOf("fish-1"),
        )
        val imported = BackupCodec.import(BackupCodec.export(original))
        assertTrue(imported is ImportResult.Ok)
        val snapshot = (imported as ImportResult.Ok).snapshot
        assertEquals(listOf("fish-1", "bird-1"), snapshot.foundIds)
        assertEquals(listOf("fish-1"), snapshot.certifiedIds)
    }
}
