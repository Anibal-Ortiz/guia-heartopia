package com.guiaheartopia.app.ui.legal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Textos públicos: se actualizan en la web sin publicar un APK nuevo. */
const val PRIVACY_URL = "https://anibalortizdev.github.io/guia-heartopia-contenido/"
const val TERMS_URL = "https://anibalortizdev.github.io/guia-heartopia-contenido/terminos.html"

private fun openUrl(context: Context, url: String): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: Exception) {
    false
}

fun openPrivacyPolicy(context: Context): Boolean = openUrl(context, PRIVACY_URL)

fun openTerms(context: Context): Boolean = openUrl(context, TERMS_URL)
