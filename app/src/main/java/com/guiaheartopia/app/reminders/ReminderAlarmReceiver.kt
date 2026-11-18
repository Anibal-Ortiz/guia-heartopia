package com.guiaheartopia.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (title.isBlank() || body.isBlank() || id.isBlank()) return
        val app = context.applicationContext
        val pending = goAsync()
        try {
            val granted = Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    app,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                try {
                    ReminderNotifications.show(app, title, body, id)
                } catch (_: Throwable) {
                    // No bloquear la reprogramación si el sistema rechaza el aviso.
                }
            }
            ReminderScheduler.markPosted(app, id)
        } finally {
            // La reprogramación es común: también se encola sin POST_NOTIFICATIONS
            // o si mostrar el aviso falló, para no perder los próximos avisos.
            try {
                val request = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>().build()
                WorkManager.getInstance(app).enqueueUniqueWork(
                    "recordatorios-tras-alarma",
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}
