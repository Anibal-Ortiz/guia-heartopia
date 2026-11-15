package com.guiaheartopia.app.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

private const val TAG = "GuiaAds"

class FullscreenAds(private val activity: Activity) {
    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var rewardedAdFree: RewardedAd? = null
    private var rewardedAdFreeLoading = false
    private var rewardedCalendar: RewardedAd? = null
    private var rewardedCalendarLoading = false

    private var adFree = false

    fun setAdFree(active: Boolean) {
        adFree = active
        if (active) {
            interstitial = null
            rewardedCalendar = null
        }
    }

    fun preload(mode: AdsMode) {
        if (!AdConfig.adsEnabled) return
        if (mode != AdsMode.PERSONALIZED && mode != AdsMode.NON_PERSONALIZED) return
        loadRewardedAdFree(mode)
        if (adFree) return
        loadInterstitial(mode)
        loadRewardedCalendar(mode)
    }

    fun showInterstitial(adFree: Boolean, onDismiss: () -> Unit = {}, onShown: () -> Unit = {}) {
        if (adFree || this.adFree || !AdConfig.hasInterstitial) {
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
        ad.fullScreenContentCallback = simpleCallback(onDismiss) { loadInterstitial(lastMode) }
        onShown()
        ad.show(activity)
    }

    fun showRewardedAdFree(
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        showRewarded(rewardedAdFree, AdConfig.rewardedAdFreeUnitId, onReward, onUnavailable) {
            rewardedAdFree = null
            loadRewardedAdFree(lastMode)
        }
    }

    fun showRewardedCalendar(
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
    ) {
        if (adFree) {
            onUnavailable()
            return
        }
        showRewarded(rewardedCalendar, AdConfig.rewardedCalendarUnitId, onReward, onUnavailable) {
            rewardedCalendar = null
            loadRewardedCalendar(lastMode)
        }
    }

    private var lastMode: AdsMode = AdsMode.IDLE

    private fun loadInterstitial(mode: AdsMode) {
        lastMode = mode
        val id = AdConfig.interstitialUnitId
        if (id.isBlank() || interstitialLoading || interstitial != null) return
        interstitialLoading = true
        InterstitialAd.load(
            activity,
            id,
            adRequest(mode),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialLoading = false
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialLoading = false
                    logLoadError("interstitial", id, error)
                }
            },
        )
    }

    private fun loadRewardedAdFree(mode: AdsMode) {
        lastMode = mode
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
        if (unitId.isBlank() || loading || ready) return
        onStart()
        RewardedAd.load(
            activity,
            unitId,
            adRequest(lastMode),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    onLoaded(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    onFailed()
                    logLoadError(label, unitId, error)
                }
            },
        )
    }

    private fun showRewarded(
        cached: RewardedAd?,
        unitId: String,
        onReward: () -> Unit,
        onUnavailable: () -> Unit,
        afterShow: () -> Unit,
    ) {
        if (unitId.isBlank()) {
            onUnavailable()
            return
        }
        val ad = cached
        if (ad == null) {
            onUnavailable()
            return
        }
        ad.fullScreenContentCallback = simpleCallback(onDismiss = afterShow, onClosed = afterShow)
        ad.show(activity) { onReward() }
    }

    private fun simpleCallback(onDismiss: () -> Unit, onClosed: () -> Unit) =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismiss()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "show error kind=fullscreen code=${error.code} message=${error.message}")
                onDismiss()
                onClosed()
            }
        }
}

private fun logLoadError(kind: String, unitId: String, error: LoadAdError) {
    Log.w(TAG, "load error kind=$kind unit=$unitId code=${error.code} message=${error.message}")
}
