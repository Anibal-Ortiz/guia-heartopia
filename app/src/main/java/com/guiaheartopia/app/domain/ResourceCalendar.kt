package com.guiaheartopia.app.domain

import java.time.LocalDate

enum class DayCertainty(val label: String) {
    PUBLICADO(""),
    SIN_DATOS("Sin datos"),
}

/**
 * Parcela o zona de Liuliu y de la fluorita.
 * [liuliu] y [flurita] usan el número de parcela, "Bosque" o "Ruinas".
 * El clima hora a hora no entra: no hay dos fuentes con la misma hora.
 */
data class ResourceDay(
    val liuliu: String?,
    val flurita: String?,
    val certainty: DayCertainty,
    val sources: Int,
)

object ResourceCalendar {
    /** Tabla única de noviembre 2026, compartida con VerifiedCatalog. */
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

    fun on(date: LocalDate): ResourceDay {
        val row = when {
            date.year == 2026 && date.monthValue == 11 -> novemberSpots.getOrNull(date.dayOfMonth - 1)
            else -> null
        } ?: return ResourceDay(null, null, DayCertainty.SIN_DATOS, 0)
        return ResourceDay(
            liuliu = row.first,
            flurita = row.second,
            certainty = DayCertainty.PUBLICADO,
            sources = 1,
        )
    }

    fun label(raw: String?): String = when (raw) {
        null -> "Sin datos"
        "Bosque" -> "Bosque de robles"
        "Ruinas" -> "Ruinas"
        else -> "Parcela $raw"
    }

}
