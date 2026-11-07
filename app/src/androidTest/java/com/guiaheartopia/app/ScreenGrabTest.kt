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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.NavItem
import com.guiaheartopia.app.ui.components.PixelNavBar
import com.guiaheartopia.app.ui.guide.DexDetailScreen
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.theme.Cream
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
            "04-guia-peces",
            "04b-guia-pajaros",
            "04c-guia-insectos",
            "05-calendario",
            "06-pixel",
            "07-hobbies",
            "08-mapa",
            "09-detalle-pez",
        )
        rule.setContent {
            GuiaTheme {
                val current = when (index.intValue) {
                    2 -> "routine"
                    3, 4, 5, 10 -> "guide"
                    7 -> "pixel"
                    8 -> "hobbies"
                    else -> "home"
                }
                Column(Modifier.fillMaxSize().background(Cream)) {
                    Box(Modifier.weight(1f)) { ScreenAt(index.intValue, ui) }
                    PixelNavBar(
                        listOf(
                            NavItem("home", "Inicio", NavIcon.HOME),
                            NavItem("routine", "Rutina", NavIcon.ROUTINE),
                            NavItem("guide", "Guía", NavIcon.GUIDE),
                            NavItem("pixel", "Pixel", NavIcon.PIXEL),
                            NavItem("hobbies", "Hobbies", NavIcon.HOBBIES),
                        ),
                        current,
                    ) {}
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
            2 -> RoutineScreen(ui, {}, {}, {}, {})
            3 -> GuideScreen(ui, {}, {}, {}, {}, {}, { _, _, _, _ -> }, initialTab = 1)
            4 -> GuideScreen(ui, {}, {}, {}, {}, {}, { _, _, _, _ -> }, initialTab = 2)
            5 -> GuideScreen(ui, {}, {}, {}, {}, {}, { _, _, _, _ -> }, initialTab = 3)
            6 -> WeatherScreen("CL", "America/Santiago", "origen-ejemplo") { _, _, _ -> }
            7 -> PixelArtScreen(32, 32)
            8 -> HobbiesScreen(ui.hobbies, { _, _, _, _ -> }, {}, {})
            9 -> MapScreen("America/Santiago")
            else -> {
                val entry = com.guiaheartopia.app.data.HeartodexCatalog.load(
                    androidx.compose.ui.platform.LocalContext.current,
                ).first { it.category == "Peces" }
                DexDetailScreen(entry, "America/Santiago", false, {}, {})
            }
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
