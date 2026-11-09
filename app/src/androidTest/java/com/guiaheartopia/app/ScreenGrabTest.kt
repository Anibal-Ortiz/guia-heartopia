package com.guiaheartopia.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.PixelNavBar
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.hobbies.HobbiesScreen
import com.guiaheartopia.app.ui.home.HomeScreen
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.GuiaTheme
import com.guiaheartopia.app.ui.weather.WeatherScreen
import com.guiaheartopia.app.ui.welcome.WelcomeScreen
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenGrabTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun captureMainScreens() {
        val ui = GuideUi(
            ready = true,
            profile = PlayerProfile(onboarded = true, name = "Nico", countryId = "CL", zoneId = "America/Santiago"),
            hobbies = listOf(Hobby("hob-cocina", "Cocina", level = 2, maxLevel = 10)),
            greeting = "Buenas noches, Nico",
            todayLabel = "viernes 6 de noviembre",
        )
        val index = mutableIntStateOf(0)
        val names = listOf("01-bienvenida", "02-inicio", "03-guia", "04-calendario", "05-hobbies")
        rule.setContent {
            GuiaTheme {
                val current = when (index.intValue) {
                    2 -> "guide"
                    3 -> "calendar"
                    4 -> "hobbies"
                    else -> "home"
                }
                Column(Modifier.fillMaxSize().background(Cream)) {
                    Box(Modifier.weight(1f)) { ScreenAt(index.intValue, ui) }
                    PixelNavBar(AppNavigation.BottomNavTabs, current) {}
                }
            }
        }
        names.forEachIndexed { i, name ->
            rule.runOnIdle { index.intValue = i }
            rule.waitForIdle()
            save(name)
        }
    }

    @Composable
    private fun ScreenAt(index: Int, ui: GuideUi) {
        androidx.compose.runtime.key(index) {
            when (index) {
                0 -> WelcomeScreen(PlayerProfile(countryId = "CL", zoneId = "America/Santiago"), {}, {}, {})
                1 -> HomeScreen(ui) {}
                2 -> GuideScreen(ui, {}, {}, {}, {}, { _, _, _, _ -> }, initialTab = 1)
                3 -> WeatherScreen("CL", "America/Santiago") { _, _ -> }
                else -> HobbiesScreen(ui.hobbies, { _, _, _, _ -> }, {}, {})
            }
        }
    }

    private fun save(name: String) {
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.filesDir, "screens")
        dir.mkdirs()
        File(dir, "$name.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }
}
