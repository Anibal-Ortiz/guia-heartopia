package com.guiaheartopia.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.lang.ref.WeakReference

private const val TAG = "GuiaAds"

/**
 * Anuncios a pantalla completa.
 *
 * Modo seguro por defecto: no se carga nada hasta que [preload] recibe un modo
 * consentido (PERSONALIZED o NON_PERSONALIZED). Se precargan el intersticial y el
 * recompensado de «sin anuncios»; el del Calendario se carga bajo demanda. Tras
 * mostrar o fallar un recompensado se precarga el siguiente para poder acumular
 * varios videos seguidos. El Activity se mantiene con [WeakReference] y las cargas
 * usan applicationContext; tras [onHostDestroyed] se ignoran los callbacks del SDK.
 */
class FullscreenAds(activity: Activity) {
    private val appContext: Context = activity.applicationContext
    private val activityRef = WeakReference<Activity>(activity)

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var rewardedAdFree: RewardedAd? = null
    private var rewardedAdFreeLoading = false
    private var rewardedCalendar: RewardedAd? = null
    private var rewardedCalendarLoading = false

    private var adFree = false
    private var destroyed = false
    private var consentAllowed = false
    private var lastMode: AdsMode = AdsMode.IDLE
    private var modeGeneration = 0

    fun setAdFree(active: Boolean) {
        adFree = active
        if (active) {
            interstitial = null
            rewardedCalendar = null
        }
    }

    /** Llamado desde el ON_DESTROY del host; evita callbacks y cargas posteriores. */
    fun onHostDestroyed() {
        destroyed = true
        interstitial = null
        rewardedAdFree = null
        rewardedCalendar = null
        interstitialLoading = false
        rewardedAdFreeLoading = false
        rewardedCalendarLoading = false
    }

    private fun canLoad(mode: AdsMode): Boolean =
        !destroyed && consentAllowed && AdConfig.adsEnabled &&
            (mode == AdsMode.PERSONALIZED || mode == AdsMode.NON_PERSONALIZED)

    /**
     * Precarga idempotente del intersticial y del recompensado de «sin anuncios».
     * El del Calendario sigue cargándose solo cuando se solicita. Un modo no
     * consentido deja el objeto en modo seguro y limpia la caché.
     */
    fun preload(mode: AdsMode) {
        lastMode = mode
        consentAllowed = AdConfig.adsEnabled &&
            (mode == AdsMode.PERSONALIZED || mode == AdsMode.NON_PERSONALIZED)
        if (!consentAllowed) {
            interstitial = null
            rewardedAdFree = null
            rewardedCalendar = null
            return
        }
        if (!adFree) loadInterstitial(mode)
        // También con el beneficio activo: así el botón «+3h» puede sumar otro video.
        loadRewardedAdFree(mode)
    }

    /** Reintenta en segundo plano la carga del recompensado de «sin anuncios». */
    fun preloadRewardedAdFree() {
        if (destroyed || !consentAllowed) return
        loadRewardedAdFree(lastMode)
    }

    /**
     * Descarta los anuncios ya cargados (y los que estén en vuelo) antes de
     * cambiar de modo, para que un anuncio personalizado no se muestre después
     * de pasar a `npa=1`.
     */
    fun clearLoadedAds() {
        modeGeneration++
        interstitial = null
        rewardedAdFree = null
        rewardedCalendar = null
    }

    fun showInterstitial(adFree: Boolean, onDismiss: () -> Unit = {}, onShown: () -> Unit = {}) {
        if (destroyed) {
            onDismiss()
            return
        }
        if (adFree || this.adFree || !AdConfig.hasInterstitial || !consentAllowed) {
            onDismiss()
            return
        }
        val ad = interstitial
        if (ad == null) {
            loadInterstitial(lastMode)
            onDismiss()
            return
        }
        interstitial = null
        val host = activityRef.get()
        if (host == null) {
            onDismiss()
            return
        }
        val reload = once { loadInterstitial(lastMode) }
        ad.fullScreenContentCallback = fullScreenCallback(
            onDismissed = { onDismiss(); reload() },
            onShowed = onShown,
            onFailed = { onDismiss(); reload() },
        )
        try {
            ad.show(host)
        } catch (_: Throwable) {
            onDismiss()
            reload()
        }
    }

    fun showRewardedAdFree(
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        if (destroyed || !consentAllowed) {
            onUnavailable()
            return
        }
        val ad = rewardedAdFree
        if (ad == null) {
            loadRewardedAdFree(lastMode)
            onUnavailable()
            return
        }
        showRewarded(ad, onReward, onUnavailable) {
            rewardedAdFree = null
            loadRewardedAdFree(lastMode)
        }
    }

    fun showRewardedCalendar(
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        if (adFree || destroyed || !AdConfig.hasRewardedCalendar || !consentAllowed) {
            onUnavailable()
            return
        }
        val ad = rewardedCalendar
        if (ad == null) {
            loadRewardedCalendar(lastMode)
            onUnavailable()
            return
        }
        showRewarded(ad, onReward, onUnavailable) {
            rewardedCalendar = null
            loadRewardedCalendar(lastMode)
        }
    }

    private fun loadInterstitial(mode: AdsMode) {
        lastMode = mode
        if (!canLoad(mode)) return
        val id = AdConfig.interstitialUnitId
        if (id.isBlank() || interstitialLoading || interstitial != null) return
        interstitialLoading = true
        val generation = modeGeneration
        try {
            InterstitialAd.load(
                appContext,
                id,
                adRequest(mode),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialLoading = false
                        if (destroyed || !consentAllowed || generation != modeGeneration) return
                        interstitial = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialLoading = false
                        logLoadError("interstitial", error)
                    }
                },
            )
        } catch (_: Throwable) {
            interstitialLoading = false
        }
    }

    private fun loadRewardedAdFree(mode: AdsMode) {
        lastMode = mode
        if (!canLoad(mode)) return
        loadRewarded(
            AdConfig.rewardedAdFreeUnitId,
            rewardedAdFreeLoading,
            rewardedAdFree != null,
            onStart = { rewardedAdFreeLoading = true },
            onLoaded = { rewardedAdFree = it; rewardedAdFreeLoading = false },
            onFailed = { rewardedAdFreeLoading = false },
            label = "rewarded_adfree",
        )
    }

    private fun loadRewardedCalendar(mode: AdsMode) {
        lastMode = mode
        if (!canLoad(mode)) return
        loadRewarded(
            AdConfig.rewardedCalendarUnitId,
            rewardedCalendarLoading,
            rewardedCalendar != null,
            onStart = { rewardedCalendarLoading = true },
            onLoaded = { rewardedCalendar = it; rewardedCalendarLoading = false },
            onFailed = { rewardedCalendarLoading = false },
            label = "rewarded_calendar",
        )
    }

    private fun loadRewarded(
        unitId: String,
        loading: Boolean,
        ready: Boolean,
        onStart: () -> Unit,
        onLoaded: (RewardedAd) -> Unit,
        onFailed: () -> Unit,
        label: String,
    ) {
        if (!canLoad(lastMode)) return
        if (unitId.isBlank() || loading || ready) return
        onStart()
        val generation = modeGeneration
        try {
            RewardedAd.load(
                appContext,
                unitId,
                adRequest(lastMode),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        if (destroyed || !consentAllowed || generation != modeGeneration) {
                            onFailed()
                            return
                        }
                        onLoaded(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        onFailed()
                        logLoadError(label, error)
                    }
                },
            )
        } catch (_: Throwable) {
            onFailed()
        }
    }

    private fun showRewarded(
        ad: RewardedAd,
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
        afterShow: () -> Unit,
    ) {
        val host = activityRef.get()
        if (host == null) {
            onUnavailable()
            return
        }
        // dismissed y failed son rutas distintas, pero solo se ejecuta una vez.
        val finish = once(afterShow)
        ad.fullScreenContentCallback = fullScreenCallback(
            onDismissed = finish,
            onFailed = finish,
        )
        try {
            ad.show(host) { onReward() }
        } catch (_: Throwable) {
            onUnavailable()
            finish()
        }
    }

    private fun fullScreenCallback(
        onDismissed: () -> Unit = {},
        onShowed: () -> Unit = {},
        onFailed: () -> Unit = {},
    ): FullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            if (!destroyed) onShowed()
        }

        override fun onAdDismissedFullScreenContent() {
            if (!destroyed) onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(error: AdError) {
            Log.w(TAG, "show error kind=fullscreen code=${error.code}")
            if (!destroyed) onFailed()
        }
    }

    /** Envuelve [action] para que se ejecute como máximo una vez. */
    private fun once(action: () -> Unit): () -> Unit {
        var called = false
        return {
            if (!called) {
                called = true
                action()
            }
        }
    }
}

/** Logs sin IDs de unidad: solo el tipo de anuncio y el código de error. */
private fun logLoadError(kind: String, error: LoadAdError) {
    Log.w(TAG, "load error kind=$kind code=${error.code}")
}
