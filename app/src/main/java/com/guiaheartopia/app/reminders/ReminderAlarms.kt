package com.guiaheartopia.app.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.guiaheartopia.app.domain.PlannedNotice
import com.guiaheartopia.app.domain.ReminderIds

object ReminderAlarms {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            val show = PendingIntent.getActivity(
                app,
                requestCode xor 0x5a5a,
                Intent(app, com.guiaheartopia.app.MainActivity::class.java),
                flags,
            )
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), pending)
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