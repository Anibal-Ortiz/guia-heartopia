package com.guiaheartopia.app

import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.CropReminders
import com.guiaheartopia.app.domain.ReminderIds
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CropReminderTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun cropReminderUsesStableIdAndDoesNotCollideWithHobby() {
        val planted = ZonedDateTime.of(2026, 11, 15, 10, 0, 0, 0, zone).toInstant().toEpochMilli()
        val ready = planted + 60_000L
        val crop = ActiveCrop("c1", "tomate", "Tomate", planted, ready)
        val now = ZonedDateTime.of(2026, 11, 15, 9, 0, 0, 0, zone)
        val notice = CropReminders.notices(listOf(crop), zone, now).single()
        val hobbyId = ReminderIds.hobby("oleada-peces", planted)
        assertTrue(notice.id.startsWith("cultivo-"))
        assertTrue(notice.id != hobbyId)
        assertEquals(1, CropReminders.notices(listOf(crop), zone, now).map { ReminderIds.notificationId(it.id) }.distinct().size)
    }
}