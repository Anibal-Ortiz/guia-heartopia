package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.GameEvent
import java.time.LocalDate

/**
 * Solo entra aquí lo que se puede citar. Si dos fuentes no coinciden, el campo queda pendiente.
 *
 * Comidas: coinciden Polygon, Dexerto y heartopia.noobgo.com (inglés).
 * El nombre en español de cada comida es una glosa, no un texto oficial del cliente.
 * Los comederos no coinciden entre Polygon, Dexerto, heartopiawiki.com y heartopiawiki.org.
 *
 * flurita / Liuliu en noviembre 2026: Tidebell y heartodex coinciden día a día.
 * Tidebell llama al recurso «Roaming Oak» y «Fluorite Mine». heartodex habla de
 * «Liuliu Oak» y «Flawless Fluorite». No hay ficha oficial en español que unifique
 * «Liuliu» con el roble, así que el lugar se muestra y el nombre se deja como en la petición.
 */
object VerifiedCatalog {
    const val EXTRACTED = "2026-11-06"
    const val FOOD_SOURCES =
        "Polygon (polygon.com/heartopia-guide-animal-trough-locations-favorite-foods-list), " +
            "Dexerto (dexerto.com/wikis/heartopia/all-animals-favorite-foods-trough-locations) " +
            "y heartopia.noobgo.com/animals. Consultados en noviembre 2026."

    const val SPOT_SOURCES =
        "Tidebell, tidebell.com/oak-and-fluorite, y heartodex.com/en/daily-location. " +
            "Noviembre 2026 coincide en las dos listas. La hora 6:00 la cita solo Tidebell " +
            "y no identifica la zona del servidor."

    const val EVENT_SOURCES =
        "Inicio 10 oct 2026: heartopia.th.gl (Noche de la adivinación / Night of Divination) " +
            "y build-heartopia.com/calendar. El cierre no coincide: 20 nov (th.gl) y 21 nov (build-heartopia)."

    val events: List<GameEvent> = listOf(
        GameEvent(
            id = "noche-adivinacion",
            title = "Noche de la adivinación",
            summary = "Empieza el 10 de octubre de 2026. El cierre no coincide entre las listas, así que se deja el 21 de noviembre.",
            start = LocalDate.of(2026, 10, 10),
            end = LocalDate.of(2026, 11, 21),
            verified = true,
            sources = "heartodex.com/en/events (extracción 2026-11-06): activo del 10 oct al 21 nov 2026.",
        ),
    )

    /** Día de noviembre 2026 → parcela o zona del roble (Liuliu, según heartodex) y de la flurita. */
    val novemberSpots: List<Pair<String, String>> = listOf(
        "2" to "8",
        "8" to "4",
        "4" to "1",
        "8" to "Ruinas",
        "6" to "8",
        "8" to "8",
        "12" to "6",
        "10" to "2",
        "7" to "2",
        "12" to "Ruinas",
        "8" to "5",
        "6" to "11",
        "11" to "11",
        "2" to "2",
        "9" to "12",
        "3" to "4",
        "6" to "11",
        "2" to "7",
        "11" to "9",
        "Bosque" to "4",
        "Bosque" to "4",
        "11" to "7",
        "9" to "5",
        "10" to "3",
        "3" to "7",
        "6" to "7",
        "9" to "1",
        "4" to "5",
        "10" to "9",
        "Bosque" to "11",
    )

    fun novemberSpot(date: LocalDate): Pair<String, String>? {
        if (date.year != 2026 || date.monthValue != 11) return null
        return novemberSpots.getOrNull(date.dayOfMonth - 1)
    }

    fun spotLabel(raw: String): String = when (raw) {
        "Bosque" -> "Bosque de robles"
        "Ruinas" -> "Ruinas"
        else -> "Parcela $raw"
    }
}
