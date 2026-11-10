package com.guiaheartopia.app.ads

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.guiaheartopia.app.BuildConfig

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
    val hasInterstitial: Boolean = interstitialUnitId.isNotBlank()
    val hasRewardedCalendar: Boolean = rewardedCalendarUnitId.isNotBlank()
    val hasRewardedAdFree: Boolean = rewardedAdFreeUnitId.isNotBlank()
    val usesGoogleTestIds: Boolean = BuildConfig.ADS_USE_GOOGLE_TEST_IDS
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
        !adFree && initialized && (mode == AdsMode.PERSONALIZED || mode == AdsMode.NON_PERSONALIZED)
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

class AdsController(
    private val activity: Activity,
    private val gateway: AdsConsentGateway = UmpConsentGateway(),
) {
    val fullscreen = FullscreenAds(activity)
    var state by mutableStateOf(AdsUiState())
        private set
    private var started = false
    private var initStarted = false

    fun setAdFree(active: Boolean) {
        state = state.copy(adFreeActive = active)
    }

    fun start() {
        if (started) return
        started = true
        if (!AdConfig.adsEnabled) {
            state = AdsUiState(mode = AdsMode.NOT_CONFIGURED, status = "Anuncios sin configurar.")
            return
        }
        gateway.gather(activity) { outcome ->
            activity.runOnUiThread {
                when (outcome) {
                    ConsentOutcome.CAN_REQUEST -> initialize(personalized = true)
                    ConsentOutcome.FAILED -> initialize(personalized = false)
                    ConsentOutcome.NOT_CONSENTED -> {
                        state = state.copy(
                            mode = AdsMode.NOT_CONSENTED,
                            initialized = false,
                            status = "Sin consentimiento: no se piden anuncios.",
                            privacyOptionsRequired = privacyRequired(),
                        )
                    }
                }
            }
        }
    }

    fun showPrivacyOptions() {
        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) {
                state = state.copy(privacyOptionsRequired = privacyRequired())
            }
        } catch (_: Throwable) {
            state = state.copy(status = "No se pudieron abrir las opciones de privacidad.")
        }
    }

    fun onAdsReady() = fullscreen.preload(state.mode)

    fun showInterstitial(adFree: Boolean, onDone: () -> Unit = {}) {
        fullscreen.showInterstitial(adFree, onDone)
    }

    fun showRewardedAdFree(onReward: () -> Unit, onUnavailable: () -> Unit) {
        fullscreen.showRewardedAdFree(onReward, onUnavailable)
    }

    fun showRewardedCalendar(onReward: () -> Unit, onUnavailable: () -> Unit) {
        fullscreen.showRewardedCalendar(onReward, onUnavailable)
    }

    private fun initialize(personalized: Boolean) {
        if (initStarted) return
        initStarted = true
        val mode = if (personalized) AdsMode.PERSONALIZED else AdsMode.NON_PERSONALIZED
        try {
            MobileAds.initialize(activity) {
                activity.runOnUiThread {
                    state = AdsUiState(
                        mode = mode,
                        initialized = true,
                        privacyOptionsRequired = privacyRequired(),
                        status = if (personalized) {
                            "Anuncios listos."
                        } else {
                            "Anuncios no personalizados: el consentimiento no se pudo completar."
                        },
                        adFreeActive = state.adFreeActive,
                    )
                    fullscreen.preload(mode)
                }
            }
        } catch (_: Throwable) {
            state = AdsUiState(
                mode = AdsMode.FAILED,
                initialized = false,
                status = "Los anuncios no están disponibles.",
            )
        }
    }

    private fun privacyRequired(): Boolean = try {
        val status = UserMessagingPlatform.getConsentInformation(activity).privacyOptionsRequirementStatus
        status == com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    } catch (_: Throwable) {
        false
    }
}

@Composable
fun rememberAdsState(activity: Activity, enabled: Boolean): AdsController {
    val controller = remember(activity) { AdsController(activity) }
    LaunchedEffect(enabled) {
        if (enabled) controller.start()
    }
    return controller
}

@Composable
fun DiscreteBanner(mode: AdsMode, modifier: Modifier = Modifier) {
    if (!AdConfig.adsEnabled) return
    if (mode != AdsMode.PERSONALIZED && mode != AdsMode.NON_PERSONALIZED) return
    var visible by remember { mutableStateOf(true) }
    if (!visible) return
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val holder = FrameLayout(context)
            try {
                val adView = AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConfig.bannerUnitId
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(
                                TAG,
                                "load error kind=banner unit=${AdConfig.bannerUnitId} " +
                                    "code=${error.code} message=${error.message}",
                            )
                            visibility = View.GONE
                            visible = false
                        }
                    }
                }
                holder.addView(adView)
                adView.loadAd(adRequest(mode))
            } catch (_: Throwable) {
                visible = false
            }
            holder
        },
    )
}

fun adRequest(mode: AdsMode): AdRequest {
    val builder = AdRequest.Builder()
    if (mode == AdsMode.NON_PERSONALIZED) {
        val extras = Bundle().apply { putString("npa", "1") }
        builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
    }
    return builder.build()
}
