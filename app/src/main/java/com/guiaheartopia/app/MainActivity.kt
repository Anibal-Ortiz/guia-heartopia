package com.guiaheartopia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guiaheartopia.app.ads.DiscreteBanner
import com.guiaheartopia.app.ads.rememberAdsState
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.GuideViewModel
import androidx.compose.runtime.LaunchedEffect
import com.guiaheartopia.app.reminders.ReminderScheduler
import com.guiaheartopia.app.ui.components.NavIcon
import com.guiaheartopia.app.ui.components.NavItem
import com.guiaheartopia.app.ui.components.PixelNavBar
import com.guiaheartopia.app.ui.map.MapScreen
import com.guiaheartopia.app.ui.guide.AnimalDetailScreen
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.hobbies.HobbiesScreen
import com.guiaheartopia.app.ui.home.HomeScreen
import com.guiaheartopia.app.ui.legal.LegalScreen
import com.guiaheartopia.app.ui.pixel.PixelArtScreen
import com.guiaheartopia.app.ui.routine.RoutineScreen
import com.guiaheartopia.app.ui.settings.SettingsScreen
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.GuiaTheme
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.weather.WeatherScreen
import com.guiaheartopia.app.ui.welcome.WelcomeScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = GuideRepository(this)
        setContent {
            val vm: GuideViewModel = viewModel(factory = GuideViewModel.factory(repository))
            val ui by vm.ui.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) vm.refreshDay()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            val ads = rememberAdsState(this, ui.ready && ui.profile.onboarded)
            LaunchedEffect(ui.profile) {
                if (ui.ready && ui.profile.onboarded) ReminderScheduler.sync(this@MainActivity, ui.profile)
            }
            GuiaTheme {
                var legal by remember { mutableStateOf<String?>(null) }
                when {
                    !ui.ready -> Box(Modifier.fillMaxSize().background(Cream))
                    legal != null -> Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        LegalScreen(
                            asset = if (legal == "terminos") "legal/terminos.md" else "legal/privacidad.md",
                            title = if (legal == "terminos") "Términos" else "Privacidad",
                            onBack = { legal = null },
                        )
                    }
                    !ui.profile.onboarded -> Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        WelcomeScreen(
                            initial = ui.profile,
                            onContinue = vm::saveProfile,
                            onPrivacy = { legal = "privacidad" },
                            onTerms = { legal = "terminos" },
                        )
                    }
                    else -> AppScaffold(
                        ui = ui,
                        vm = vm,
                        showBanner = ads.state.canShowBanner,
                        adsMode = ads.state.mode,
                        adsStatus = ads.state.status,
                        privacyOptions = ads.state.privacyOptionsRequired,
                        onPrivacy = { legal = "privacidad" },
                        onTerms = { legal = "terminos" },
                        onAdPrivacy = ads::showPrivacyOptions,
                    )
                }
            }
        }
    }
}

private val Tabs = listOf(
    NavItem("home", "Inicio", NavIcon.HOME),
    NavItem("routine", "Rutina", NavIcon.ROUTINE),
    NavItem("guide", "Guía", NavIcon.GUIDE),
    NavItem("pixel", "Píxel", NavIcon.PIXEL),
    NavItem("hobbies", "Aficiones", NavIcon.HOBBIES),
)

@Composable
private fun AppScaffold(
    ui: GuideUi,
    vm: GuideViewModel,
    showBanner: Boolean,
    adsMode: com.guiaheartopia.app.ads.AdsMode,
    adsStatus: String,
    privacyOptions: Boolean,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onAdPrivacy: () -> Unit,
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tab = Tabs.firstOrNull { route?.startsWith(it.route) == true }?.route
    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                Modifier
                    .background(Cream)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("Guía Heartopia", color = Ink)
                TextButton(onClick = { nav.navigate("settings") }) { Text("Ajustes") }
            }
        },
        bottomBar = {
            Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                if (showBanner) DiscreteBanner(adsMode)
                PixelNavBar(Tabs, tab) { dest ->
                    nav.navigate(dest) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
            composable("home") { HomeScreen(ui) { dest -> nav.navigate(dest) } }
            composable("routine") {
                RoutineScreen(ui, vm::toggleTask, vm::restoreRoutine, vm::addTask, vm::removeTask)
            }
            composable("guide") {
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenWeather = { nav.navigate("weather") },
                    onOpenMap = { nav.navigate("map") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onAddAnimal = vm::addAnimal,
                )
            }
            composable("pixel") { PixelArtScreen(ui.profile.pixelCols, ui.profile.pixelRows) }
            composable("hobbies") {
                HobbiesScreen(ui.hobbies, vm::setHobby, vm::addHobby, vm::removeHobby)
            }
            composable("map") { MapScreen(ui.profile.zoneId) }
            composable(
                "dex/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val context = androidx.compose.ui.platform.LocalContext.current
                val item = androidx.compose.runtime.remember(id) {
                    com.guiaheartopia.app.data.HeartodexCatalog.load(context).firstOrNull { it.id == id }
                }
                if (item == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    com.guiaheartopia.app.ui.guide.DexDetailScreen(
                        entry = item,
                        zoneId = ui.profile.zoneId,
                        found = item.id in ui.found,
                        onToggle = { vm.toggleFound(item.id) },
                        onBack = { nav.popBackStack() },
                    )
                }
            }
            composable("weather") {
                WeatherScreen(ui.profile.countryId, ui.profile.zoneId, ui.profile.weatherOriginId, vm::updateRegion)
            }
            composable("settings") {
                SettingsScreen(
                    profile = ui.profile,
                    adsMode = adsMode,
                    adsStatus = adsStatus,
                    privacyOptions = privacyOptions,
                    onSave = vm::saveProfile,
                    onRegion = vm::updateRegion,
                    onPrivacy = onPrivacy,
                    onTerms = onTerms,
                    onAdPrivacy = onAdPrivacy,
                    onReminders = vm::saveReminders,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup,
                )
            }
            composable(
                "animal/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val animal = ui.animals.firstOrNull { it.id == id }
                if (animal == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    AnimalDetailScreen(
                        animal = animal,
                        annotation = ui.annotations[animal.id] ?: com.guiaheartopia.app.domain.AnimalAnnotation(),
                        sighted = animal.id in ui.sighted,
                        onToggleSighted = { vm.toggleSighted(animal.id) },
                        onSave = { vm.saveAnnotation(animal.id, it) },
                        onBack = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}
