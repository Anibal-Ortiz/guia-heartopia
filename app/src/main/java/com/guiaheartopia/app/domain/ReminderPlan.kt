package com.guiaheartopia.app.domain

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class PlannedNotice(
    val id: String,
    val fireAt: ZonedDateTime,
    val title: String,
    val body: String,
)

data class ActiveSeason(
    val id: String,
    val name: String,
    val remaining: String?,
)

@Serializable
data class EventosDocument(
    val zonaFuente: String = "Etc/GMT+5",
    val servidor: String = "",
    val eventos: List<RemoteEvent> = emptyList(),
)

@Serializable
data class RemoteEvent(
    val id: String = "",
    val tipo: String = "",
    val categoria: String = "",
    val temporadaId: String? = null,
    val nombre: String = "",
    val repeticion: String? = null,
    val dias: List<String> = emptyList(),
    val horas: List<String> = emptyList(),
    val inicio: String? = null,
    val fin: String? = null,
    val duracionMin: Int? = null,
    val avisoMinAntes: Int? = null,
)

/**
 * Avisos de las próximas 48 h. Las horas del clima son las del calendario, puestas
 * en la zona del usuario. El día del servidor que usa la guía empieza a las 08:00
 * de Chile (amanecer; en invierno UTC−3) y acaba a las 08:00 siguientes: un tramo
 * anterior a esa hora sigue siendo de esa fecha del calendario, no se descarta.
 * Los eventos traen hora de servidor fija ([EventosDocument.zonaFuente]) y se
 * convierten con java.time a la zona del usuario.
 */
object ReminderPlan {
    val horizon: Duration = Duration.ofHours(48)
    private val clock: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val json = Json { ignoreUnknownKeys = true }
    private val weekdays = mapOf(
        "lun" to DayOfWeek.MONDAY,
        "mar" to DayOfWeek.TUESDAY,
        "mie" to DayOfWeek.WEDNESDAY,
        "mié" to DayOfWeek.WEDNESDAY,
        "jue" to DayOfWeek.THURSDAY,
        "vie" to DayOfWeek.FRIDAY,
        "sab" to DayOfWeek.SATURDAY,
        "sáb" to DayOfWeek.SATURDAY,
        "dom" to DayOfWeek.SUNDAY,
    )

    fun weatherNotices(
        days: Collection<SkyDay>,
        zone: ZoneId,
        now: ZonedDateTime,
        within: Duration = horizon,
    ): List<PlannedNotice> {
        val until = now.plus(within)
        val spans = ArrayList<ClimateSpan>()
        val first = now.toLocalDate().minusDays(1)
        val last = until.toLocalDate().plusDays(1)
        for (day in days) {
            if (day.date.isBefore(first) || day.date.isAfter(last)) continue
            for (slot in day.slots) {
                if (isDespejado(slot) || isSinDatos(slot)) continue
                val (start, end) = localRange(day.date, slot, zone)
                if (!end.toInstant().isAfter(now.toInstant())) continue
                if (!start.toInstant().isBefore(until.plusMinutes(30).toInstant())) continue
                spans += ClimateSpan(labelOf(slot), start, end)
            }
        }
        return merge(spans).mapNotNull { span ->
            val fire = span.start.minusMinutes(30)
            if (!fire.toInstant().isAfter(now.toInstant())) return@mapNotNull null
            if (fire.toInstant().isAfter(until.toInstant())) return@mapNotNull null
            val range = "${span.start.toLocalTime().format(clock)}–${span.end.toLocalTime().format(clock)}"
            PlannedNotice(
                id = "clima-${span.start.toInstant().toEpochMilli()}-${span.end.toInstant().toEpochMilli()}",
                fireAt = fire,
                title = "Clima",
                body = "En 30 min: ${span.label} ($range)",
            )
        }
    }

    fun parseEvents(raw: String?): EventosDocument? {
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString(EventosDocument.serializer(), raw)
        } catch (_: Exception) {
            null
        }
    }

    fun sourceZone(document: EventosDocument?): ZoneId? {
        val name = document?.zonaFuente?.takeIf { it.isNotBlank() } ?: "Etc/GMT+5"
        return runCatching { ZoneId.of(name) }.getOrNull()
    }

    fun serverToUser(serverLocal: LocalDateTime, source: ZoneId, user: ZoneId): ZonedDateTime =
        serverLocal.atZone(source).withZoneSameInstant(user)

    fun eventNotices(
        document: EventosDocument?,
        prefs: ReminderPrefs,
        user: ZoneId,
        now: ZonedDateTime,
        within: Duration = horizon,
    ): List<PlannedNotice> {
        val source = sourceZone(document) ?: return emptyList()
        val events = document?.eventos.orEmpty()
        val until = now.plus(within)
        val out = ArrayList<PlannedNotice>()
        for (event in events) {
            if (event.id.isBlank() || event.nombre.isBlank()) continue
            when (event.tipo) {
                "aficion" -> if (hobbyEnabled(event.categoria, prefs)) {
                    out += hobbyNotices(event, source, user, now, until)
                }
                "temporada" -> if (prefs.temporadaEnabled) {
                    out += seasonNotices(event, source, user, now, until)
                }
            }
        }
        return out
    }

    fun activeSeasons(document: EventosDocument?, user: ZoneId, now: ZonedDateTime): List<ActiveSeason> {
        val source = sourceZone(document) ?: return emptyList()
        return document?.eventos.orEmpty().mapNotNull { event ->
            if (event.tipo != "temporada" || event.nombre.isBlank()) return@mapNotNull null
            val start = parseStamp(event.inicio)?.atZone(source)?.withZoneSameInstant(user)
            val end = parseStamp(event.fin)?.atZone(source)?.withZoneSameInstant(user)
            if (start == null) return@mapNotNull null
            if (now.toInstant().isBefore(start.toInstant())) return@mapNotNull null
            if (end != null && !now.toInstant().isBefore(end.toInstant())) return@mapNotNull null
            ActiveSeason(
                id = event.id.ifBlank { event.nombre },
                name = event.nombre,
                remaining = end?.let { remaining(now, it) },
            )
        }
    }

    fun remaining(now: ZonedDateTime, end: ZonedDateTime): String {
        val minutes = Duration.between(now.toInstant(), end.toInstant()).toMinutes()
        if (minutes <= 0) return "Termina ahora"
        val days = minutes / (24 * 60)
        val hours = (minutes % (24 * 60)) / 60
        val mins = minutes % 60
        if (days == 1L && hours == 0L && mins == 0L) return "Queda 1 día"
        val parts = ArrayList<String>()
        if (days > 0) parts += if (days == 1L) "1 día" else "$days días"
        if (hours > 0) parts += "$hours h"
        if (days == 0L && mins > 0) parts += "$mins min"
        return "Quedan ${parts.joinToString(" y ")}"
    }

    private fun hobbyNotices(
        event: RemoteEvent,
        source: ZoneId,
        user: ZoneId,
        now: ZonedDateTime,
        until: ZonedDateTime,
    ): List<PlannedNotice> {
        val lead = (event.avisoMinAntes ?: 15).coerceAtLeast(0)
        val weekly = event.repeticion.equals("semanal", ignoreCase = true)
        val allowed = event.dias.mapNotNull { weekdays[it.lowercase(Locale.ROOT)] }.toSet()
        if (weekly && allowed.isEmpty()) return emptyList()
        val windowStart = parseStamp(event.inicio)?.atZone(source)
        val windowEnd = parseStamp(event.fin)?.atZone(source)
        val from = now.withZoneSameInstant(source).toLocalDate().minusDays(1)
        val last = until.withZoneSameInstant(source).toLocalDate().plusDays(1)
        val out = ArrayList<PlannedNotice>()
        var date = from
        while (!date.isAfter(last)) {
            if (!weekly || date.dayOfWeek in allowed) {
                for (hour in event.horas) {
                    val time = runCatching { LocalTime.parse(hour) }.getOrNull() ?: continue
                    val at = ZonedDateTime.of(date, time, source)
                    if (windowStart != null && at.toInstant().isBefore(windowStart.toInstant())) continue
                    if (windowEnd != null && at.toInstant().isAfter(windowEnd.toInstant())) continue
                    val local = at.withZoneSameInstant(user)
                    val fire = local.minusMinutes(lead.toLong())
                    if (!fire.toInstant().isAfter(now.toInstant())) continue
                    if (fire.toInstant().isAfter(until.toInstant())) continue
                    out += PlannedNotice(
                        id = "aficion-${event.id}-${at.toInstant().toEpochMilli()}",
                        fireAt = fire,
                        title = event.nombre,
                        body = "En $lead min: ${event.nombre}",
                    )
                }
            }
            date = date.plusDays(1)
        }
        return out
    }

    private fun seasonNotices(
        event: RemoteEvent,
        source: ZoneId,
        user: ZoneId,
        now: ZonedDateTime,
        until: ZonedDateTime,
    ): List<PlannedNotice> {
        val out = ArrayList<PlannedNotice>()
        val start = parseStamp(event.inicio)?.atZone(source)?.withZoneSameInstant(user)
        val end = parseStamp(event.fin)?.atZone(source)?.withZoneSameInstant(user)
        if (start != null && inWindow(start, now, until)) {
            out += PlannedNotice(
                id = "temporada-inicio-${event.id}",
                fireAt = start,
                title = event.nombre,
                body = "Empieza: ${event.nombre}",
            )
        }
        if (end != null) {
            val warn = end.minusHours(24)
            if (inWindow(warn, now, until)) {
                out += PlannedNotice(
                    id = "temporada-dia-${event.id}",
                    fireAt = warn,
                    title = event.nombre,
                    body = "Queda 1 día",
                )
            }
        }
        return out
    }

    private fun inWindow(moment: ZonedDateTime, now: ZonedDateTime, until: ZonedDateTime): Boolean =
        moment.toInstant().isAfter(now.toInstant()) && !moment.toInstant().isAfter(until.toInstant())

    private fun hobbyEnabled(categoria: String, prefs: ReminderPrefs): Boolean =
        categoria.lowercase(Locale.ROOT)
            .split('|', ',', ' ')
            .filter { it.isNotBlank() }
            .any { part ->
                when (part) {
                    "pesca" -> prefs.pescaEnabled
                    "aves" -> prefs.avesEnabled
                    "insectos" -> prefs.insectosEnabled
                    else -> false
                }
            }

    private fun parseStamp(raw: String?): LocalDateTime? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(raw) }.getOrNull()
            ?: runCatching { LocalDate.parse(raw).atStartOfDay() }.getOrNull()
    }

    private fun isDespejado(slot: SkySlot): Boolean =
        slot.kind == SkyKind.DESPEJADO || slot.rawKind.equals("despejado", ignoreCase = true)

    private fun isSinDatos(slot: SkySlot): Boolean {
        if (slot.kind != SkyKind.SIN_DATOS) return false
        val raw = slot.rawKind.trim()
        return raw.isEmpty() || raw.equals("sin_datos", true) || raw.equals("sin datos", true)
    }

    private fun labelOf(slot: SkySlot): String {
        if (slot.kind != SkyKind.SIN_DATOS) return slot.kind.label
        val raw = slot.rawKind.trim().replace('_', ' ')
        return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    private fun localRange(date: LocalDate, slot: SkySlot, zone: ZoneId): Pair<ZonedDateTime, ZonedDateTime> {
        val start = ZonedDateTime.of(date, slot.start, zone)
        val end = if (slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start)) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, zone)
        } else {
            ZonedDateTime.of(date, slot.end, zone)
        }
        return start to end
    }

    private fun merge(spans: List<ClimateSpan>): List<ClimateSpan> {
        val sorted = spans.sortedBy { it.start.toInstant() }
        val out = ArrayList<ClimateSpan>()
        for (span in sorted) {
            val prev = out.lastOrNull()
            if (prev != null && prev.label == span.label && !span.start.toInstant().isAfter(prev.end.toInstant())) {
                val end = if (span.end.toInstant().isAfter(prev.end.toInstant())) span.end else prev.end
                out[out.lastIndex] = prev.copy(end = end)
            } else {
                out += span
            }
        }
        return out
    }

    private data class ClimateSpan(val label: String, val start: ZonedDateTime, val end: ZonedDateTime)
}
