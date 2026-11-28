package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.GameEvent
import com.guiaheartopia.app.domain.ResourceCalendar
import java.time.LocalDate

/**
 * Solo entra aquí lo que se puede citar. Si dos fuentes no coinciden, el campo queda pendiente.
 *
 * Comidas: coinciden Polygon, Dexerto y heartopia.noobgo.com (inglés).
 * El nombre en español de cada comida es una glosa, no un texto oficial del cliente.
 * Los comederos no coinciden entre Polygon, Dexerto, heartopiawiki.com y heartopiawiki.org.
 *
 * fluorita / Liuliu en noviembre 2026: Tidebell publica el ciclo diario de 50 días.
 * Tidebell llama al recurso «Roaming Oak» y «Fluorite Mine». No hay ficha oficial en
 * español que unifique «Liuliu» con el roble, así que el lugar se muestra y el nombre
 * se deja como en la petición.
 */
object VerifiedCatalog {
    const val EXTRACTED = "2026-11-06"
    const val FOOD_SOURCES =
        "Polygon (polygon.com/heartopia-guide-animal-trough-locations-favorite-foods-list), " +
            "Dexerto (dexerto.com/wikis/heartopia/all-animals-favorite-foods-trough-locations) " +
            "y heartopia.noobgo.com/animals. Consultados en noviembre 2026."

    const val SPOT_SOURCES =
        "Liuliu y fluorita de noviembre de 2026: Tidebell (tidebell.com/oak-and-fluorite), " +
            "ciclo de 50 días consultado en noviembre de 2026. Diciembre se marca como estimado."

    val events: List<GameEvent> = emptyList()

    /** Día de noviembre 2026 → parcela o zona del roble (Liuliu) y de la fluorita. Fuente única en domain. */
    val novemberSpots: List<Pair<String, String>> get() = ResourceCalendar.novemberSpots

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
