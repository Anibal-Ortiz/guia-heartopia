package com.guiaheartopia.app.domain

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
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

data class CalendarEventRow(
    val id: String,
    val title: String,
    val detail: String?,
)

@Serializable
data class EventosDocument(
    val zonaFuente: String = WeatherSource.ZONE_ID,
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
 * Avisos de las próximas 48 h. El calendario de clima declara `reloj-local`: sus
 * horas son hora civil de la zona elegida por el jugador. Solo avisa lluvia,
 * arcoíris y meteoritos, 30 min antes del inicio. Los eventos usan
 * [EventosDocument.zonaFuente] y java.time para convertir a la zona del usuario.
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
        viewerZone: ZoneId,
        now: ZonedDateTime,
        within: Duration = horizon,
        sourceZone: ZoneId? = null,
    ): List<PlannedNotice> {
        // El JSON de clima es hora civil de la zona elegida, no UTC.
        val source = sourceZone ?: WeatherSource.zoneFor(viewerZone)
        val until = now.plus(within)
        val spans = ArrayList<ClimateSpan>()
        val first = now.toLocalDate().minusDays(1)
        val last = until.toLocalDate().plusDays(1)
        for (day in days) {
            if (day.date.isBefore(first) || day.date.isAfter(last)) continue
            for (slot in day.slots) {
                if (!alertsClimate(slot)) continue
                val (start, end) = serverRange(day.date, slot, source, viewerZone)
                if (!end.toInstant().isAfter(now.toInstant())) continue
                if (!start.toInstant().isBefore(until.plusMinutes(30).toInstant())) continue
                spans += ClimateSpan(alertLabel(slot), start, end)
            }
        }
        return merge(spans).mapNotNull { span ->
            val fire = span.start.minusMinutes(30)
            // Mientras el tramo no haya empezado, el aviso sigue en el plan.
            // Si ya pasó la hora de avisar y WorkManager aún no lo entregó,
            // reprogramar no debe cancelarlo.
            if (!span.start.toInstant().isAfter(now.toInstant())) return@mapNotNull null
            if (fire.toInstant().isAfter(until.toInstant())) return@mapNotNull null
            val localStart = span.start.toLocalTime().format(clock)
            val startEpoch = span.start.toInstant().toEpochMilli()
            PlannedNotice(
                id = ReminderIds.weather(span.label, startEpoch),
                fireAt = fire.withZoneSameInstant(viewerZone),
                title = "Clima",
                body = "En 30 min: ${span.label} ($localStart)",
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
        val name = document?.zonaFuente?.takeIf { it.isNotBlank() } ?: WeatherSource.ZONE_ID
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
        // Los avisos de eventos solo se generan si el usuario los activó.
        if (!prefs.eventsEnabled) return emptyList()
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
            val start = parseStamp(event.inicio, source)?.atZone(source)?.withZoneSameInstant(user)
            val end = parseStamp(event.fin, source)?.atZone(source)?.withZoneSameInstant(user)
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

    /**
     * Lo que Inicio mostraba de eventos.json: temporadas en curso con el tiempo
     * que falta, y el resto de eventos solo con los campos que trae el archivo.
     */
    fun calendarRows(document: EventosDocument?, user: ZoneId, now: ZonedDateTime): List<CalendarEventRow> {
        val events = document?.eventos.orEmpty()
        if (events.isEmpty()) return emptyList()
        val source = sourceZone(document) ?: return emptyList()
        val active = activeSeasons(document, user, now).associateBy { it.id }
        val rows = events.mapNotNull { event ->
            if (event.nombre.isBlank()) return@mapNotNull null
            val key = event.id.ifBlank { event.nombre }
            val season = active[key]
            val detail = when {
                season != null -> season.remaining
                event.tipo == "temporada" -> formatEventSpan(event, user, source)
                event.tipo == "aficion" && event.horas.isNotEmpty() -> event.horas.joinToString(", ")
                !event.inicio.isNullOrBlank() && !event.fin.isNullOrBlank() -> formatEventSpan(event, user, source)
                !event.inicio.isNullOrBlank() -> formatEventSpan(event, user, source)
                !event.fin.isNullOrBlank() -> formatEventSpan(event, user, source)
                else -> null
            }
            CalendarEventRow(id = key, title = event.nombre, detail = detail)
        }
        return rows.sortedWith(
            compareByDescending<CalendarEventRow> { it.id in active }
                .thenBy { it.title },
        )
    }

    fun eventCalendarDays(document: EventosDocument?, user: ZoneId): Set<LocalDate> {
        val source = sourceZone(document) ?: return emptySet()
        val days = LinkedHashSet<LocalDate>()
        for (event in document?.eventos.orEmpty()) {
            if (event.tipo != "temporada") continue
            val start = parseStamp(event.inicio, source)?.toLocalDate() ?: continue
            val end = parseStamp(event.fin, source)?.toLocalDate() ?: start
            val (from, to) = EventTime.localSpan(start, end, source, user)
            var day = from
            while (!day.isAfter(to)) {
                days += day
                day = day.plusDays(1)
            }
        }
        return days
    }

    fun formatEventSpan(event: RemoteEvent, user: ZoneId, source: ZoneId): String? {
        val start = parseStamp(event.inicio, source)?.atZone(source)?.withZoneSameInstant(user) ?: return null
        val end = parseStamp(event.fin, source)?.atZone(source)?.withZoneSameInstant(user)
        val fmt = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.forLanguageTag("es"))
        return if (end != null) {
            "${start.format(fmt)} – ${end.format(fmt)}"
        } else {
            "Desde ${start.format(fmt)}"
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
        val windowStart = parseStamp(event.inicio, source)?.atZone(source)
        val windowEnd = parseStamp(event.fin, source)?.atZone(source)
        val from = now.withZoneSameInstant(source).toLocalDate().minusDays(1)
        val last = until.withZoneSameInstant(source).toLocalDate().plusDays(1)
        val out = ArrayList<PlannedNotice>()
        var date = from
        while (!date.isAfter(last)) {
            if (!weekly || date.dayOfWeek in allowed) {
                for (hour in event.horas) {
                    val time = parseClock(hour) ?: continue
                    val at = ZonedDateTime.of(date, time, source)
                    if (windowStart != null && at.toInstant().isBefore(windowStart.toInstant())) continue
                    if (windowEnd != null && at.toInstant().isAfter(windowEnd.toInstant())) continue
                    val local = at.withZoneSameInstant(user)
                    val fire = local.minusMinutes(lead.toLong())
                    if (!local.toInstant().isAfter(now.toInstant())) continue
                    if (fire.toInstant().isAfter(until.toInstant())) continue
                    out += PlannedNotice(
                        id = ReminderIds.hobby(event.id, at.toInstant().toEpochMilli()),
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
        val start = parseStamp(event.inicio, source)?.atZone(source)?.withZoneSameInstant(user)
        val end = parseStamp(event.fin, source)?.atZone(source)?.withZoneSameInstant(user)
        if (start != null && inWindow(start, now, until)) {
            out += PlannedNotice(
                id = ReminderIds.seasonStart(event.id),
                fireAt = start,
                title = event.nombre,
                body = "Empieza: ${event.nombre}",
            )
        }
        if (end != null) {
            val warn = end.minusHours(24)
            if (inWindow(warn, now, until)) {
                out += PlannedNotice(
                    id = ReminderIds.seasonDay(event.id),
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

    /**
     * Acepta fecha, fecha+hora local, Instant e OffsetDateTime. Los valores con
     * desfase se convierten a la zona de origen antes de quedarse con la hora civil.
     */
    private fun parseStamp(raw: String?, source: ZoneId = WeatherSource.zone): LocalDateTime? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(raw) }.getOrNull()
            ?: runCatching { LocalDate.parse(raw).atStartOfDay() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(raw).atZoneSameInstant(source).toLocalDateTime() }.getOrNull()
            ?: runCatching { Instant.parse(raw).atZone(source).toLocalDateTime() }.getOrNull()
    }

    /** Hora del archivo; "24:00" significa medianoche del día siguiente. */
    private fun parseClock(raw: String): LocalTime? = when (raw.trim()) {
        "24:00", "24:00:00" -> LocalTime.MIDNIGHT
        else -> runCatching { LocalTime.parse(raw.trim()) }.getOrNull()
    }

    fun alertsClimate(slot: SkySlot): Boolean {
        val raw = slot.rawKind.trim().lowercase(Locale.ROOT)
        if (slot.kind == SkyKind.LLUVIA || raw == "lluvia") return true
        if (slot.kind == SkyKind.ARCOIRIS || slot.kind == SkyKind.ARCOIRIS_LUNA || raw.contains("arcoiris")) return true
        if (slot.kind == SkyKind.METEORO || raw == "meteoro") return true
        return false
    }

    fun alertLabel(slot: SkySlot): String = when {
        slot.kind == SkyKind.METEORO || slot.rawKind.equals("meteoro", ignoreCase = true) ->
            "Lluvia de meteoritos"
        slot.kind == SkyKind.ARCOIRIS || slot.kind == SkyKind.ARCOIRIS_LUNA -> "Arcoíris"
        slot.kind == SkyKind.LLUVIA -> "Lluvia"
        else -> slot.kind.label
    }

    fun serverRange(
        date: LocalDate,
        slot: SkySlot,
        sourceZone: ZoneId,
        viewerZone: ZoneId,
    ): Pair<ZonedDateTime, ZonedDateTime> {
        val startServer = ZonedDateTime.of(date, slot.start, sourceZone)
        val endServer = if (slot.end == LocalTime.MIDNIGHT || !slot.end.isAfter(slot.start)) {
            ZonedDateTime.of(date.plusDays(1), LocalTime.MIDNIGHT, sourceZone)
        } else {
            ZonedDateTime.of(date, slot.end, sourceZone)
        }
        return startServer.withZoneSameInstant(viewerZone) to endServer.withZoneSameInstant(viewerZone)
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
