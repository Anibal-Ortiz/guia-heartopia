package com.guiaheartopia.app

import com.guiaheartopia.app.data.SkyTable
import com.guiaheartopia.app.domain.CreatureAppearing
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatureAppearingTest {
  private val zone = ZoneId.of("America/Santiago")

    @Test
    fun emptyScheduleReturnsNull() {
        val next = CreatureAppearing.nextWindow(
            schedule = emptyList(),
            weather = emptyList(),
            sky = SkyTable(reviewedOn = "", patch = "", days = emptyMap()),
            zone = zone,
        )
        org.junit.Assert.assertNull(next)
    }

    @Test
    fun nextWindowIsAfterFromInstant() {
        val day = SkyDay(
            LocalDate.of(2026, 6, 10),
            listOf(slot(6, 22, SkyKind.DESPEJADO)),
        )
        val sky = SkyTable(reviewedOn = "", patch = "", days = mapOf(day.date to day))
        val from = Instant.parse("2026-06-09T12:00:00Z")
        val next = CreatureAppearing.nextWindow(
            schedule = listOf("Dawn", "Day", "Dusk", "Night"),
            weather = listOf("Sunny"),
            sky = sky,
            zone = zone,
            from = from,
        )
        assertNotNull(next)
        assertTrue(next!!.start.toInstant().isAfter(from))
    }

    private fun slot(from: Int, to: Int, kind: SkyKind) = SkySlot(
        start = LocalTime.of(from, 0),
        end = LocalTime.of(to, 0),
        kind = kind,
        status = SkyStatus.ACUERDO,
        rawKind = kind.name.lowercase(),
    )
}