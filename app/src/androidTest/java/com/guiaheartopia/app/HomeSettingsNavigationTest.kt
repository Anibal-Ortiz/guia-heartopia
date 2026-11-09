package com.guiaheartopia.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeSettingsNavigationTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeThenSettingsThenHomeDoesNotRestoreSettings() {
        rule.waitForIdle()
        rule.onNodeWithTag("open_settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("nav_home").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("nav_home").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("open_settings").assertExists()
    }
}
