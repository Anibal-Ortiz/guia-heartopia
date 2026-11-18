package com.guiaheartopia.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.guiaheartopia.app.domain.PlannedNotice
import com.guiaheartopia.app.domain.ReminderIds

object ReminderAlarms {
    /**
     * Estado para la UI: true cuando se usa alarma inexacta por falta de permiso.
     * Consulta desde ui/ (sin tocar este módulo):
     * ```
     * if (ReminderAlarms.needsExactAlarmPermission(context)) {
     *     ReminderAlarms.exactAlarmSettingsIntent(context)?.let { launcher.launch(it) }
     * }
     * ```
     * Equivale a pedir ACTION_REQUEST_SCHEDULE_EXACT_ALARM con el paquete en `data`.
     */
    fun needsExactAlarmPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(context)

    /** true si el sistema permite setExactAndAllowWhileIdle (o no hace falta permiso). */
    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.applicationContext.getSystemService(AlarmManager::class.java) ?: return false
        return manager.canScheduleExactAlarms()
    }

    /** Intent listo para pedir el permiso; null en versiones donde no aplica. */
    fun exactAlarmSettingsIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            .setData(Uri.parse("package:${context.applicationContext.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun schedule(context: Context, notice: PlannedNotice) {
        val app = context.applicationContext
        val manager = app.getSystemService(AlarmManager::class.java) ?: return
        val trigger = notice.fireAt.toInstant().toEpochMilli()
        if (trigger <= System.currentTimeMillis()) return
        val intent = Intent(app, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_ID, notice.id)
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, notice.title)
            putExtra(ReminderAlarmReceiver.EXTRA_BODY, notice.body)
        }
        val requestCode = ReminderIds.notificationId(notice.id)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getBroadcast(app, requestCode, intent, flags)
        if (!canScheduleExactAlarms(app)) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        } else {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        }
    }

    fun cancel(context: Context, workId: String) {
        val app = context.applicationContext
        val manager = app.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(app, ReminderAlarmReceiver::class.java)
        val requestCode = ReminderIds.notificationId(workId)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getBroadcast(app, requestCode, intent, flags)
        manager.cancel(pending)
        pending.cancel()
    }
}
