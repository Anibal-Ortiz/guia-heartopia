package com.guiaheartopia.app.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Conversión de hora civil entre zonas IANA. Usa las reglas del sistema (java.time),
 * incluidos los cambios de horario de verano. No usa un desfase UTC fijo.
 *
 * En un hueco (el reloj salta hacia adelante) [LocalDateTime.atZone] ajusta la hora local.
 * En un solapamiento (el reloj retrocede) elige el desfase anterior, el de java.time.
 */
object WeatherTime {
    fun convert(originLocal: LocalDateTime, originZone: ZoneId, viewerZone: ZoneId): ZonedDateTime =
        originLocal.atZone(originZone).withZoneSameInstant(viewerZone)
}

enum class WeatherKind(val label: String) {
    SUN("Sol"),
    RAIN("Lluvia"),
    CLOUD("Nubes"),
    SNOW("Nieve"),
    STORM("Tormenta"),
    FOG("Niebla"),
    UNVERIFIED("Sin confirmar"),
}

data class WeatherOrigin(
    val id: String,
    val name: String,
    val zoneId: String,
    val note: String,
)

/** Nombre mostrado tal cual. No se reinterpreta sin una fuente fiable. */
data class ResourceSighting(
    val key: String,
    val displayName: String,
    val location: String?,
    val appearance: String?,
    val verified: Boolean,
)

data class WeatherSlot(
    val id: String,
    val originId: String,
    val originLocal: LocalDateTime,
    val kind: WeatherKind,
    val summary: String,
    val verified: Boolean,
    val sightings: List<ResourceSighting>,
)

data class PlacedSlot(
    val slot: WeatherSlot,
    val originZone: ZoneId,
    val local: ZonedDateTime,
    val dayShifted: Boolean,
) {
    val localDate: LocalDate get() = local.toLocalDate()
}

object WeatherCalendar {
    fun place(
        slots: List<WeatherSlot>,
        origins: List<WeatherOrigin>,
        viewerZone: ZoneId,
        originId: String,
    ): List<PlacedSlot> {
        val origin = origins.firstOrNull { it.id == originId } ?: return emptyList()
        val originZone = ZoneId.of(origin.zoneId)
        return slots.filter { it.originId == origin.id }
            .map { slot ->
                val local = WeatherTime.convert(slot.originLocal, originZone, viewerZone)
                PlacedSlot(
                    slot = slot,
                    originZone = originZone,
                    local = local,
                    dayShifted = local.toLocalDate() != slot.originLocal.toLocalDate(),
                )
            }
            .sortedBy { it.local.toInstant() }
    }

    fun onDate(placed: List<PlacedSlot>, date: LocalDate): List<PlacedSlot> =
        placed.filter { it.localDate == date }
}

object ResourceKeys {
    const val FLURITA = "flurita"
    const val LIULIU = "Liuliu"
}
