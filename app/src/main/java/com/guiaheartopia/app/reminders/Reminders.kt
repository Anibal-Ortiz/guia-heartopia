package com.guiaheartopia.app.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.guiaheartopia.app.R
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.ReminderPlanner
import com.guiaheartopia.app.domain.ReminderPrefs
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val CHANNEL = "guia-recordatorios"

object ReminderScheduler {
    fun sync(context: Context, profile: PlayerProfile) {
        val zone = runCatching { ZoneId.of(profile.zoneId) }.getOrElse { ZoneId.systemDefault() }
        val now = ZonedDateTime.now(zone)
        val prefs = profile.reminders
        schedule(context, "recordatorio-rutina", prefs.routineEnabled, prefs.routineMinutes, now)
        schedule(context, "recordatorio-clima", prefs.weatherEnabled, prefs.weatherMinutes, now)
        schedule(context, "recordatorio-eventos", prefs.eventsEnabled, prefs.eventsMinutes, now)
    }

    private fun schedule(context: Context, name: String, enabled: Boolean, minutes: Int, now: ZonedDateTime) {
        val manager = WorkManager.getInstance(context)
        if (!enabled) {
            manager.cancelUniqueWork(name)
            return
        }
        val next = ReminderPlanner.next(now, minutes)
        val delay = Duration.between(now.toInstant(), next.toInstant()).toMillis().coerceAtLeast(60_000)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY to name))
            .build()
        manager.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, request)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY) ?: return Result.success()
        show(applicationContext, kind)
        val profile = GuideRepository(applicationContext).snapshots.first().profile
        ReminderScheduler.sync(applicationContext, profile)
        return Result.success()
    }

    companion object {
        const val KEY = "kind"
    }
}

private fun show(context: Context, kind: String) {
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(CHANNEL) == null) {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Recordatorios de la guía", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
    val (title, body) = when {
        kind.contains("rutina") -> "Rutina de hoy" to "Tu lista personal sigue en Guía Heartopia."
        kind.contains("clima") -> "Clima y recursos" to "Revisa el calendario. La hora del servidor sigue pendiente."
        else -> "Eventos" to "Hay fechas citadas por las guías. El cierre de algunas sigue sin contrastar."
    }
    val notification = NotificationCompat.Builder(context, CHANNEL)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .build()
    manager.notify(kind.hashCode(), notification)
}
