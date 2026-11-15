package com.guiaheartopia.app

import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlayerProfile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMigrationTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun oldSnapshotKeepsCapturedCertifiedLevelsAlarmsAndSettings() {
        val raw = """
            {
              "profile": {
                "onboarded": true,
                "name": "Ana",
                "countryId": "CL",
                "zoneId": "America/Santiago",
                "reminders": {"weatherEnabled": true, "pescaEnabled": true, "promptDismissed": true}
              },
              "foundIds": ["pez-1"],
              "certifiedIds": ["pez-1"],
              "sightedIds": ["fauna-1"],
              "hobbies": [{"id": "hob-pesca", "name": "Pesca", "level": 4, "maxLevel": 10, "note": "río"}],
              "hobbiesInitialized": true
            }
        """.trimIndent()
        val loaded = json.decodeFromString(GuideSnapshot.serializer(), raw)
        assertEquals("Ana", loaded.profile.name)
        assertEquals("CL", loaded.profile.countryId)
        assertTrue(loaded.profile.reminders.weatherEnabled)
        assertTrue(loaded.profile.reminders.pescaEnabled)
        assertEquals(listOf("pez-1"), loaded.foundIds)
        assertEquals(listOf("pez-1"), loaded.certifiedIds)
        assertEquals(listOf("fauna-1"), loaded.sightedIds)
        assertEquals(4, loaded.hobbies.single().level)
        assertEquals("río", loaded.hobbies.single().note)
        assertNull(loaded.profile.avatarJpegBase64)
        assertTrue(loaded.profile.seenCodes.isEmpty())
    }

    @Test
    fun backupRoundTripKeepsTheAvatarAndDoesNotDropOlderFields() {
        val snapshot = GuideSnapshot(
            profile = PlayerProfile(
                onboarded = true,
                name = "Nico",
                countryId = "CL",
                zoneId = "America/Santiago",
                avatarJpegBase64 = "aG9sYQ==",
                seenCodes = listOf("ABC"),
            ),
            foundIds = listOf("pez-1"),
            certifiedIds = listOf("pez-1"),
        )
        val imported = BackupCodec.import(BackupCodec.export(snapshot))
        assertTrue(imported is ImportResult.Ok)
        val back = (imported as ImportResult.Ok).snapshot
        assertEquals("aG9sYQ==", back.profile.avatarJpegBase64)
        assertEquals(listOf("ABC"), back.profile.seenCodes)
        assertEquals(listOf("pez-1"), back.foundIds)
        assertEquals(listOf("pez-1"), back.certifiedIds)
    }
}
