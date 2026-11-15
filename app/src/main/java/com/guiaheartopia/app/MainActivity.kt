package com.guiaheartopia.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.guiaheartopia.app.ads.AdConfig
import com.guiaheartopia.app.ads.DiscreteBanner
import com.guiaheartopia.app.ads.rememberAdsState
import com.guiaheartopia.app.data.AdRewardStore
import com.guiaheartopia.app.data.AdRewardState
import kotlinx.coroutines.launch
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.AndroidContentStore
import com.guiaheartopia.app.data.ContentCheckStore
import com.guiaheartopia.app.data.ContentSignals
import com.guiaheartopia.app.data.ContentUpdateScheduler
import com.guiaheartopia.app.domain.ContentSyncResult
import com.guiaheartopia.app.domain.ContentUpdatePolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.guiaheartopia.app.reminders.ReminderScheduler
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.GuideViewModel
import com.guiaheartopia.app.ui.components.CozySettingsIcon
import com.guiaheartopia.app.ui.components.WikiFanAppIcon
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ui.components.PixelNavBar
import com.guiaheartopia.app.ui.guide.AnimalDetailScreen
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.hobbies.HobbiesScreen
import com.guiaheartopia.app.ui.home.HomeScreen
import com.guiaheartopia.app.ui.legal.LegalScreen
import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.NavigationPolicy
import com.guiaheartopia.app.ui.settings.SettingsScreen
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.GreenSoft
import com.guiaheartopia.app.ui.theme.GuiaTheme
import com.guiaheartopia.app.ui.theme.Ink
import com.guiaheartopia.app.ui.theme.Line
import com.guiaheartopia.app.ui.theme.Muted
import com.guiaheartopia.app.ui.theme.Paper
import com.guiaheartopia.app.ui.theme.PaperSubtle
import com.guiaheartopia.app.ui.weather.WeatherScreen
import com.guiaheartopia.app.ui.welcome.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        val repository = GuideRepository(this)
        setContent {
            val vm: GuideViewModel = viewModel(factory = GuideViewModel.factory(repository, this))
            val ui by vm.ui.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        com.guiaheartopia.app.ads.NavAdStore.completeSession(this@MainActivity)
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        vm.refreshDay()
                        val now = System.currentTimeMillis()
                        val due = ContentUpdatePolicy.isDue(
                            AndroidContentStore.lastAttemptMillis(this@MainActivity),
                            now,
                        )
                        if (due) {
                            lifecycleScope.launch {
                                ContentSignals.setBusy(true)
                                try {
                                    val report = withContext(Dispatchers.IO) {
                                        AndroidContentStore.syncNow(this@MainActivity)
                                    }
                                    ContentCheckStore.record(this@MainActivity, report, System.currentTimeMillis())
                                    if (report.result == ContentSyncResult.APPLIED) {
                                        ContentSignals.note(this@MainActivity)
                                    }
                                } finally {
                                    ContentSignals.setBusy(false)
                                }
                            }
                        }
                        ContentUpdateScheduler.enqueueIfDue(this@MainActivity)
                        ContentSignals.note(this@MainActivity)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            val ads = rememberAdsState(this, ui.ready && ui.profile.onboarded)
            val contentBusy by ContentSignals.busy().collectAsStateWithLifecycle()
            val adRewards by AdRewardStore.observe(this).collectAsStateWithLifecycle(AdRewardState())
            LaunchedEffect(adRewards.suppressAds) {
                ads.setAdFree(adRewards.suppressAds)
            }
            LaunchedEffect(ui.profile) {
                if (ui.ready && ui.profile.onboarded) ReminderScheduler.sync(this@MainActivity, ui.profile)
            }
            GuiaTheme {
                val paper = ImageBitmap.imageResource(R.drawable.papel_textura)
                val paperBrush = remember(paper) {
                    ShaderBrush(ImageShader(paper, TileMode.Repeated, TileMode.Repeated))
                }
                var legal by remember { mutableStateOf<String?>(null) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF7EFDB))
                        .background(paperBrush),
                ) {
                when {
                    !ui.ready -> Box(Modifier.fillMaxSize())
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
                        ads = ads,
                        adRewards = adRewards,
                        adsMode = ads.state.mode,
                        adsStatus = ads.state.status,
                        privacyOptions = ads.state.privacyOptionsRequired,
                        onPrivacy = { legal = "privacidad" },
                        onTerms = { legal = "terminos" },
                        onAdPrivacy = ads::showPrivacyOptions,
                    )
                }
                if (contentBusy) {
                    Text(
                        "Actualizando contenido…",
                        color = Ink,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
}

@Composable
private fun AppScaffold(
    ui: GuideUi,
    vm: GuideViewModel,
    ads: com.guiaheartopia.app.ads.AdsController,
    adRewards: AdRewardState,
    adsMode: com.guiaheartopia.app.ads.AdsMode,
    adsStatus: String,
    privacyOptions: Boolean,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onAdPrivacy: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nav = rememberNavController()
    val showBanner = ads.state.canShowBanner(adRewards.suppressAds)
    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    val watchAdFree: () -> Unit = {
        ads.showRewardedAdFree(
            onReward = { scope.launch { AdRewardStore.grantAdFreeHours(context) } },
            onUnavailable = {
                toast("El video no está listo todavía. Prueba en unos segundos.")
            },
        )
    }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabs = AppNavigation.BottomNavTabs
    val tab = AppNavigation.tabForRoute(route)
    fun navigateBottomTab(dest: String) {
        val policy = NavigationPolicy.onBottomTabSelected(dest)
        fun go() {
            nav.navigate(dest) {
                popUpTo(policy.popUpToRoute) {
                    inclusive = policy.inclusive
                    saveState = policy.saveState
                }
                launchSingleTop = true
                restoreState = policy.restoreState
            }
        }
        val sameTab = tab == dest
        if (adRewards.suppressAds || sameTab) {
            go()
            return
        }
        if (dest == "calendar" && !adRewards.calendarUnlocked && AdConfig.hasRewardedCalendar) {
            ads.showRewardedCalendar(
                onReward = {
                    scope.launch {
                        AdRewardStore.grantCalendarUntilServerDay(context, ui.profile.zoneId)
                        go()
                    }
                },
                onUnavailable = {
                    toast("El video no está listo todavía. Prueba en unos segundos.")
                },
            )
            return
        }
        val allow = com.guiaheartopia.app.domain.NavInterstitial.allow(
            adFree = adRewards.adFreeActive,
            sameTab = sameTab,
            firstLaunch = com.guiaheartopia.app.ads.NavAdStore.isFirstSession(context),
            nowMillis = System.currentTimeMillis(),
            lastShownMillis = com.guiaheartopia.app.ads.NavAdStore.lastShownMillis(context),
        )
        if (!allow) {
            go()
            return
        }
        ads.showInterstitial(
            adFree = adRewards.adFreeActive,
            onDone = { go() },
            onShown = { com.guiaheartopia.app.ads.NavAdStore.markShown(context) },
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .border(width = 1.dp, color = Line)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    WikiFanAppIcon(Modifier.size(36.dp))
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Ink,
                            maxLines = 2,
                        )
                        Text(
                            "Hecha por fans",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .testTag("open_settings")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(PaperSubtle)
                        .border(1.dp, Line, CircleShape)
                        .clickable { nav.navigate("settings") },
                    contentAlignment = Alignment.Center,
                ) {
                    CozySettingsIcon(tint = Ink, size = 18.dp)
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                            .union(WindowInsets.ime)
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (showBanner && tab != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Publicidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                        )
                        DiscreteBanner(adsMode)
                    }
                }
                PixelNavBar(tabs, tab, onSelect = { navigateBottomTab(it) })
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
            composable("home") {
                HomeScreen(
                    ui = ui,
                    onOpen = { dest -> nav.navigate(dest) },
                    onDismissReminderPrompt = vm::dismissReminderPrompt,
                    adFreeActive = adRewards.adFreeActive,
                    onWatchAdFree = watchAdFree,
                    onPickAvatar = vm::saveAvatar,
                    onClearAvatar = vm::clearAvatar,
                )
            }
            composable("aficiones") {
                com.guiaheartopia.app.ui.hobbies.HobbyHubScreen(ui = ui) { dest -> nav.navigate(dest) }
            }
            composable("codigos") {
                com.guiaheartopia.app.ui.codes.CodesScreen(
                    seen = ui.profile.seenCodes,
                    redeemed = ui.redeemedCodes,
                    onSeen = vm::markCodesSeen,
                    onRedeemed = vm::setCodeRedeemed,
                )
            }
            composable("huerto") {
                com.guiaheartopia.app.ui.hobbies.HuertoScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenSiembra = { nav.navigate("huerto/siembra/$it") },
                    onOpenFlor = { nav.navigate("huerto/flor/$it") },
                    onToggleSiembraFound = vm::toggleHuertoFound,
                    onToggleFlorFound = vm::toggleFlorFound,
                    onCancelCrop = vm::cancelCrop,
                    onOpenAboveLevel = { nav.navigate("huerto/pendiente-nivel") },
                )
            }
            composable("huerto/pendiente-nivel") {
                com.guiaheartopia.app.ui.hobbies.HuertoScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenSiembra = { nav.navigate("huerto/siembra/$it") },
                    onOpenFlor = { nav.navigate("huerto/flor/$it") },
                    onToggleSiembraFound = vm::toggleHuertoFound,
                    onToggleFlorFound = vm::toggleFlorFound,
                    onCancelCrop = vm::cancelCrop,
                    onOpenAboveLevel = { nav.navigate("huerto/pendiente-nivel") },
                    onlyAboveLevel = true,
                )
            }
            composable("cocina") {
                com.guiaheartopia.app.ui.hobbies.CocinaScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenReceta = { nav.navigate("cocina/receta/$it") },
                    onToggleFound = vm::toggleRecetaFound,
                    onToggleFavorite = vm::toggleRecetaFavorite,
                    onOpenAboveLevel = { nav.navigate("cocina/pendiente-nivel") },
                )
            }
            composable("cocina/pendiente-nivel") {
                com.guiaheartopia.app.ui.hobbies.CocinaScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenReceta = { nav.navigate("cocina/receta/$it") },
                    onToggleFound = vm::toggleRecetaFound,
                    onToggleFavorite = vm::toggleRecetaFavorite,
                    onOpenAboveLevel = { nav.navigate("cocina/pendiente-nivel") },
                    onlyAboveLevel = true,
                )
            }
            composable("guide") {
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable("guide/disponibles-ahora") {
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    guideLaunch = com.guiaheartopia.app.domain.GuideLaunch(onlyNow = true),
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable("guide/pendientes") {
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    guideLaunch = com.guiaheartopia.app.domain.GuideLaunch(registered = "no"),
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable(
                "guide/evento/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { entry ->
                val eventId = entry.arguments?.getString("eventId").orEmpty()
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    guideLaunch = com.guiaheartopia.app.domain.GuideLaunch(eventFocus = eventId),
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable("guide/pendiente-nivel") {
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    onlyAboveLevel = true,
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable("calendar") {
                WeatherScreen(
                    ui.profile.countryId,
                    ui.profile.zoneId,
                    ui.profile.regionKey,
                    onRegion = { country, zone, region ->
                        vm.updateRegion(country, zone, regionKey = region)
                    },
                    onOpenEventCreatures = { eventId -> nav.navigate("guide/evento/$eventId") },
                )
            }
            composable("hobbies") {
                HobbiesScreen(ui.hobbies, vm::setHobby, onBack = { nav.popBackStack() })
            }
            composable(
                "huerto/siembra/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val context = androidx.compose.ui.platform.LocalContext.current
                val revision = com.guiaheartopia.app.ui.contentRevision()
                val bundle = androidx.compose.runtime.remember(revision) {
                    com.guiaheartopia.app.data.AficionesCatalog.load(context)
                }
                val siembra = bundle.siembras.firstOrNull { it.id == id }
                if (siembra == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    val crop = ui.activeCrops.firstOrNull { it.siembraId == siembra.id }
                    com.guiaheartopia.app.ui.hobbies.SiembraDetailScreen(
                        entry = siembra,
                        recetas = bundle.recetas,
                        found = siembra.id in ui.huertoFound,
                        activeCrop = crop,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleHuertoFound(siembra.id) },
                        onPlant = { vm.plantSiembra(siembra.id, siembra.nombre, siembra.tiempoCrecimientoMinutos) },
                        onOpenReceta = { nav.navigate("cocina/receta/$it") },
                    )
                }
            }
            composable(
                "huerto/flor/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val context = androidx.compose.ui.platform.LocalContext.current
                val revision = com.guiaheartopia.app.ui.contentRevision()
                val flor = androidx.compose.runtime.remember(revision) {
                    com.guiaheartopia.app.data.AficionesCatalog.load(context).flores.firstOrNull { it.id == id }
                }
                if (flor == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    com.guiaheartopia.app.ui.hobbies.FlorDetailScreen(
                        entry = flor,
                        found = flor.id in ui.florFound,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleFlorFound(flor.id) },
                    )
                }
            }
            composable(
                "cocina/receta/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val context = androidx.compose.ui.platform.LocalContext.current
                val revision = com.guiaheartopia.app.ui.contentRevision()
                val bundle = androidx.compose.runtime.remember(revision) {
                    com.guiaheartopia.app.data.AficionesCatalog.load(context)
                }
                val receta = bundle.recetas.firstOrNull { it.id == id }
                if (receta == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    com.guiaheartopia.app.ui.hobbies.RecetaDetailScreen(
                        entry = receta,
                        siembras = bundle.siembras,
                        recetas = bundle.recetas,
                        found = receta.id in ui.recetaFound,
                        favorite = receta.id in ui.recetaFavorites,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleRecetaFound(receta.id) },
                        onToggleFavorite = { vm.toggleRecetaFavorite(receta.id) },
                        onOpenSiembra = { nav.navigate("huerto/siembra/$it") },
                        onOpenReceta = { nav.navigate("cocina/receta/$it") },
                    )
                }
            }
            composable(
                "dex/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id")
                val context = androidx.compose.ui.platform.LocalContext.current
                val revision = com.guiaheartopia.app.ui.contentRevision()
                val item = androidx.compose.runtime.remember(id, revision) {
                    com.guiaheartopia.app.data.HeartodexCatalog.load(context).firstOrNull { it.id == id }
                }
                if (item == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    val zone = com.guiaheartopia.app.domain.DexSchedule.zoneOf(ui.profile.zoneId)
                    val sky = androidx.compose.runtime.remember(revision) {
                        com.guiaheartopia.app.data.SkyCatalog.load(context)
                    }
                    val nextWindow = androidx.compose.runtime.remember(item.id, revision) {
                        com.guiaheartopia.app.domain.CreatureAppearing.nextWindow(
                            item.schedule,
                            item.weather,
                            sky,
                            zone,
                        )
                    }
                    val nextLabel = nextWindow?.let {
                        com.guiaheartopia.app.domain.CreatureAppearing.userLabel(it, zone)
                    }
                    val alarmScheduled = ui.creatureWindowAlarms.any { it.creatureId == item.id }
                    com.guiaheartopia.app.ui.guide.DexDetailScreen(
                        entry = item,
                        zoneId = ui.profile.zoneId,
                        found = item.id in ui.found,
                        certified = item.id in ui.certified,
                        onToggle = { vm.toggleFound(item.id) },
                        onToggleCertified = { vm.toggleCertified(item.id, item.category) },
                        onBack = { nav.popBackStack() },
                        windowAlarmScheduled = alarmScheduled,
                        nextWindowLabel = nextLabel,
                        onScheduleWindow = { vm.scheduleCreatureWindowAlarm(item.id, item.name) },
                        onCancelWindow = { vm.cancelCreatureWindowAlarm(item.id) },
                    )
                }
            }
            composable("settings/recordatorios") {
                SettingsScreen(
                    profile = ui.profile,
                    adsMode = adsMode,
                    adsStatus = adsStatus,
                    privacyOptions = privacyOptions,
                    onSave = vm::saveProfile,
                    onRegion = { country, zone, origin, region ->
                        vm.updateRegion(country, zone, origin, region)
                    },
                    onPrivacy = onPrivacy,
                    onTerms = onTerms,
                    onAdPrivacy = onAdPrivacy,
                    onReminders = vm::saveReminders,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup,
                    onPhotoCredits = { nav.navigate("photo-credits") },
                    scrollToReminders = true,
                    adFreeUntilMillis = adRewards.adFreeUntilMillis,
                    onWatchAdFree = watchAdFree,
                )
            }
            composable("settings") {
                SettingsScreen(
                    profile = ui.profile,
                    adsMode = adsMode,
                    adsStatus = adsStatus,
                    privacyOptions = privacyOptions,
                    onSave = vm::saveProfile,
                    onRegion = { country, zone, origin, region ->
                        vm.updateRegion(country, zone, origin, region)
                    },
                    onPrivacy = onPrivacy,
                    onTerms = onTerms,
                    onAdPrivacy = onAdPrivacy,
                    onReminders = vm::saveReminders,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup,
                    onPhotoCredits = { nav.navigate("photo-credits") },
                    adFreeUntilMillis = adRewards.adFreeUntilMillis,
                    onWatchAdFree = watchAdFree,
                )
            }
            composable("photo-credits") {
                com.guiaheartopia.app.ui.settings.PhotoCreditsScreen(onBack = { nav.popBackStack() })
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
