package com.guiaheartopia.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.ui.theme.GuiaTheme
import com.guiaheartopia.app.ui.welcome.WelcomeScreen
import org.junit.Rule
import org.junit.Test

class WelcomeScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun welcomeShowsIndependentGuideNotice() {
        rule.setContent {
            GuiaTheme {
                WelcomeScreen(
                    initial = PlayerProfile(countryId = "CL", zoneId = "America/Santiago"),
                    onContinue = {},
                    onPrivacy = {},
                    onTerms = {},
                )
            }
        }
        rule.onNodeWithText("Guía independiente", substring = true).assertIsDisplayed()
        rule.onNodeWithText("no está afiliada", substring = true).assertIsDisplayed()
    }
}
