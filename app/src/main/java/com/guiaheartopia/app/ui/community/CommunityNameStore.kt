package com.guiaheartopia.app.ui.community

import android.content.Context
import android.content.SharedPreferences

/** Persistencia local síncrona del nombre de comunidad; no retrasa el `value` del campo. */
object CommunityNameStore {
    const val MAX_NAME_LENGTH = 28
    private const val PREFS_NAME = "wikifan_community"
    private const val KEY_COMMUNITY_NAME = "community_name"

    fun load(context: Context): String =
        prefs(context).getString(KEY_COMMUNITY_NAME, "").orEmpty().take(MAX_NAME_LENGTH)

    fun save(context: Context, name: String) {
        prefs(context).edit().putString(KEY_COMMUNITY_NAME, name.take(MAX_NAME_LENGTH)).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
