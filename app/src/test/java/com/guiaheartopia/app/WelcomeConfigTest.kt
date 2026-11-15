package com.guiaheartopia.app

import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.PlayerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WelcomeConfigTest {
    @Test
    fun backupWithLegacyPlaceNameStillImports() {
        val original = GuideSnapshot(
            profile = PlayerProfile(onboarded = true, name = "Ana", placeName = "Casa del lago"),
        )
        val imported = BackupCodec.import(BackupCodec.export(original))
        assertTrue(imported is ImportResult.Ok)
        val profile = (imported as ImportResult.Ok).snapshot.profile
        assertEquals("Casa del lago", profile.placeName)
        assertEquals("Ana", profile.name)
    }
}
