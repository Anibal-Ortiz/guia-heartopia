package com.guiaheartopia.app.domain

import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Origen horario de los datos de clima.
 *
 * `calendar/clima-*.json` declara `serverZone: "reloj-local"` y su nota indica que
 * Tidebell aplica las horas a la fecha y hora locales del lector; **no están
 * documentadas como UTC**. Por tanto los relojes del JSON son hora civil de la
 * zona elegida por el jugador. Todas las capas (SkyWeatherLocal, SkyWeatherGameDay,
 * SkyWeather y ReminderPlan) deben usar esa misma zona.
 *
 * Las franjas del juego (Amanecer/Día/Tarde/Noche) sí son UTC fijo y se convierten
 * con [DexSchedule]; eso es independiente de este origen.
 */
object WeatherSource {
    /** Id usado por documentos antiguos de `eventos.json` sin `zonaFuente`. */
    const val ZONE_ID = "UTC"

    /** Zona por defecto solo para documentos/eventos que no declaran la suya. */
    val zone: ZoneId = ZoneOffset.UTC

    /** Zona en la que se leen las horas del JSON de clima: la elegida por el jugador. */
    fun zoneFor(viewerZone: ZoneId): ZoneId = viewerZone

    /** Lee un id IANA con fallback seguro a la zona por defecto. */
    fun zoneOf(raw: String?, fallback: ZoneId = zone): ZoneId =
        if (raw.isNullOrBlank()) fallback else runCatching { ZoneId.of(raw) }.getOrDefault(fallback)
}
