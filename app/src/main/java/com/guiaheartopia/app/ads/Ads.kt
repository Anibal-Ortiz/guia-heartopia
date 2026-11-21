package com.guiaheartopia.app.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.guiaheartopia.app.BuildConfig
import java.lang.ref.WeakReference

private const val TAG = "GuiaAds"

/**
 * IDs y banderas salen de BuildConfig.
 * Con marcadores en gradle.properties/local.properties, release usa los IDs oficiales de prueba.
 */
object AdConfig {
    val adsEnabled: Boolean = BuildConfig.ADS_ENABLED &&
        BuildConfig.ADMOB_APP_ID.isNotBlank() &&
        BuildConfig.ADMOB_BANNER_UNIT_ID.isNotBlank()
    val bannerUnitId: String = BuildConfig.ADMOB_BANNER_UNIT_ID
    val interstitialUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID
    val rewardedCalendarUnitId: String = BuildConfig.ADMOB_REWARDED_CALENDAR_UNIT_ID
    val rewardedAdFreeUnitId: String = BuildConfig.ADMOB_REWARDED_ADFREE_UNIT_ID
    val rewardedAlarmUnitId: String = BuildConfig.ADMOB_REWARDED_ALARM_UNIT_ID
    val hasInterstitial: Boolean = interstitialUnitId.isNotBlank()
    val hasRewardedCalendar: Boolean = rewardedCalendarUnitId.isNotBlank()
    val hasRewardedAdFree: Boolean = rewardedAdFreeUnitId.isNotBlank()
    val hasRewardedAlarm: Boolean = rewardedAlarmUnitId.isNotBlank()
    val usesGoogleTestIds: Boolean = BuildConfig.ADS_USE_GOOGLE_TEST_IDS

    /**
     * UMP 4.0 no permite saber si el consentimiento autoriza la personalización
     * (`OBTAINED` = «personalized vs non-personalized undefined»). Política por
     * defecto: personalizado. Poner `false` aquí hace que TODAS las peticiones
     * (banner, intersticial y recompensados) lleven `npa=1`.
     * En caliente se puede conmutar con `AdsController.setPersonalizedAds(...)`.
     */
    val personalizedAds: Boolean = true
    val umpDebugGeography: Int? = if (BuildConfig.DEBUG) geography(BuildConfig.UMP_DEBUG_GEOGRAPHY) else null
    val umpTestDeviceId: String = if (BuildConfig.DEBUG) BuildConfig.UMP_TEST_DEVICE_ID else ""

    private fun geography(raw: String): Int? = when (raw.trim().uppercase()) {
        "EEA" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
        "NOT_EEA" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
        "REGULATED_US_STATE" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE
        "OTHER" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER
        else -> null
    }
}

enum class AdsMode {
    IDLE,
    PERSONALIZED,
    NON_PERSONALIZED,
    NOT_CONSENTED,
    NOT_CONFIGURED,
    FAILED,
}

data class AdsUiState(
    val mode: AdsMode = AdsMode.IDLE,
    val initialized: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val status: String = "",
    val adFreeActive: Boolean = false,
) {
    fun canShowBanner(adFree: Boolean): Boolean =
        !adFree && !adFreeActive && initialized &&
            (mode == AdsMode.PERSONALIZED || mode == AdsMode.NON_PERSONALIZED)
}

enum class ConsentOutcome {
    CAN_REQUEST,
    NOT_CONSENTED,
    FAILED,
}

fun interface AdsConsentGateway {
    fun gather(activity: Activity, onResult: (ConsentOutcome) -> Unit)
}

class UmpConsentGateway : AdsConsentGateway {
    override fun gather(activity: Activity, onResult: (ConsentOutcome) -> Unit) {
        try {
            val info = UserMessagingPlatform.getConsentInformation(activity)
            val params = ConsentRequestParameters.Builder()
            val geo = AdConfig.umpDebugGeography
            if (geo != null) {
                val debug = ConsentDebugSettings.Builder(activity).setDebugGeography(geo)
                if (AdConfig.umpTestDeviceId.isNotBlank()) {
                    debug.addTestDeviceHashedId(AdConfig.umpTestDeviceId)
                }
                params.setConsentDebugSettings(debug.build())
            }
            info.requestConsentInfoUpdate(
                activity,
                params.build(),
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            onResult(ConsentOutcome.FAILED)
                        } else if (info.canRequestAds()) {
                            onResult(ConsentOutcome.CAN_REQUEST)
                        } else {
                            onResult(ConsentOutcome.NOT_CONSENTED)
                        }
                    }
                },
                { onResult(ConsentOutcome.FAILED) },
            )
        } catch (_: Throwable) {
            onResult(ConsentOutcome.FAILED)
        }
    }
}

/**
 * Controlador de anuncios. La firma pública se mantiene intacta: el host llama a
 * rememberAdsState(activity, ready). Internamente se guarda applicationContext y
 * una [WeakReference] al Activity; las operaciones que exigen Activity se ignoran
 * después de ON_DESTROY, y los callbacks del SDK se publican en el hilo principal
 * solo mientras el host siga vivo.
 */
class AdsController(
    activity: Activity,
    private val gateway: AdsConsentGateway = UmpConsentGateway(),
    personalizedAds: Boolean = true,
) {
    private val appContext: Context = activity.applicationContext
    private val activityRef = WeakReference<Activity>(activity)
    private val mainHandler = Handler(Looper.getMainLooper())

    val fullscreen = FullscreenAds(activity)
    var state by mutableStateOf(AdsUiState())
        private set
    private var started = false
    private var initStarted = false
    private var destroyed = false

    /**
     * UMP 4.0 no expone si el consentimiento permite personalizar: `OBTAINED`
     * significa «personalized vs non-personalized undefined». Por eso la decisión
     * se aísla aquí. [setPersonalizedAds] conmuta TODAS las peticiones (banner,
     * intersticial y recompensados) a `npa=1` sin reinicializar el SDK.
     */
    private var personalizedAds: Boolean = personalizedAds
        private set

    fun setPersonalizedAds(enabled: Boolean) {
        if (destroyed || personalizedAds == enabled) return
        personalizedAds = enabled
        if (!state.initialized) return // se aplicará al inicializar el SDK
        if (!canRequestAdsNow()) {
            applyNotConsented()
        } else {
            applyMode(if (enabled) AdsMode.PERSONALIZED else AdsMode.NON_PERSONALIZED)
        }
    }

    init {
        val owner = activity as? LifecycleOwner
        if (owner != null) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    destroyed = true
                    fullscreen.onHostDestroyed()
                }
            }
            owner.lifecycle.addObserver(observer)
        }
    }

    private fun currentActivity(): Activity? =
        if (destroyed) null else activityRef.get()?.takeUnless { it.isFinishing || it.isDestroyed }

    private fun postToUi(block: () -> Unit) {
        mainHandler.post { if (!destroyed) block() }
    }

    fun setAdFree(active: Boolean) {
        val was = state.adFreeActive
        state = state.copy(adFreeActive = active)
        fullscreen.setAdFree(active)
        if (was && !active && state.initialized) fullscreen.preload(state.mode)
    }

    fun start() {
        if (started || destroyed) return
        started = true
        if (!AdConfig.adsEnabled) {
            state = AdsUiState(mode = AdsMode.NOT_CONFIGURED, status = "Anuncios sin configurar.")
            return
        }
        requestConsent()
    }

    /**
     * Reintenta el flujo de consentimiento tras FAILED o NOT_CONSENTED.
     * Call-site: opcional desde ui/, p. ej. un botón "Reintentar" junto al estado
     * de anuncios: `ads.retryConsent()`. No cambia la API existente de MainActivity.
     */
    fun retryConsent() {
        if (destroyed || !AdConfig.adsEnabled) return
        if (state.mode != AdsMode.FAILED && state.mode != AdsMode.NOT_CONSENTED) return
        initStarted = false
        requestConsent()
    }

    private fun requestConsent() {
        val host = currentActivity() ?: return
        state = state.copy(status = "Comprobando consentimiento…")
        try {
            gateway.gather(host) { outcome ->
                postToUi {
                    // Una sola lectura de canRequestAds: evita decisiones encontradas
                    // entre el gateway y la inicialización del SDK.
                    when (ConsentDecision.mode(outcome, canRequestAdsNow(), personalizedAds)) {
                        AdsMode.PERSONALIZED, AdsMode.NON_PERSONALIZED ->
                            initialize(personalized = personalizedAds)
                        // FAILED nunca inicializa el SDK con npa=1: modo seguro + reintento.
                        AdsMode.FAILED -> applyFailed()
                        else -> applyNotConsented()
                    }
                }
            }
        } catch (_: Throwable) {
            postToUi { applyFailed() }
        }
    }

    private fun applyFailed() {
        state = AdsUiState(
            mode = AdsMode.FAILED,
            initialized = false,
            status = "No se pudo comprobar el consentimiento. Puedes reintentar.",
            privacyOptionsRequired = privacyRequired(),
            adFreeActive = state.adFreeActive,
        )
        fullscreen.preload(AdsMode.FAILED)
    }

    private fun applyNotConsented() {
        state = AdsUiState(
            mode = AdsMode.NOT_CONSENTED,
            initialized = false,
            status = "Sin consentimiento: no se piden anuncios.",
            privacyOptionsRequired = privacyRequired(),
            adFreeActive = state.adFreeActive,
        )
        fullscreen.preload(AdsMode.NOT_CONSENTED)
    }

    /** Cambia el modo con el SDK ya inicializado, descartando los anuncios del modo anterior. */
    private fun applyMode(mode: AdsMode) {
        if (state.mode == mode) return
        fullscreen.clearLoadedAds()
        state = state.copy(
            mode = mode,
            privacyOptionsRequired = privacyRequired(),
            status = if (mode == AdsMode.NON_PERSONALIZED) {
                "Anuncios no personalizados."
            } else {
                "Anuncios listos."
            },
        )
        fullscreen.preload(mode)
    }

    fun showPrivacyOptions() {
        val host = currentActivity() ?: return
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(host) {
                postToUi {
                    if (!canRequestAdsNow()) {
                        applyNotConsented()
                    } else {
                        state = state.copy(privacyOptionsRequired = privacyRequired())
                        applyMode(if (personalizedAds) AdsMode.PERSONALIZED else AdsMode.NON_PERSONALIZED)
                    }
                }
            }
        } catch (_: Throwable) {
            postToUi { state = state.copy(status = "No se pudieron abrir las opciones de privacidad.") }
        }
    }

    fun onAdsReady() {
        if (canRequestAdsNow()) fullscreen.preload(state.mode)
        else applyNotConsented()
    }

    fun showInterstitial(adFree: Boolean, onDone: () -> Unit = {}, onShown: () -> Unit = {}) {
        fullscreen.showInterstitial(adFree, onDone, onShown)
    }

    fun showRewardedAdFree(onReward: () -> Unit, onUnavailable: () -> Unit) {
        fullscreen.showRewardedAdFree(onReward, onUnavailable)
    }

    /** Deja listo el siguiente video bonificado para acumular horas seguidas. */
    fun preloadRewardedAdFree() {
        fullscreen.preloadRewardedAdFree()
    }

    fun showRewardedCalendar(onReward: () -> Unit, onUnavailable: () -> Unit) {
        fullscreen.showRewardedCalendar(onReward, onUnavailable)
    }

    /** Recompensado de alarmas: no se ve afectado por el beneficio «sin anuncios». */
    fun showRewardedAlarm(onReward: () -> Unit, onUnavailable: () -> Unit) {
        fullscreen.showRewardedAlarm(onReward, onUnavailable)
    }

    fun preloadRewardedAlarm() {
        fullscreen.preloadRewardedAlarm()
    }

    /** Solo se inicializa el SDK si UMP permite pedir anuncios. */
    private fun canRequestAdsNow(): Boolean = try {
        UserMessagingPlatform.getConsentInformation(appContext).canRequestAds()
    } catch (_: Throwable) {
        false
    }

    private fun initialize(personalized: Boolean) {
        if (initStarted) return
        if (!canRequestAdsNow()) {
            applyNotConsented()
            return
        }
        initStarted = true
        val mode = if (personalized) AdsMode.PERSONALIZED else AdsMode.NON_PERSONALIZED
        try {
            MobileAds.initialize(appContext) {
                postToUi {
                    if (!canRequestAdsNow()) {
                        applyNotConsented()
                        return@postToUi
                    }
                    state = AdsUiState(
                        mode = mode,
                        initialized = true,
                        privacyOptionsRequired = privacyRequired(),
                        status = if (personalized) {
                            "Anuncios listos."
                        } else {
                            "Anuncios no personalizados."
                        },
                        adFreeActive = state.adFreeActive,
                    )
                    fullscreen.setAdFree(state.adFreeActive)
                    fullscreen.preload(mode)
                }
            }
        } catch (_: Throwable) {
            applyFailed()
        }
    }

    private fun privacyRequired(): Boolean = try {
        val status = UserMessagingPlatform.getConsentInformation(appContext).privacyOptionsRequirementStatus
        status == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    } catch (_: Throwable) {
        false
    }
}

@Composable
fun rememberAdsState(activity: Activity, enabled: Boolean): AdsController {
    val controller = remember(activity) {
        AdsController(activity, personalizedAds = AdConfig.personalizedAds)
    }
    LaunchedEffect(enabled) {
        if (enabled) controller.start()
    }
    return controller
}

/**
 * Banner adaptativo. Firma intacta: `DiscreteBanner(adsMode)`.
 * Se oculta hasta onAdLoaded, reintenta con backoff y se pausa/destruye con el
 * ciclo de vida del composable. No añade padding: MainActivity reserva el espacio.
 */
@Composable
fun DiscreteBanner(mode: AdsMode, modifier: Modifier = Modifier) {
    if (!AdConfig.adsEnabled) return
    if (mode != AdsMode.PERSONALIZED && mode != AdsMode.NON_PERSONALIZED) return
    val hostRef = remember { mutableStateOf<BannerHost?>(null) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val hostView = FrameLayout(ctx)
            val host = BannerHost()
            val adView = AdView(ctx).apply {
                val density = ctx.resources.displayMetrics.density.coerceAtLeast(0.01f)
                val widthDp = (ctx.resources.displayMetrics.widthPixels / density).toInt().coerceAtLeast(1)
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, widthDp))
                adUnitId = AdConfig.bannerUnitId
                visibility = View.GONE
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        if (host.destroyed) return
                        visibility = View.VISIBLE
                        host.failures = 0
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        visibility = View.GONE
                        Log.w(TAG, "load error kind=banner code=${error.code}")
                        if (!host.destroyed) scheduleBannerRetry(host)
                    }
                }
            }
            host.adView = adView
            hostRef.value = host
            hostView.addView(adView)
            hostView
        },
        update = { _ ->
            val host = hostRef.value ?: return@AndroidView
            if (host.destroyed || host.lastMode == mode) return@AndroidView
            host.retry?.let { bannerHandler.removeCallbacks(it) }
            host.retry = null
            host.lastMode = mode
            try {
                host.adView.loadAd(adRequest(mode))
            } catch (_: Throwable) {
                Unit
            }
        },
    )

    DisposableEffect(lifecycle) {
        val observer = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                val host = hostRef.value ?: return
                when (event) {
                    Lifecycle.Event.ON_RESUME -> host.adView.resume()
                    Lifecycle.Event.ON_PAUSE -> host.adView.pause()
                    else -> Unit
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            val host = hostRef.value
            host?.destroyed = true
            host?.retry?.let { bannerHandler.removeCallbacks(it) }
            try {
                host?.adView?.destroy()
            } catch (_: Throwable) {
                Unit
            }
            hostRef.value = null
        }
    }
}

private val bannerHandler = Handler(Looper.getMainLooper())

private class BannerHost {
    lateinit var adView: AdView
    var failures = 0
    var retry: Runnable? = null
    var destroyed = false
    var lastMode: AdsMode? = null
}

/** Backoff 2s, 4s, 8s, 16s, 32s (tope). Sin IDs de unidad en los logs. */
private fun scheduleBannerRetry(host: BannerHost) {
    if (host.destroyed) return
    host.retry?.let { bannerHandler.removeCallbacks(it) }
    val attempt = host.failures++
    val delayMs = 2_000L shl minOf(attempt, 4)
    val runnable = Runnable {
        host.retry = null
        if (host.destroyed) return@Runnable
        val currentMode = host.lastMode
        if (currentMode != AdsMode.PERSONALIZED && currentMode != AdsMode.NON_PERSONALIZED) return@Runnable
        try {
            host.adView.loadAd(adRequest(currentMode))
        } catch (_: Throwable) {
            Unit
        }
    }
    host.retry = runnable
    bannerHandler.postDelayed(runnable, delayMs)
}

fun adRequest(mode: AdsMode): AdRequest {
    val builder = AdRequest.Builder()
    if (mode == AdsMode.NON_PERSONALIZED) {
        val extras = Bundle().apply { putString("npa", "1") }
        builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
    }
    return builder.build()
}
