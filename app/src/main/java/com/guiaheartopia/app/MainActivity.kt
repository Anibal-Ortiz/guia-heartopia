package com.guiaheartopia.app

import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.guiaheartopia.app.data.LicensePreferences
import com.guiaheartopia.app.data.LicenseState
import com.guiaheartopia.app.data.PolarLicenseRepository
import kotlinx.coroutines.launch
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.AndroidContentStore
import com.guiaheartopia.app.data.ContentCheckStore
import com.guiaheartopia.app.data.ContentSignals
import com.guiaheartopia.app.data.ThemeMode
import com.guiaheartopia.app.data.ThemeModeStore
import com.guiaheartopia.app.domain.ContentDownloadProgress
import com.guiaheartopia.app.domain.ContentSyncMessages
import com.guiaheartopia.app.domain.ContentSyncResult
import com.guiaheartopia.app.domain.ContentUpdatePolicy
import com.guiaheartopia.app.domain.checkOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.guiaheartopia.app.reminders.ReminderScheduler
import com.guiaheartopia.app.ui.GuideUi
import com.guiaheartopia.app.ui.GuideViewModel
import com.guiaheartopia.app.ui.components.AdFreeStatusChip
import com.guiaheartopia.app.ui.components.AndroidRobotIcon
import com.guiaheartopia.app.ui.components.CozyMoonIcon
import com.guiaheartopia.app.ui.components.CozySettingsIcon
import com.guiaheartopia.app.ui.components.CozySunIcon
import com.guiaheartopia.app.ui.components.UpdateHeaderState
import com.guiaheartopia.app.ui.components.UpdateStatusIcon
import com.guiaheartopia.app.ui.components.WikiFanAppIcon
import com.guiaheartopia.app.R
import com.guiaheartopia.app.ui.components.PixelNavBar
import com.guiaheartopia.app.ui.feedback.FeedbackMenu
import com.guiaheartopia.app.ui.guide.AnimalDetailScreen
import com.guiaheartopia.app.ui.guide.GuideScreen
import com.guiaheartopia.app.ui.hobbies.HobbiesScreen
import com.guiaheartopia.app.ui.home.HomeScreen
import com.guiaheartopia.app.ui.legal.openPrivacyPolicy
import com.guiaheartopia.app.ui.legal.openTerms
import com.guiaheartopia.app.ui.AppNavigation
import com.guiaheartopia.app.ui.NavigationPolicy
import com.guiaheartopia.app.ui.settings.SettingsScreen
import com.guiaheartopia.app.ui.updates.UpdateAvailableDialog
import com.guiaheartopia.app.ui.updates.PlayUpdateResult
import com.guiaheartopia.app.ui.updates.checkPlayUpdate
import com.guiaheartopia.app.ui.updates.openPlayStore
import com.guiaheartopia.app.ui.theme.Cream
import com.guiaheartopia.app.ui.theme.Green
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
        val darkSystem = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES
        val statusStyle = if (darkSystem) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        val navStyle = if (darkSystem) {
            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        } else {
            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = statusStyle, navigationBarStyle = navStyle)
        val repository = GuideRepository(this)
        setContent {
            val vm: GuideViewModel = viewModel(factory = GuideViewModel.factory(repository, this))
            val ui by vm.ui.collectAsStateWithLifecycle()
            val licensePreferences = remember { LicensePreferences.get(this@MainActivity) }
            val licenseState by licensePreferences.stateFlow.collectAsStateWithLifecycle(LicenseState())
            val licenseRepository = remember { PolarLicenseRepository.get(this@MainActivity) }
            LaunchedEffect(Unit) { licenseRepository.validateLicenseSilently() }
            val themeMode by ThemeModeStore.observe(this@MainActivity)
                .collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }
            LaunchedEffect(darkTheme) {
                val statusStyle = if (darkTheme) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                val navStyle = if (darkTheme) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = statusStyle, navigationBarStyle = navStyle)
            }
            val lifecycleOwner = LocalLifecycleOwner.current
            // La cabecera muestra una única secuencia: contenido y Play se combinan
            // para no encadenar dos checks distintos antes de volver al logo.
            val contentBusy by ContentSignals.busy().collectAsStateWithLifecycle()
            val contentProgress by ContentSignals.progress().collectAsStateWithLifecycle()
            val contentFailed by ContentSignals.failed().collectAsStateWithLifecycle()
            var contentCheckFinished by remember { mutableStateOf(false) }
            var contentWasBusy by remember { mutableStateOf(false) }
            LaunchedEffect(contentBusy) {
                if (contentBusy) {
                    contentWasBusy = true
                    contentCheckFinished = false
                } else if (contentWasBusy) {
                    contentWasBusy = false
                    contentCheckFinished = true
                }
            }
            fun checkContentUpdates(force: Boolean = false) {
                if (ContentSignals.busy().value) return
                if (!force) {
                    val last = AndroidContentStore.lastAttemptMillis(this@MainActivity)
                    if (!ContentUpdatePolicy.isDue(last, System.currentTimeMillis())) {
                        contentCheckFinished = true
                        return
                    }
                }
                contentCheckFinished = false
                lifecycleScope.launch {
                    ContentSignals.setBusy(true)
                    try {
                        val report = withContext(Dispatchers.IO) {
                            AndroidContentStore.syncNow(this@MainActivity) {
                                ContentSignals.setProgress(it)
                            }
                        }
                        ContentCheckStore.record(this@MainActivity, report, System.currentTimeMillis())
                        if (report.result == ContentSyncResult.APPLIED) {
                            ContentSignals.note(this@MainActivity)
                        }
                        if (report.result.checkOk) {
                            ContentSignals.noteCheckOk()
                        }
                        if (report.result == ContentSyncResult.APPLIED ||
                            report.result == ContentSyncResult.UNCHANGED
                        ) {
                            ContentSignals.emitSyncResult(report.result)
                        }
                        ContentSignals.setFailed(!report.result.checkOk)
                        if (!report.result.checkOk) {
                            Toast.makeText(
                                this@MainActivity,
                                ContentSyncMessages.message(report),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    } catch (_: Exception) {
                        ContentSignals.setFailed(true)
                    } finally {
                        ContentSignals.setBusy(false)
                        ContentSignals.setProgress(null)
                        contentCheckFinished = true
                    }
                }
            }
            LaunchedEffect(Unit) { checkContentUpdates(force = true) }
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP) {
                        com.guiaheartopia.app.ads.NavAdStore.completeSession(this@MainActivity)
                    }
                    if (event == Lifecycle.Event.ON_RESUME) {
                        vm.refreshDay()
                        // Buscar actualizaciones en cada apertura de la app (sin intervalo de 12 h).
                        checkContentUpdates(force = true)
                        ContentSignals.note(this@MainActivity)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
            val ads = rememberAdsState(
                this,
                ui.ready && ui.profile.onboarded && licenseState.loaded && !licenseState.isPro,
            )
            val adRewards by AdRewardStore.observe(this).collectAsStateWithLifecycle(AdRewardState())
            // Un solo despertar al vencer el beneficio: mantiene banner y navegación
            // sincronizados sin reevaluar el reloj en toda la pantalla cada 30 s.
            var adFreeClock by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(adRewards.adFreeUntilMillis) {
                adFreeClock = System.currentTimeMillis()
                val wait = adRewards.adFreeUntilMillis - adFreeClock
                if (wait > 0L) {
                    delay(wait + 1_000L)
                    adFreeClock = System.currentTimeMillis()
                }
            }
            val suppressAds = !adRewards.loaded ||
                adRewards.adFreeUntilMillis > adFreeClock ||
                licenseState.isPro
            val adFreeActive = adRewards.loaded && adRewards.adFreeUntilMillis > adFreeClock
            LaunchedEffect(licenseState.isPro) {
                ads.setPro(licenseState.isPro)
            }
            LaunchedEffect(suppressAds) {
                ads.setAdFree(suppressAds)
            }
            LaunchedEffect(ui.profile) {
                if (ui.ready && ui.profile.onboarded) ReminderScheduler.sync(this@MainActivity, ui.profile)
            }
            val currentProfile = androidx.compose.runtime.rememberUpdatedState(ui.profile)
            LaunchedEffect(Unit) {
                com.guiaheartopia.app.data.ContentSignals.syncResults().collect { result ->
                    if (result == ContentSyncResult.APPLIED || result == ContentSyncResult.UNCHANGED) {
                        ReminderScheduler.sync(this@MainActivity, currentProfile.value)
                    }
                }
            }
            // La cabecera refleja solo la actualización del repositorio de contenido.
            // Play se consulta aparte y, si hay versión nueva, se avisa con su diálogo;
            // un fallo de Play (APK instalado a mano) no pinta el reintentar.
            var playResult by remember { mutableStateOf<PlayUpdateResult?>(null) }
            var playAttempt by remember { mutableStateOf(0) }
            fun runUpdateCheck() {
                playAttempt += 1
                val attempt = playAttempt
                playResult = null
                checkPlayUpdate(this@MainActivity) { result ->
                    if (attempt == playAttempt) {
                        playResult = result
                    }
                }
            }
            LaunchedEffect(Unit) { runUpdateCheck() }
            val updateChecking = contentBusy || contentProgress != null || !contentCheckFinished
            val updateFailed = !updateChecking && contentFailed
            var showUpdateOk by remember { mutableStateOf(false) }
            LaunchedEffect(updateChecking, updateFailed) {
                if (!updateChecking && !updateFailed) {
                    showUpdateOk = true
                    delay(1_500)
                    showUpdateOk = false
                } else {
                    showUpdateOk = false
                }
            }
            val headerUpdateState = when {
                updateChecking -> UpdateHeaderState.CHECKING
                updateFailed -> UpdateHeaderState.FAILED
                showUpdateOk -> UpdateHeaderState.OK
                else -> null
            }
            GuiaTheme(darkTheme = darkTheme) {
                val darkUi = darkTheme
                // Misma textura de papel en ambos temas; la versión oscura conserva el
                // grano original con el tono dark, en vez de un fondo plano de un color.
                val paper = ImageBitmap.imageResource(
                    if (darkUi) R.drawable.papel_textura_dark else R.drawable.papel_textura,
                )
                val paperBrush = remember(paper) {
                    ShaderBrush(ImageShader(paper, TileMode.Repeated, TileMode.Repeated))
                }
                // Política y términos se abren en la web: se actualizan sin publicar un APK.
                val openPrivacy: () -> Unit = { openPrivacyPolicy(this@MainActivity) }
                val openTerms: () -> Unit = { openTerms(this@MainActivity) }
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .background(paperBrush),
                ) {
                when {
                    !ui.ready -> Box(Modifier.fillMaxSize())
                    !ui.profile.onboarded -> Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                        WelcomeScreen(
                            initial = ui.profile,
                            onContinue = vm::completeOnboarding,
                            onPrivacy = openPrivacy,
                            onTerms = openTerms,
                        )
                    }
                    else -> AppScaffold(
                        ui = ui,
                        vm = vm,
                        ads = ads,
                        adRewards = adRewards,
                        adFreeActive = adFreeActive,
                        isPro = licenseState.isPro,
                        suppressAds = suppressAds,
                        adsMode = ads.state.mode,
                        adsStatus = ads.state.status,
                        privacyOptions = ads.state.privacyOptionsRequired,
                        onPrivacy = openPrivacy,
                        onTerms = openTerms,
                        onAdPrivacy = ads::showPrivacyOptions,
                        themeMode = themeMode,
                        onThemeMode = { mode ->
                            lifecycleScope.launch { ThemeModeStore.set(this@MainActivity, mode) }
                        },
                        updateStatus = headerUpdateState,
                        updatePercent = contentProgress?.let { contentPercent(it) },
                        onRetryUpdate = {
                            checkContentUpdates(force = true)
                        },
                    )
                }

                if (playResult == PlayUpdateResult.AVAILABLE && ui.ready && ui.profile.onboarded) {
                    UpdateAvailableDialog(
                        onDismiss = { playResult = null },
                        onUpdate = {
                            openPlayStore(this@MainActivity)
                            playResult = null
                        },
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
    adFreeActive: Boolean,
    isPro: Boolean,
    suppressAds: Boolean,
    adsMode: com.guiaheartopia.app.ads.AdsMode,
    adsStatus: String,
    privacyOptions: Boolean,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onAdPrivacy: () -> Unit,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    updateStatus: UpdateHeaderState?,
    updatePercent: Int?,
    onRetryUpdate: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nav = rememberNavController()
    val showBanner = ads.state.canShowBanner(suppressAds)
    var themeFlash by remember { mutableStateOf(false) }
    LaunchedEffect(themeFlash) {
        if (themeFlash) {
            delay(700)
            themeFlash = false
        }
    }
    fun cycleTheme() {
        val next = when (themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        val label = when (next) {
            ThemeMode.SYSTEM -> "Modo del sistema"
            ThemeMode.LIGHT -> "Modo claro"
            ThemeMode.DARK -> "Modo oscuro"
        }
        themeFlash = true
        Toast.makeText(context, "$label activado", Toast.LENGTH_SHORT).show()
        onThemeMode(next)
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    val watchAdFree: () -> Unit = {
        // Comprobación exacta en el momento del toque: el contador del chip se
        // actualiza cada 30 s y no debe permitir un video que dé menos de 3 h.
        val remaining = AdRewardStore.remainingMillis(adRewards.adFreeUntilMillis)
        if (remaining > AdRewardStore.AD_FREE_ADD_MORE_THRESHOLD_MILLIS) {
            toast("Máximo 12h alcanzado. Podrás sumar otro video cuando queden 9 h o menos.")
        } else {
            ads.showRewardedAdFree(
                onReward = { scope.launch { AdRewardStore.grantAdFreeHours(context) } },
                onUnavailable = {
                    toast("No hay otro video listo aún, intenta en unos segundos")
                    // La carga puede estar en vuelo o haber fallado (no-fill): se
                    // reintenta en segundo plano para que el siguiente «+3h» esté listo.
                    ads.preloadRewardedAdFree()
                    scope.launch {
                        delay(5_000L)
                        ads.preloadRewardedAdFree()
                    }
                },
            )
        }
    }
    // Recompensado de alarmas: mismo esquema que «sin ads», pero con su propio
    // unit y su propio beneficio. No se ve afectado por adFreeActive.
    val watchAlarmAd: () -> Unit = {
        val remaining = AdRewardStore.remainingMillis(adRewards.cropAlarmUntilMillis)
        if (remaining > AdRewardStore.CROP_ALARM_ADD_MORE_THRESHOLD_MILLIS) {
            toast("Máximo 12h alcanzado. Podrás sumar otro video cuando queden 9 h o menos.")
        } else {
            ads.showRewardedAlarm(
                onReward = { scope.launch { AdRewardStore.grantCropAlarmHours(context) } },
                onUnavailable = {
                    toast("No hay otro video listo aún, intenta en unos segundos")
                    ads.preloadRewardedAlarm()
                    scope.launch {
                        delay(5_000L)
                        ads.preloadRewardedAlarm()
                    }
                },
            )
        }
    }
    val plantIfUnlocked: (String, String, Int) -> Unit = { id, nombre, minutos ->
        if (adRewards.cropAlarmUnlocked || isPro) vm.plantSiembra(id, nombre, minutos)
    }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val tabs = AppNavigation.BottomNavTabs
    val tab = AppNavigation.tabForRoute(route)

    // La fila del chip «Sin ads» se pliega al bajar por cualquier pantalla y
    // vuelve a mostrarse solo al llegar arriba. La cabecera WikiFan no se mueve.
    val adFreeBarVisible = remember { mutableStateOf(true) }
    val adFreeBarScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f && consumed.y == 0f) {
                    // Desplazamiento hacia abajo sin contenido que consumir:
                    // la lista ya está en el tope superior.
                    adFreeBarVisible.value = true
                } else if (consumed.y < 0f) {
                    adFreeBarVisible.value = false
                }
                return Offset.Zero
            }
        }
    }
    // Cada pantalla nueva empieza en el tope: el chip debe estar visible.
    LaunchedEffect(route) { adFreeBarVisible.value = true }

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
        if (suppressAds || sameTab) {
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
            adFree = adFreeActive,
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
            adFree = adFreeActive,
            onDone = { go() },
            onShown = { com.guiaheartopia.app.ads.NavAdStore.markShown(context) },
        )
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                            .union(WindowInsets.displayCutout)
                            .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .border(width = 1.dp, color = Line),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                            if (updateStatus == null) {
                                WikiFanAppIcon(Modifier.size(36.dp))
                            } else {
                                val canRetry = updateStatus == UpdateHeaderState.FAILED
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable(enabled = canRetry) { onRetryUpdate() }
                                        .semantics {
                                            contentDescription = when (updateStatus) {
                                                UpdateHeaderState.CHECKING -> "Comprobando actualizaciones"
                                                UpdateHeaderState.OK -> "Sin actualizaciones pendientes"
                                                UpdateHeaderState.FAILED -> "No se pudo comprobar. Toca para reintentar"
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    UpdateStatusIcon(updateStatus, percent = updatePercent)
                                }
                            }
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .testTag("theme_toggle")
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (themeFlash) GreenSoft else PaperSubtle)
                                .border(1.dp, if (themeFlash) Green else Line, CircleShape)
                                .clickable { cycleTheme() }
                                .semantics { contentDescription = "Cambiar tema" },
                            contentAlignment = Alignment.Center,
                        ) {
                            val themeIcon = when (themeMode) {
                                ThemeMode.SYSTEM -> com.guiaheartopia.app.R.drawable.ic_theme_system
                                ThemeMode.LIGHT -> com.guiaheartopia.app.R.drawable.ic_theme_sun
                                ThemeMode.DARK -> com.guiaheartopia.app.R.drawable.ic_theme_moon
                            }
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(themeIcon),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        FeedbackMenu()
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
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(com.guiaheartopia.app.R.drawable.ic_settings),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = adRewards.loaded && adFreeBarVisible.value && !isPro,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    ) {
                        AdFreeStatusChip(
                            untilMillis = adRewards.adFreeUntilMillis,
                            onWatchAdFree = watchAdFree,
                        )
                    }
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
                .nestedScroll(adFreeBarScroll)
                .padding(padding)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        ) {
            composable("home") {
                HomeScreen(
                    ui = ui,
                    onOpen = { dest -> nav.navigate(dest) },
                    onDismissReminderPrompt = vm::dismissReminderPrompt,
                    adFreeActive = adFreeActive || isPro,
                    onWatchAdFree = watchAdFree,
                    onPickAvatar = vm::saveAvatar,
                    onClearAvatar = vm::clearAvatar,
                    onCancelCrop = vm::cancelCrop,
                )
            }
            composable("aficiones") {
                com.guiaheartopia.app.ui.hobbies.HobbyHubScreen { dest -> nav.navigate(dest) }
            }
            composable("codigos") {
                com.guiaheartopia.app.ui.codes.CodesScreen(
                    seen = ui.profile.seenCodes,
                    redeemed = ui.redeemedCodes,
                    onSeen = vm::markCodesSeen,
                    onRedeemed = vm::setCodeRedeemed,
                    zoneId = ui.profile.zoneId,
                )
            }
            composable("huerto") {
                com.guiaheartopia.app.ui.hobbies.HuertoScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenSiembra = { nav.navigate("huerto/siembra/$it") },
                    onOpenFlor = { nav.navigate("huerto/flor/$it") },
                    onToggleSiembraFound = vm::toggleHuertoFound,
                    onToggleSiembraCertified = vm::toggleHuertoCertified,
                    onToggleFlorFound = vm::toggleFlorFound,
                    onToggleFlorCertified = vm::toggleFlorCertified,
                    onCancelCrop = vm::cancelCrop,
                    onPlant = plantIfUnlocked,
                    cropAlarmUnlocked = adRewards.cropAlarmUnlocked || isPro,
                    cropAlarmUntilMillis = adRewards.cropAlarmUntilMillis,
                    onWatchAlarmAd = watchAlarmAd,
                    proActive = isPro,
                )
            }
            composable("huerto/flores") {
                com.guiaheartopia.app.ui.hobbies.HuertoScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenSiembra = { nav.navigate("huerto/siembra/$it") },
                    onOpenFlor = { nav.navigate("huerto/flor/$it") },
                    onToggleSiembraFound = vm::toggleHuertoFound,
                    onToggleSiembraCertified = vm::toggleHuertoCertified,
                    onToggleFlorFound = vm::toggleFlorFound,
                    onToggleFlorCertified = vm::toggleFlorCertified,
                    onCancelCrop = vm::cancelCrop,
                    onPlant = plantIfUnlocked,
                    cropAlarmUnlocked = adRewards.cropAlarmUnlocked || isPro,
                    cropAlarmUntilMillis = adRewards.cropAlarmUntilMillis,
                    onWatchAlarmAd = watchAlarmAd,
                    proActive = isPro,
                    initialTab = 1,
                )
            }
            composable("cocina") {
                com.guiaheartopia.app.ui.hobbies.CocinaScreen(
                    ui = ui,
                    onBack = { nav.popBackStack() },
                    onOpenReceta = { nav.navigate("cocina/receta/$it") },
                    onToggleFound = vm::toggleRecetaFound,
                    onToggleCertified = vm::toggleRecetaCertified,
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
                val context = androidx.compose.ui.platform.LocalContext.current
                val eventTab = androidx.compose.runtime.remember(eventId) {
                    com.guiaheartopia.app.domain.GuideEventNav.dexTabIndexFromContext(context, eventId)
                }
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    initialTab = eventTab,
                    guideLaunch = com.guiaheartopia.app.domain.GuideLaunch(eventFocus = eventId),
                    onSaveDexFilters = vm::saveDexFilters,
                )
            }
            composable(
                "guide/categoria/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType }),
            ) { entry ->
                val category = entry.arguments?.getString("category").orEmpty()
                val tab = com.guiaheartopia.app.domain.GuideSections.dexCategories
                    .indexOfFirst { it.equals(category, true) }
                    .coerceAtLeast(0)
                GuideScreen(
                    ui = ui,
                    onOpenAnimal = { nav.navigate("animal/$it") },
                    onOpenDex = { nav.navigate("dex/$it") },
                    onToggleFound = vm::toggleFound,
                    onToggleCertified = vm::toggleCertified,
                    onAddAnimal = vm::addAnimal,
                    onBack = { nav.popBackStack() },
                    initialTab = tab,
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
                        certified = siembra.id in ui.huertoCertified,
                        activeCrop = crop,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleHuertoFound(siembra.id) },
                        onToggleCertified = {
                            vm.toggleHuertoCertified(siembra.id, com.guiaheartopia.app.domain.AficionesMarks.certifiableSiembra(siembra))
                        },
                        alarmUnlocked = adRewards.cropAlarmUnlocked || isPro,
                        onWatchAlarmAd = watchAlarmAd,
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
                        certified = flor.id in ui.florCertified,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleFlorFound(flor.id) },
                        onToggleCertified = {
                            vm.toggleFlorCertified(flor.id, com.guiaheartopia.app.domain.AficionesMarks.certifiableFlor(flor))
                        },
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
                        certified = receta.id in ui.recetaCertified,
                        onBack = { nav.popBackStack() },
                        onToggleFound = { vm.toggleRecetaFound(receta.id) },
                        onToggleCertified = {
                            vm.toggleRecetaCertified(receta.id, com.guiaheartopia.app.domain.AficionesMarks.certifiableReceta(receta))
                        },
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
                    com.guiaheartopia.app.data.CreatureCatalog.load(context).firstOrNull { it.id == id }
                }
                if (item == null) {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
                } else {
                    val zone = com.guiaheartopia.app.domain.DexSchedule.zoneOf(ui.profile.zoneId)
                    val sky = androidx.compose.runtime.remember(revision) {
                        com.guiaheartopia.app.data.SkyCatalog.load(context, ui.profile.zoneId)
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
                    onRegion = { country, zone, origin, region ->
                        vm.updateRegion(country, zone, origin, region)
                    },
                    onPrivacy = onPrivacy,
                    onTerms = onTerms,
                    onAdPrivacy = onAdPrivacy,
                    onReminders = vm::saveReminders,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup,
                    isPro = isPro,
                    scrollToReminders = true,
                    adFreeUntilMillis = adRewards.adFreeUntilMillis,
                    onWatchAdFree = watchAdFree,
                    onRetryAds = ads::retryConsent,
                )
            }
            composable("settings") {
                SettingsScreen(
                    profile = ui.profile,
                    adsMode = adsMode,
                    adsStatus = adsStatus,
                    privacyOptions = privacyOptions,
                    onRegion = { country, zone, origin, region ->
                        vm.updateRegion(country, zone, origin, region)
                    },
                    onPrivacy = onPrivacy,
                    onTerms = onTerms,
                    onAdPrivacy = onAdPrivacy,
                    onReminders = vm::saveReminders,
                    onExport = vm::exportBackup,
                    onImport = vm::importBackup,
                    isPro = isPro,
                    adFreeUntilMillis = adRewards.adFreeUntilMillis,
                    onWatchAdFree = watchAdFree,
                    onRetryAds = ads::retryConsent,
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

private fun contentPercent(progress: ContentDownloadProgress?): Int = when {
    progress == null -> 0
    progress.totalBytes > 0 -> (progress.downloadedBytes * 100 / progress.totalBytes).toInt()
    progress.totalFiles > 0 -> progress.downloadedFiles * 100 / progress.totalFiles
    else -> 0
}.coerceIn(0, 100)
