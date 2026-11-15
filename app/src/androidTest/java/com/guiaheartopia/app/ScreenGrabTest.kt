package com.guiaheartopia.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.guiaheartopia.app.data.AvatarStore
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.ImageZoom
import com.guiaheartopia.app.ui.codes.CodesScreen
import com.guiaheartopia.app.ui.hobbies.CocinaScreen
import com.guiaheartopia.app.ui.hobbies.HuertoScreen
import com.guiaheartopia.app.ui.hobbies.HobbyHubScreen
import com.guiaheartopia.app.ui.hobbies.FlorDetailScreen
import com.guiaheartopia.app.ui.hobbies.RecetaDetailScreen
import com.guiaheartopia.app.ui.hobbies.SiembraDetailScreen
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
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
            hobbies = com.guiaheartopia.app.domain.HobbyCatalog.fixed.map { hobby ->
                hobby.copy(level = hobby.maxLevel)
            },
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

    @Test
    fun captureRequestedLayouts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val ui = sampleUi()
        val photo = android.graphics.Bitmap.createBitmap(80, 60, android.graphics.Bitmap.Config.ARGB_8888)
        photo.eraseColor(android.graphics.Color.parseColor("#C4A15A"))
        val encoded = android.util.Base64.encodeToString(AvatarStore.centerCropJpeg(photo), android.util.Base64.NO_WRAP)
        photo.recycle()
        writeOverlay(context, "codes/codigos.json", "[]\n")
        val step = mutableIntStateOf(0)
        val guideTab = mutableIntStateOf(0)
        rule.setContent {
            GuiaTheme {
                val base = LocalDensity.current
                val font = if (step.intValue == 11) 1.3f else base.fontScale
                CompositionLocalProvider(LocalDensity provides Density(base.density, fontScale = font)) {
                    Box(Modifier.fillMaxSize().background(Cream)) {
                        androidx.compose.runtime.key(step.intValue) {
                            when (step.intValue) {
                                0, 1 -> HomeScreen(ui, onOpen = {})
                                2 -> HomeScreen(ui.copy(profile = ui.profile.copy(avatarJpegBase64 = encoded)), onOpen = {})
                                3 -> HomeScreen(ui, onOpen = {})
                                4 -> HobbyHubScreen(ui) {}
                                5 -> HuertoScreen(
                                    ui,
                                    onBack = {},
                                    onOpenSiembra = {},
                                    onOpenFlor = {},
                                    onToggleSiembraFound = {},
                                    onToggleFlorFound = {},
                                    onCancelCrop = {},
                                    onOpenAboveLevel = {},
                                )
                                6 -> CocinaScreen(
                                    ui,
                                    onBack = {},
                                    onOpenReceta = {},
                                    onToggleFound = {},
                                    onToggleFavorite = {},
                                    onOpenAboveLevel = {},
                                )
                                7, 8 -> CodesScreen(seen = emptyList()) {}
                                9 -> WeatherScreen("CL", "America/Santiago") { _, _, _ -> }
                                10 -> androidx.compose.runtime.key(guideTab.intValue) {
                                    GuideScreen(ui, {}, {}, {}, { _, _ -> }, { _, _, _, _ -> }, initialTab = guideTab.intValue)
                                }
                                else -> HomeScreen(ui, onOpen = {})
                            }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
        save("inicio-banner")
        rule.onNodeWithTag("coleccion").performScrollTo()
        rule.waitForIdle()
        save("tu-coleccion")

        go(step, 2)
        save("avatar-con-foto")
        go(step, 3)
        save("avatar-sin-foto")
        go(step, 4)
        save("aficiones")
        writeAficionesOverlay(context)
        go(step, 5)
        save("huerto")
        go(step, 6)
        save("cocina")
        go(step, 7)
        save("codigos-vacios")
        writeOverlay(
            context,
            "codes/codigos.json",
            """[{"code":"MUESTRA","recompensa":"Ejemplo de recompensa","fechaVencimiento":"2026-12-31"}]""",
        )
        go(step, 8)
        save("codigos-con-datos")
        writeOverlay(
            context,
            "calendar/eventos.json",
            """
            {"zonaFuente":"Etc/GMT+5","servidor":"America","eventos":[
              {"id":"halloween-noche-grimkin","tipo":"temporada","nombre":"Noche grimkin",
               "inicio":"2026-10-30T06:00","fin":"2026-12-31T05:59"}
            ]}
            """.trimIndent(),
        )
        go(step, 9)
        rule.onNodeWithText("Noche grimkin").performScrollTo()
        rule.waitForIdle()
        save("calendario-eventos")
        go(step, 10)
        openFirstCreature("tarjeta-pez")
        rule.onAllNodes(hasContentDescription(ImageZoom.enlargedDescription(""), substring = true))[0].performClick()
        rule.waitForIdle()
        writeBitmap("dialogo-imagen", rule.onNodeWithTag("zoom-dialog").captureToImage().asAndroidBitmap())
        rule.onNodeWithText("Cerrar").performClick()
        rule.waitForIdle()
        go(guideTab, 1)
        openFirstCreature("tarjeta-ave")
        go(guideTab, 2)
        openFirstCreature("tarjeta-insecto")
        go(step, 11)
        save("inicio-360-fuente-grande")
    }

    private fun go(step: androidx.compose.runtime.MutableIntState, value: Int) {
        rule.runOnIdle { step.intValue = value }
        rule.waitForIdle()
    }

    private fun sampleUi() = GuideUi(
        ready = true,
        profile = PlayerProfile(onboarded = true, name = "Nico", countryId = "CL", zoneId = "America/Santiago"),
        hobbies = com.guiaheartopia.app.domain.HobbyCatalog.fixed.map { hobby ->
            hobby.copy(level = hobby.maxLevel)
        },
        greeting = "Buenas noches, Nico",
        todayLabel = "viernes 6 de noviembre",
        animals = com.guiaheartopia.app.data.SeedCatalog.animals,
    )

    private fun openFirstCreature(name: String) {
        val marks = rule.onAllNodes(hasContentDescription("Marcar como capturado"))
        if (marks.fetchSemanticsNodes().isNotEmpty()) {
            marks[0].performScrollTo()
            rule.waitForIdle()
        }
        save(name)
    }

    private fun writeOverlay(context: android.content.Context, relative: String, body: String) {
        val file = java.io.File(context.filesDir, "content/live/files/$relative")
        file.parentFile?.mkdirs()
        file.writeText(body)
    }

    private fun writeAficionesOverlay(context: android.content.Context) {
        writeOverlay(
            context,
            "huerto/siembras.json",
            """
            [{"id":"tomate","nombre":"Tomate","nivelSiembra":2,"precioSemilla":30,
              "fuenteSemilla":"Tienda del pueblo","tiempoCrecimientoMinutos":90,
              "precioVentaEstrellas":[10,20,30,40,50],"categoria":"Verduras"}]
            """.trimIndent(),
        )
        writeOverlay(
            context,
            "huerto/flores.json",
            """[{"id":"rosa","nombre":"Rosa","nivelSiembra":1,
              "variantes":[{"color":"Roja","origen":"tienda","precioVenta":12}]}]""",
        )
        writeOverlay(
            context,
            "cocina/recetas.json",
            """
            [{"id":"ensalada","nombre":"Ensalada","nivelCocina":1,
              "energiaEstrellas":[5,6,7,8,9],"precioVentaEstrellas":[11,12,13,14,15],
              "ingredientes":[{"idSiembra":"tomate","cantidad":2}],
              "categoria":"Ensaladas"}]
            """.trimIndent(),
        )
    }

    @Test
    fun captureAficiones105() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        writeAficionesOverlay(context)
        val planted = System.currentTimeMillis() - 30_000L
        val plantedUi = sampleUi().copy(
            huertoFound = setOf("tomate"),
            florFound = setOf("rosa"),
            recetaFound = emptySet(),
            recetaFavorites = setOf("ensalada"),
            activeCrops = listOf(
                ActiveCrop("c1", "tomate", "Tomate", planted, planted + 90 * 60_000L),
            ),
        )
        val lowLevelUi = sampleUi().copy(
            hobbies = com.guiaheartopia.app.domain.HobbyCatalog.fixed.map { hobby ->
                when (hobby.id) {
                    "hob-siembra", "hob-cocina" -> hobby.copy(level = 0)
                    else -> hobby
                }
            },
            huertoFound = setOf("tomate"),
            activeCrops = plantedUi.activeCrops,
        )
        val siembra = com.guiaheartopia.app.domain.AficionesContent.parseSiembras(
            java.io.File(context.filesDir, "content/live/files/huerto/siembras.json").readText(),
        ).single()
        val flores = com.guiaheartopia.app.domain.AficionesContent.parseFlores(
            java.io.File(context.filesDir, "content/live/files/huerto/flores.json").readText(),
        )
        val recetas = com.guiaheartopia.app.domain.AficionesContent.parseRecetas(
            java.io.File(context.filesDir, "content/live/files/cocina/recetas.json").readText(),
        )
        val step = mutableIntStateOf(0)
        rule.setContent {
            GuiaTheme {
                Box(Modifier.fillMaxSize().background(Cream)) {
                    androidx.compose.runtime.key(step.intValue) {
                        when (step.intValue) {
                            0 -> HobbyHubScreen(lowLevelUi) {}
                            1 -> HuertoScreen(
                                plantedUi,
                                onBack = {},
                                onOpenSiembra = {},
                                onOpenFlor = {},
                                onToggleSiembraFound = {},
                                onToggleFlorFound = {},
                                onCancelCrop = {},
                                onOpenAboveLevel = {},
                            )
                            2 -> HuertoScreen(
                                plantedUi,
                                onBack = {},
                                onOpenSiembra = {},
                                onOpenFlor = {},
                                onToggleSiembraFound = {},
                                onToggleFlorFound = {},
                                onCancelCrop = {},
                                onOpenAboveLevel = {},
                                initialTab = 1,
                            )
                            3 -> CocinaScreen(
                                plantedUi,
                                onBack = {},
                                onOpenReceta = {},
                                onToggleFound = {},
                                onToggleFavorite = {},
                                onOpenAboveLevel = {},
                            )
                            4 -> SiembraDetailScreen(
                                siembra, recetas, true, plantedUi.activeCrops.single(),
                                {}, {}, {}, {},
                            )
                            5 -> FlorDetailScreen(flores.single(), true, {}, {})
                            6 -> RecetaDetailScreen(
                                recetas.single(), listOf(siembra), false, true,
                                {}, {}, {}, {},
                            )
                            else -> HuertoScreen(
                                lowLevelUi,
                                onBack = {},
                                onOpenSiembra = {},
                                onOpenFlor = {},
                                onToggleSiembraFound = {},
                                onToggleFlorFound = {},
                                onCancelCrop = {},
                                onOpenAboveLevel = {},
                                onlyAboveLevel = true,
                            )
                        }
                    }
                }
            }
        }
        val shots = listOf(
            0 to ("105-aficiones-nivel-pendiente" to "Aficiones"),
            1 to ("105-huerto-siembras-temporizador" to "Cultivos en curso"),
            2 to ("105-huerto-flores" to "Rosa"),
            3 to ("105-cocina" to "Ensalada"),
            4 to ("105-ficha-siembra" to "Tomate"),
            5 to ("105-ficha-flor" to "Rosa"),
            6 to ("105-ficha-receta" to "Ensalada"),
            7 to ("105-huerto-filtro-nivel" to "más nivel"),
        )
        shots.forEach { (value, shot) ->
            go(step, value)
            saveWhenVisible(shot.first, shot.second)
        }
    }

    private fun saveRoot(name: String, index: Int) {
        val roots = rule.onAllNodes(isRoot())
        val node = if (roots.fetchSemanticsNodes().size > index) roots[index] else rule.onRoot()
        val bitmap = node.captureToImage().asAndroidBitmap()
        writeBitmap(name, bitmap)
    }

    @Composable
    private fun ScreenAt(index: Int, ui: GuideUi) {
        androidx.compose.runtime.key(index) {
            when (index) {
                0 -> WelcomeScreen(PlayerProfile(countryId = "CL", zoneId = "America/Santiago"), {}, {}, {})
                1 -> HomeScreen(ui, onOpen = {})
                2 -> GuideScreen(ui, {}, {}, {}, { _, _ -> }, { _, _, _, _ -> }, initialTab = 0)
                3 -> WeatherScreen("CL", "America/Santiago") { _, _, _ -> }
                else -> HobbiesScreen(ui.hobbies, { _, _, _, _ -> })
            }
        }
    }

    private fun save(name: String) {
        rule.waitForIdle()
        Thread.sleep(800)
        writeBitmap(name, rule.onRoot().captureToImage().asAndroidBitmap())
    }

    private fun saveWhenVisible(name: String, label: String) {
        rule.waitUntil(timeoutMillis = 15_000) {
            rule.onAllNodes(hasText(label, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
        save(name)
    }

    private fun writeBitmap(name: String, bitmap: android.graphics.Bitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(
            File(context.filesDir, "screens"),
            context.externalCacheDir?.let { File(it, "screens") },
            context.getExternalFilesDir(null)?.let { File(it, "screens") },
        )
            .filterNotNull()
            .forEach { dir ->
                dir.mkdirs()
                File(dir, "$name.png").outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                }
            }
    }
}
