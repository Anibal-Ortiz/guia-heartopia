package com.guiaheartopia.app

import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.NavigationPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationPolicyTest {
    @Test
    fun homeTabOpensMiDiaAndClearsTheStack() {
        val policy = NavigationPolicy.onBottomTabSelected(AppNavigation.HOME_ROUTE)
        assertEquals(AppNavigation.HOME_ROUTE, policy.popUpToRoute)
        assertEquals(true, policy.inclusive)
        assertFalse(policy.saveState)
        assertFalse(policy.restoreState)
    }

    @Test
    fun everyOtherTabOpensItsRootWithoutRestoringAnInnerScreen() {
        listOf("aficiones", "codigos", "calendar").forEach { route ->
            val policy = NavigationPolicy.onBottomTabSelected(route)
            assertEquals(route, policy.popUpToRoute)
            assertTrue(policy.inclusive)
            assertFalse(policy.saveState)
            assertFalse(policy.restoreState)
        }
    }
}
