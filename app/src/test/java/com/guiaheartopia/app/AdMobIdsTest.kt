package com.guiaheartopia.app

import com.guiaheartopia.app.ads.AdMobIds
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdMobIdsTest {
    @Test
    fun placeholdersAreDetected() {
        assertTrue(AdMobIds.isPlaceholder(""))
        assertTrue(AdMobIds.isPlaceholder("ca-app-pub-xxxxxxxxxxxxxxxx/yyyyyyyyyy"))
        assertTrue(AdMobIds.isPlaceholder("ADMOB_APP_ID"))
    }

    @Test
    fun realLookingIdsAreKept() {
        assertFalse(AdMobIds.isPlaceholder("ca-app-pub-3940256099942544/6300978111"))
        assertFalse(AdMobIds.isPlaceholder(AdMobIds.TEST_APP))
    }
}
