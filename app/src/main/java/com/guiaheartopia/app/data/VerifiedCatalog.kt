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
            summary = "Las guías en inglés lo llaman Night of Divination. El inicio coincide. El último día no, y la zona del servidor no está confirmada, así que no se mueve de día.",
            start = LocalDate.of(2026, 10, 10),
            end = null,
            verified = false,
            sources = EVENT_SOURCES,
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
        "Bosque" -> "Bosque de robles (F en Tidebell)"
        "Ruinas" -> "Ruinas (R en Tidebell)"
        else -> "Parcela $raw"
    }
}
