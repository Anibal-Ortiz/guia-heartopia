package com.guiaheartopia.app

import com.guiaheartopia.app.ui.AppNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationTest {
    @Test
    fun bottomBarHasFourTabsWithoutRoutineOrPixel() {
        val routes = AppNavigation.BottomNavTabs.map { it.route }
        assertEquals(listOf("home", "guide", "calendar", "hobbies"), routes)
        assertFalse(routes.any { it == "routine" || it == "pixel" })
        assertEquals(4, AppNavigation.BottomNavTabs.size)
        assertTrue(routes.contains("calendar"))
    }
}
