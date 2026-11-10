package com.guiaheartopia.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.guiaheartopia.app.R
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPlan
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val CHANNEL = "guia-recordatorios"
private const val PREFS = "guia-avisos"
private const val IDS = "ids"

object ReminderScheduler {
    fun sync(context: Context, profile: PlayerProfile) {
        val app = context.applicationContext
        val manager = WorkManager.getInstance(app)
        manager.cancelUniqueWork("recordatorio-rutina")
        manager.cancelUniqueWork("recordatorio-clima")
        manager.cancelUniqueWork("recordatorio-eventos")
        val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = store.getStringSet(IDS, emptySet()).orEmpty()
        val zone = runCatching { ZoneId.of(profile.zoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zone)
        val prefs = profile.reminders
        val notices = if (!prefs.anyAlertOn()) {
            emptyList()
        } else {
            val weather = if (prefs.weatherEnabled) {
                ReminderPlan.weatherNotices(SkyCatalog.load(app).days.values, zone, now)
            } else {
                emptyList()
            }
            val raw = AndroidContentLibrary(app).effectiveBytes("calendar/eventos.json")?.decodeToString()
            val events = ReminderPlan.eventNotices(ReminderPlan.parseEvents(raw), prefs, zone, now)
            weather + events
        }
        val nextIds = notices.map { it.id }.toSet()
        (previous - nextIds).forEach { manager.cancelUniqueWork(it) }
        notices.forEach { notice ->
            val delay = Duration.between(now.toInstant(), notice.fireAt.toInstant()).toMillis().coerceAtLeast(0)
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        ReminderWorker.TITLE to notice.title,
                        ReminderWorker.BODY to notice.body,
                        ReminderWorker.ID to notice.id,
                    ),
                )
                .addTag("guia-avisos")
                .build()
            manager.enqueueUniqueWork(notice.id, ExistingWorkPolicy.REPLACE, request)
        }
        store.edit().putStringSet(IDS, nextIds).apply()
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(TITLE).orEmpty()
        val body = inputData.getString(BODY).orEmpty()
        if (title.isNotBlank() && body.isNotBlank()) show(applicationContext, title, body)
        val profile = GuideRepository(applicationContext).snapshots.first().profile
        ReminderScheduler.sync(applicationContext, profile)
        return Result.success()
    }

    companion object {
        const val TITLE = "title"
        const val BODY = "body"
        const val ID = "id"
    }
}

class ReminderRescheduleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val profile = GuideRepository(applicationContext).snapshots.first().profile
        ReminderScheduler.sync(applicationContext, profile)
        return Result.success()
    }
}

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val request = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "recordatorios-tras-reinicio",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}

private fun show(context: Context, title: String, body: String) {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(CHANNEL) == null) {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Recordatorios de la guía", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
    val notification = NotificationCompat.Builder(context, CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_monochrome)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .build()
    manager.notify((title + body).hashCode(), notification)
}
