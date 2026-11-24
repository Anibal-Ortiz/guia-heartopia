package com.guiaheartopia.app.domain

import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Clasificación de ítems ligados a una temporada/evento.
 *
 * Un ítem es de temporada pasada cuando tiene una etiqueta (evento/temporada) que
 * no corresponde a ninguna temporada activa en este momento. Así se ocultan por
 * defecto los ítems de events ya finalizados aunque sigan en el catálogo remoto.
 */
object SeasonTags {
    fun activeKeys(document: EventosDocument?, user: ZoneId, now: ZonedDateTime): Set<String> =
        ReminderPlan.activeSeasons(document, user, now)
            .flatMap { listOf(it.id, it.name) }
            .map { it.trim().lowercase() }
            .toSet()

    fun isPast(tag: String?, activeKeys: Set<String>): Boolean {
        val value = tag?.trim().orEmpty()
        if (value.isEmpty()) return false
        // "Atraer Bichos", "Pesca marina", "Nido de las Aves"... son etiquetas de mecánica
        // del juego base, no temporadas caducadas: deben seguir visibles por defecto.
        if (TextMatch.fold(value) in CatalogMarks.BaseGameTags) return false
        return value.lowercase() !in activeKeys
    }
}
