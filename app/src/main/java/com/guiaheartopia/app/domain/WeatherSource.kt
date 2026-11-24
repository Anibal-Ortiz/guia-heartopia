package com.guiaheartopia.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Origen horario de los datos de clima.
 *
 * `calendar/clima.json` publica un único ciclo anual de 366 días. Sus horas son
 * **hora local del servidor América (UTC−5)**, el servidor fijo de esta app.
 * Para mostrar el clima se convierten a instantes con [SERVER_OFFSET] y después
 * a la zona IANA elegida en el perfil ([zoneFor]). Todas las capas
 * (SkyWeatherLocal, SkyWeatherGameDay, SkyWeather y ReminderPlan) comparten esa
 * zona, de modo que el Calendario y la pantalla principal coinciden.
 *
 * Las franjas del juego (Amanecer/Día/Tarde/Noche) sí son UTC fijo y se convierten
 * con [DexSchedule]; eso es independiente de este origen.
 */
object WeatherSource {
    /** Servidor único de la app: América, UTC−5 sin horario de verano. */
    val SERVER_OFFSET: ZoneOffset = ZoneOffset.ofHours(-5)

    const val SERVER_LABEL = "América (UTC−5)"

    /** Id usado por documentos antiguos de `eventos.json` sin `zonaFuente`. */
    const val ZONE_ID = "UTC"

    /** Zona por defecto solo para documentos/eventos que no declaran la suya. */
    val zone: ZoneId = ZoneOffset.UTC

    /** Zona en la que se muestran las horas del clima: la elegida en el perfil. */
    fun zoneFor(viewerZone: ZoneId): ZoneId = viewerZone

    /** Convierte una hora civil del servidor América en un instante. */
    fun serverInstant(serverDate: LocalDate, time: LocalTime): Instant =
        serverDate.atTime(time).atOffset(SERVER_OFFSET).toInstant()

    /** Lee un id IANA con fallback seguro a la zona por defecto. */
    fun zoneOf(raw: String?, fallback: ZoneId = zone): ZoneId =
        if (raw.isNullOrBlank()) fallback else runCatching { ZoneId.of(raw) }.getOrDefault(fallback)
}
