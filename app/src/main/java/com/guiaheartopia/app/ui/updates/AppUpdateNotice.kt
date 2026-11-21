package com.guiaheartopia.app.ui.updates

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

private const val PACKAGE_NAME = "com.guiaheartopia.app"
private const val PLAY_URL = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"
private const val MARKET_URL = "market://details?id=$PACKAGE_NAME"
private const val PLAY_STORE_PACKAGE = "com.android.vending"

/**
 * Consulta a Google Play si hay una versión nueva. Si la consulta falla (debug instalado a mano,
 * Play Store no disponible, sin red, etc.) simplemente no se muestra el aviso.
 */
fun checkPlayUpdate(context: Context, onResult: (Boolean) -> Unit) {
    try {
        AppUpdateManagerFactory.create(context)
            .appUpdateInfo
            .addOnSuccessListener { info ->
                onResult(info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
            }
            .addOnFailureListener { onResult(false) }
    } catch (_: Exception) {
        onResult(false)
    }
}

/**
 * Abre la ficha de la app en Google Play (o el navegador si no hay app de Play instalada).
 * Fijamos el paquete de Play Store para que un `market://` no lo capture la tienda del
 * fabricante (Galaxy Store, AppGallery, Mi GetApps, ...), que también registra ese esquema.
 */
fun openPlayStore(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL))
        .setPackage(PLAY_STORE_PACKAGE)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val opened = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: Exception) {
        false
    }
    if (!opened) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: Exception) {
            // Sin navegador tampoco: no hay nada más que hacer.
        }
    }
}

@Composable
fun UpdateAvailableDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualización disponible") },
        text = {
            Text(
                "Hay una versión nueva de WikiFan Heartopia en Google Play. " +
                    "Actualiza para tener las guías y mejoras más recientes.",
            )
        },
        confirmButton = {
            TextButton(onClick = onUpdate) { Text("Actualizar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ahora no") }
        },
    )
}
