package com.guiaheartopia.app

import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.NavigationPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationPolicyTest {
    @Test
    fun homeTabClearsBackStackWithoutRestoringState() {
        val policy = NavigationPolicy.onBottomTabSelected(AppNavigation.HOME_ROUTE)
        assertTrue(policy.inclusive)
        assertFalse(policy.saveState)
        assertFalse(policy.restoreState)
    }

    @Test
    fun otherTabsSaveStateUnderHome() {
        val policy = NavigationPolicy.onBottomTabSelected("guide")
        assertFalse(policy.inclusive)
        assertTrue(policy.saveState)
        assertTrue(policy.restoreState)
    }
}
