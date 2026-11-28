package com.guiaheartopia.app

import com.guiaheartopia.app.ui.AppNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationTest {
    @Test
    fun bottomBarHasFiveTabsWithComunidadAfterCalendarWithoutRoutineOrPixel() {
        val routes = AppNavigation.BottomNavTabs.map { it.route }
        assertEquals(listOf("home", "aficiones", "codigos", "calendar", "comunidad"), routes)
        assertFalse(routes.any { it == "routine" || it == "pixel" })
        assertEquals(5, AppNavigation.BottomNavTabs.size)
        assertTrue(routes.contains("calendar"))
        assertTrue(routes.indexOf("comunidad") > routes.indexOf("calendar"))
        assertEquals("Comunidad", AppNavigation.BottomNavTabs.first { it.route == "comunidad" }.label)
    }
}
