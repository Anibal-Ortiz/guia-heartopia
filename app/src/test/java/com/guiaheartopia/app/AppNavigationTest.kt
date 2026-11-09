package com.guiaheartopia.app

import com.guiaheartopia.app.ui.AppNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppNavigationTest {
    @Test
    fun bottomBarHasThreeTabsWithoutRoutineOrPixel() {
        val routes = AppNavigation.BottomNavTabs.map { it.route }
        assertEquals(listOf("home", "guide", "hobbies"), routes)
        assertFalse(routes.any { it == "routine" || it == "pixel" })
        assertEquals(3, AppNavigation.BottomNavTabs.size)
    }
}
