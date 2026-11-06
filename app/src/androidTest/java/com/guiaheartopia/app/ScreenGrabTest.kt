package com.guiaheartopia.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.RoutineState
import com.guiaheartopia.app.domain.RoutineTask
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.hobbies.HobbiesScreen
import com.guiaheartopia.app.ui.home.HomeScreen
import com.guiaheartopia.app.ui.map.MapScreen
import com.guiaheartopia.app.ui.pixel.PixelArtScreen
import com.guiaheartopia.app.ui.routine.RoutineScreen
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
            routine = RoutineState(
                tasks = listOf(
                    RoutineTask("a", "Revisar la ficha del panda"),
                    RoutineTask("b", "Anotar un hobby"),
                ),
                completedIds = listOf("a"),
                initialized = true,
            ),
            routineFraction = 0.5f,
            animals = com.guiaheartopia.app.data.SeedCatalog.animals,
            collectibles = com.guiaheartopia.app.data.SeedCatalog.collectibles,
            hobbies = listOf(Hobby("hob-cocina", "Cocina", level = 2, maxLevel = 10)),
            greeting = "Buenas noches, Nico",
            todayLabel = "viernes 6 de noviembre",
        )
        val index = mutableIntStateOf(0)
        val names = listOf(
            "01-bienvenida",
            "02-inicio",
            "03-rutina",
            "04-guia",
            "05-calendario",
            "06-pixel",
            "07-hobbies",
            "08-mapa",
        )
        rule.setContent {
            GuiaTheme {
                ScreenAt(index.intValue, ui)
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
        when (index) {
            0 -> WelcomeScreen(PlayerProfile(countryId = "CL", zoneId = "America/Santiago"), {}, {}, {})
            1 -> HomeScreen(ui) {}
            2 -> RoutineScreen(ui, {}, {}, {}, {})
            3 -> GuideScreen(ui, {}, {}, {}, {}, { _, _ -> }, { _, _, _, _ -> })
            4 -> WeatherScreen("CL", "America/Santiago", "origen-ejemplo") { _, _, _ -> }
            5 -> PixelArtScreen(32, 32)
            6 -> HobbiesScreen(ui.hobbies, { _, _, _, _ -> }, {}, {})
            else -> MapScreen("America/Santiago")
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
