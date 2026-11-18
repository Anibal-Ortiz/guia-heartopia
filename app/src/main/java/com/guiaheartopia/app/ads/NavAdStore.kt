package com.guiaheartopia.app.ads

import android.content.Context

/**
 * El primer arranque no muestra intersticiales. El intervalo por sesión vive en
 * [com.guiaheartopia.app.domain.NavInterstitial] (domain/, fuera de este módulo):
 * si se necesita un hueco mayor entre intersticiales dentro de una misma sesión,
 * ajustar el cálculo del intervalo en esa clase. `markShown` solo registra el
 * momento real en que el anuncio se mostró (lo llama `onShown` del controller).
 */
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
