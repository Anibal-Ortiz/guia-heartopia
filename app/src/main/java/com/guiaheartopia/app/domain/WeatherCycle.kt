package com.guiaheartopia.app.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.MonthDay
import java.time.ZoneId

/** Franja de un día del ciclo anual, en hora civil del servidor América. */
data class ServerSlot(
    val start: LocalTime,
    val end: LocalTime,
    val kind: SkyKind,
    val status: SkyStatus,
    val place: String?,
    val special: String?,
    val detail: String,
    val rawKind: String,
)

/**
 * Ciclo anual de 366 días (incluye el 29 de febrero). Resuelve el clima de
 * cualquier fecha local del perfil convirtiendo las horas del servidor América
 * (UTC−5) a la zona IANA elegida. No guarda un archivo por año: el mismo ciclo
 * vale para todos.
 */
class WeatherCycle(
    val days: Map<MonthDay, List<ServerSlot>>,
) {
    private val cache = LinkedHashMap<Pair<LocalDate, ZoneId>, SkyDay?>()

    @Synchronized
    fun dayOn(date: LocalDate, zone: ZoneId): SkyDay? {
        val key = date to zone
        if (cache.containsKey(key)) return cache[key]

        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        val slots = ArrayList<SkySlot>()

        // El desfase entre América (UTC−5) y una zona IANA puede cruzar la
        // medianoche; dos días a cada lado cubren cualquier caso (incluido DST).
        for (delta in -2..2) {
            val serverDate = date.plusDays(delta.toLong())
            val day = days[MonthDay.from(serverDate)] ?: continue
            for (slot in day) {
                val start = WeatherSource.serverInstant(serverDate, slot.start)
                val end = if (slot.end == LocalTime.MIDNIGHT) {
                    WeatherSource.serverInstant(serverDate.plusDays(1), LocalTime.MIDNIGHT)
                } else {
                    WeatherSource.serverInstant(serverDate, slot.end)
                }
                val from = if (start > dayStart) start else dayStart
                val to = if (end < dayEnd) end else dayEnd
                if (!from.isBefore(to)) continue
                val localStart = from.atZone(zone).toLocalTime()
                val localEnd = if (to == dayEnd) LocalTime.MIDNIGHT else to.atZone(zone).toLocalTime()
                slots += SkySlot(
                    start = localStart,
                    end = localEnd,
                    kind = slot.kind,
                    status = slot.status,
                    place = slot.place,
                    special = slot.special,
                    detail = slot.detail,
                    rawKind = slot.rawKind,
                )
            }
        }

        val result = if (slots.isEmpty()) null else SkyDay(date, slots.sortedBy { it.start })
        cache[key] = result
        return result
    }
}
