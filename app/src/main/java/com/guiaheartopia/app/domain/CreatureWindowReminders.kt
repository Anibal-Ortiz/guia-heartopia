package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object CreatureWindowReminders {
    fun notices(alarms: List<CreatureWindowAlarm>, zone: ZoneId, now: ZonedDateTime): List<PlannedNotice> =
        alarms.mapNotNull { alarm ->
            val fire = ZonedDateTime.ofInstant(Instant.ofEpochMilli(alarm.fireAtEpochMilli), zone)
            if (!fire.isAfter(now)) return@mapNotNull null
            PlannedNotice(
                id = ReminderIds.creatureWindow(alarm.creatureId, alarm.fireAtEpochMilli),
                fireAt = fire,
                title = "Criatura disponible",
                body = "Ya puedes buscar ${alarm.creatureName}.",
            )
        }
}