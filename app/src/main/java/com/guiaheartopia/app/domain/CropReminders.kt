package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object CropReminders {
    fun notices(crops: List<ActiveCrop>, zone: ZoneId, now: ZonedDateTime): List<PlannedNotice> =
        crops.mapNotNull { crop ->
            val ready = ZonedDateTime.ofInstant(Instant.ofEpochMilli(crop.readyAtEpochMilli), zone)
            if (!ready.isAfter(now)) return@mapNotNull null
            PlannedNotice(
                id = ReminderIds.crop(crop.siembraId, crop.plantedAtEpochMilli),
                fireAt = ready,
                title = "Cosecha lista",
                body = "Ya puedes cosechar ${crop.nombre}.",
            )
        }
}