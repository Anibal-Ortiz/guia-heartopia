package com.guiaheartopia.app.ads

import android.content.Context

/** El primer arranque no muestra intersticiales. El intervalo vive en [com.guiaheartopia.app.domain.NavInterstitial]. */
object NavAdStore {
    private const val PREFS = "guia-anuncios-nav"
    private const val HAD_SESSION = "had_session"
    private const val LAST_SHOWN = "last_shown"

    fun isFirstSession(context: Context): Boolean =
        !context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(HAD_SESSION, false)

    fun completeSession(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(HAD_SESSION, true)
            .apply()
    }

    fun lastShownMillis(context: Context): Long =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(LAST_SHOWN, 0L)

    fun markShown(context: Context, now: Long = System.currentTimeMillis()) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(LAST_SHOWN, now)
            .apply()
    }
}
