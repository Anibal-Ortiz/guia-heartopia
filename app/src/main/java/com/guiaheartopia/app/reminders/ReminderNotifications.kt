package com.guiaheartopia.app.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.guiaheartopia.app.R
import com.guiaheartopia.app.domain.ReminderIds

private const val CHANNEL = "guia-recordatorios"

internal object ReminderNotifications {
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Recordatorios de la guía", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    /** Comprueba el permiso aquí también: el aviso se puede mostrar desde más de un call-site. */
    fun show(context: Context, title: String, body: String, id: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val app = context.applicationContext
        ensureChannel(app)
        val manager = app.getSystemService(NotificationManager::class.java)
        val launch = app.packageManager.getLaunchIntentForPackage(app.packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentIntent = launch?.let { PendingIntent.getActivity(app, 0, it, flags) }
        val notification = NotificationCompat.Builder(app, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setGroup("guia-avisos")
            .build()
        val notificationId = if (id.isBlank()) {
            ReminderIds.notificationId("$title|$body")
        } else {
            ReminderIds.notificationId(id)
        }
        manager.notify(notificationId, notification)
    }
}
