package com.guiaheartopia.app

import com.guiaheartopia.app.domain.HobbyContent
import com.guiaheartopia.app.domain.LevelFilter
import com.guiaheartopia.app.domain.NavInterstitial
import com.guiaheartopia.app.domain.RedeemCodes
import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.hobbies.HobbyDoors
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelAndNavTest {
    @Test
    fun levelOptionsComeFromTheCatalogMaximum() {
        val max = LevelFilter.maximum(listOf(1, null, 14, 3))
        assertEquals(14, max)
        assertEquals(listOf(null) + (0..14).toList(), LevelFilter.options(max!!))
        assertEquals(null, LevelFilter.maximum(listOf(null, null)))
    }

    @Test
    fun hobbyHubOpensNatureGardenKitchenAndLevels() {
        assertEquals(listOf("guide", "huerto", "cocina", "hobbies"), HobbyDoors.map { it.route })
        assertEquals("aficiones", AppNavigation.tabForRoute("guide"))
        assertEquals("aficiones", AppNavigation.tabForRoute("dex/pez-1"))
        assertEquals("aficiones", AppNavigation.tabForRoute("huerto"))
        assertEquals("codigos", AppNavigation.tabForRoute("codigos"))
        assertEquals("calendar", AppNavigation.tabForRoute("calendar"))
        assertEquals("home", AppNavigation.tabForRoute("home"))
    }

    @Test
    fun interstitialRespectsAdFreeSameTabFirstLaunchAndGap() {
        val now = 1_000_000L
        assertFalse(NavInterstitial.allow(adFree = true, sameTab = false, firstLaunch = false, nowMillis = now, lastShownMillis = 0))
        assertFalse(NavInterstitial.allow(adFree = false, sameTab = true, firstLaunch = false, nowMillis = now, lastShownMillis = 0))
        assertFalse(NavInterstitial.allow(adFree = false, sameTab = false, firstLaunch = true, nowMillis = now, lastShownMillis = 0))
        assertFalse(
            NavInterstitial.allow(
                adFree = false,
                sameTab = false,
                firstLaunch = false,
                nowMillis = now,
                lastShownMillis = now - NavInterstitial.MIN_GAP_MILLIS + 1,
            ),
        )
        assertTrue(
            NavInterstitial.allow(
                adFree = false,
                sameTab = false,
                firstLaunch = false,
                nowMillis = now,
                lastShownMillis = now - NavInterstitial.MIN_GAP_MILLIS,
            ),
        )
    }

    @Test
    fun emptyCodesAndMissingHobbyFilesStayEmpty() {
        assertTrue(RedeemCodes.parse("[]").isEmpty())
        assertTrue(RedeemCodes.parse(null).isEmpty())
        val today = LocalDate.of(2026, 11, 14)
        val raw = """[{"code":"VIEJO","recompensa":"Nada","fechaVencimiento":"2026-01-01"},{"code":"NUEVO","recompensa":"Una semilla","fechaInicio":"2026-11-01"}]"""
        val codes = RedeemCodes.parse(raw)
        assertEquals(listOf("NUEVO"), RedeemCodes.active(codes, today).map { it.code })
        assertEquals(listOf("VIEJO"), RedeemCodes.expired(codes, today).map { it.code })
        assertEquals(listOf("NUEVO"), RedeemCodes.unseen(RedeemCodes.active(codes, today), emptySet()).map { it.code })
        assertTrue(HobbyContent.parse(null).isEmpty())
        assertTrue(HobbyContent.parse("[]").isEmpty())
    }
}
