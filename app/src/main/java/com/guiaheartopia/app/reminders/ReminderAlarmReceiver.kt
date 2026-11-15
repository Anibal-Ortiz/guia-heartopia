package com.guiaheartopia.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.guiaheartopia.app.data.GuideRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        if (title.isBlank() || body.isBlank() || id.isBlank()) return
        val app = context.applicationContext
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                app,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ReminderScheduler.markPosted(app, id)
                return
            }
        }
        ReminderNotifications.show(app, title, body, id)
        ReminderScheduler.markPosted(app, id)
        val profile = runBlocking { GuideRepository(app).snapshots.first() }.profile
        ReminderScheduler.sync(app, profile)
    }

    companion object {
        const val EXTRA_ID = "id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
    }
}