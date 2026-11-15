package com.guiaheartopia.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
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

    fun show(context: Context, title: String, body: String, id: String) {
        ensureChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
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