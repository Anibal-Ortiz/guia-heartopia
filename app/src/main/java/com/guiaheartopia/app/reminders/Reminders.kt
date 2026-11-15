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
import com.guiaheartopia.app.domain.ReminderIds
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderSync
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import com.guiaheartopia.app.domain.CropReminders
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val CHANNEL = "guia-recordatorios"
private const val PREFS = "guia-avisos"
private const val ENQUEUED = "enqueued"
private const val POSTED = "posted"

object ReminderScheduler {
    fun sync(context: Context, profile: PlayerProfile) {
        val app = context.applicationContext
        val manager = WorkManager.getInstance(app)
        manager.cancelUniqueWork("recordatorio-rutina")
        manager.cancelUniqueWork("recordatorio-clima")
        manager.cancelUniqueWork("recordatorio-eventos")
        val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = store.getStringSet(ENQUEUED, emptySet()).orEmpty()
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty()
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
            val crops = runBlocking {
                GuideRepository(app).snapshots.first().activeCrops
            }
            weather + events + CropReminders.notices(crops, zone, now)
        }
        val diff = ReminderSync.plan(previous, posted, notices)
        diff.cancel.forEach { manager.cancelUniqueWork(it) }
        diff.enqueue.forEach { notice ->
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
            // KEEP: un aviso ya encolado no se reemplaza ni se borra al reprogramar otro.
            manager.enqueueUniqueWork(notice.id, ExistingWorkPolicy.KEEP, request)
        }
        val stillWanted = notices.map { it.id }.toSet()
        store.edit()
            .putStringSet(ENQUEUED, diff.keep + diff.enqueue.map { it.id })
            .putStringSet(POSTED, posted.intersect(stillWanted))
            .apply()
    }

    fun markPosted(context: Context, id: String) {
        if (id.isBlank()) return
        val store = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty() + id
        val enqueued = store.getStringSet(ENQUEUED, emptySet()).orEmpty() - id
        store.edit()
            .putStringSet(POSTED, posted)
            .putStringSet(ENQUEUED, enqueued)
            .apply()
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(TITLE).orEmpty()
        val body = inputData.getString(BODY).orEmpty()
        val id = inputData.getString(ID).orEmpty()
        if (title.isNotBlank() && body.isNotBlank()) {
            show(applicationContext, title, body, id)
            ReminderScheduler.markPosted(applicationContext, id)
        }
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

private fun show(context: Context, title: String, body: String, id: String) {
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
        .setGroup("guia-avisos")
        .build()
    val notificationId = if (id.isBlank()) ReminderIds.notificationId("$title|$body") else ReminderIds.notificationId(id)
    manager.notify(notificationId, notification)
}
