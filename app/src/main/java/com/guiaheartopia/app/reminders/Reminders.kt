package com.guiaheartopia.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.guiaheartopia.app.data.AndroidContentLibrary
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderIds
import com.guiaheartopia.app.domain.ReminderPlan
import com.guiaheartopia.app.domain.ReminderSync
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.guiaheartopia.app.domain.CreatureWindowReminders
import com.guiaheartopia.app.domain.CropReminders

private const val PREFS = "guia-avisos"
private const val ENQUEUED = "enqueued"
private const val POSTED = "posted"

object ReminderScheduler {
    fun sync(context: Context, profile: PlayerProfile) {
        val app = context.applicationContext
        WorkManager.getInstance(app).cancelUniqueWork("recordatorio-rutina")
        WorkManager.getInstance(app).cancelUniqueWork("recordatorio-clima")
        WorkManager.getInstance(app).cancelUniqueWork("recordatorio-eventos")
        val store = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val previous = store.getStringSet(ENQUEUED, emptySet()).orEmpty()
        val posted = store.getStringSet(POSTED, emptySet()).orEmpty()
        val zone = runCatching { ZoneId.of(profile.zoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zone)
        val prefs = profile.reminders
        val scheduled = runBlocking { GuideRepository(app).snapshots.first() }
        val personal = CropReminders.notices(scheduled.activeCrops, zone, now) +
            CreatureWindowReminders.notices(scheduled.creatureWindowAlarms, zone, now)
        val notices = if (!prefs.anyAlertOn()) {
            personal
        } else {
            val weather = if (prefs.weatherEnabled) {
                ReminderPlan.weatherNotices(SkyCatalog.load(app).days.values, zone, now)
            } else {
                emptyList()
            }
            val raw = AndroidContentLibrary(app).effectiveBytes("calendar/eventos.json")?.decodeToString()
            val events = ReminderPlan.eventNotices(ReminderPlan.parseEvents(raw), prefs, zone, now)
            weather + events + personal
        }
        val future = notices.filter { it.fireAt.isAfter(now) }
        val expired = notices.filter { !it.fireAt.isAfter(now) }
        val stillPosted = posted.toMutableSet()
        expired.forEach { stillPosted += it.id }
        val diff = ReminderSync.plan(previous, stillPosted, future)
        diff.cancel.forEach { ReminderAlarms.cancel(app, it) }
        diff.enqueue.forEach { ReminderAlarms.schedule(app, it) }
        val stillWanted = future.map { it.id }.toSet()
        store.edit()
            .putStringSet(ENQUEUED, diff.keep + diff.enqueue.map { it.id })
            .putStringSet(POSTED, stillPosted.intersect(stillWanted + expired.map { it.id }.toSet()))
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
            ReminderNotifications.show(applicationContext, title, body, id)
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