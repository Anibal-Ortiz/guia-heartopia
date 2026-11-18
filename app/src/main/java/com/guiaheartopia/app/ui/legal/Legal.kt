package com.guiaheartopia.app.ui.legal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** URL pública de la política: la misma que se registra en Play y en AdMob. */
const val PRIVACY_URL = "https://anibalortizdev.github.io/guia-heartopia-contenido/"

fun loadLegal(context: Context, asset: String): String =
    context.assets.open(asset).bufferedReader().use { it.readText() }

/**
 * Abre la política publicada en el navegador. Devuelve false si no se pudo
 * abrir (sin navegador, sin red, actividad cancelada) para que la app muestre
 * la copia incluida en el APK como respaldo.
 */
fun openPrivacyPolicy(context: Context): Boolean = try {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: Exception) {
    false
}

@Composable
fun LegalScreen(asset: String, title: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val body = remember(asset) { loadLegal(context, asset) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
    }
}
