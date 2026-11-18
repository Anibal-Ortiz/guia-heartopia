package com.guiaheartopia.app.domain

/**
 * Un intersticial solo al cambiar de pestaña, nunca en el primer inicio,
 * nunca con el beneficio de 24 h y nunca antes del intervalo mínimo.
 */
object NavInterstitial {
    const val MIN_GAP_MILLIS = 120_000L

    fun allow(
        adFree: Boolean,
        sameTab: Boolean,
        firstLaunch: Boolean,
        nowMillis: Long,
        lastShownMillis: Long,
    ): Boolean {
        if (adFree || sameTab || firstLaunch) return false
        if (lastShownMillis > 0L && nowMillis - lastShownMillis < MIN_GAP_MILLIS) return false
        return true
    }
}
