package com.guiaheartopia.app

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("open_settings").fetchSemanticsNodes().isNotEmpty() ||
                rule.onAllNodesWithText("Tu nombre").fetchSemanticsNodes().isNotEmpty()
        }
        if (rule.onAllNodesWithTag("open_settings").fetchSemanticsNodes().isEmpty()) {
            rule.onNodeWithText("Tu nombre").performTextInput("Nico")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            rule.waitForIdle()
            repeat(3) {
                rule.onNode(hasText("Continuar →") and hasClickAction()).performScrollTo().performClick()
                rule.waitForIdle()
            }
            rule.onNode(hasText("Entrar →") and hasClickAction()).performScrollTo().performClick()
            rule.waitUntil(8_000) {
                rule.onAllNodesWithTag("open_settings").fetchSemanticsNodes().isNotEmpty()
            }
        }
        rule.onNodeWithTag("open_settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("nav_home").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("nav_home").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("open_settings").assertExists()
    }
}
