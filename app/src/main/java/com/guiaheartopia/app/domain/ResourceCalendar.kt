package com.guiaheartopia.app.domain

import java.time.LocalDate

enum class DayCertainty(val label: String) {
    CONSENSO("Dos listas coinciden"),
    ESTIMADO("Estimado"),
    SIN_DATOS("Sin datos"),
}

/**
 * Parcela o zona de Liuliu y de la flurita.
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
    private val november = listOf(
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

    /**
     * Diciembre 2026 no está día a día en heartodex.
     * Sale del ciclo de 50 días que Tidebell publica y que coincide con heartodex
     * en octubre y noviembre. Por eso queda marcado como estimado.
     */
    private val december = listOf(
        "7" to "6",
        "6" to "2",
        "11" to "2",
        "2" to "3",
        "12" to "12",
        "4" to "11",
        "12" to "Ruinas",
        "Bosque" to "8",
        "2" to "5",
        "1" to "4",
        "10" to "11",
        "5" to "Ruinas",
        "10" to "8",
        "4" to "8",
        "7" to "2",
        "3" to "2",
        "Bosque" to "2",
        "3" to "1",
        "5" to "1",
        "12" to "4",
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
    )

    fun on(date: LocalDate): ResourceDay {
        val row = when {
            date.year == 2026 && date.monthValue == 11 -> november.getOrNull(date.dayOfMonth - 1)
            date.year == 2026 && date.monthValue == 12 -> december.getOrNull(date.dayOfMonth - 1)
            else -> null
        } ?: return ResourceDay(null, null, DayCertainty.SIN_DATOS, 0)
        val estimated = date.monthValue == 12
        return ResourceDay(
            liuliu = row.first,
            flurita = row.second,
            certainty = if (estimated) DayCertainty.ESTIMADO else DayCertainty.CONSENSO,
            sources = if (estimated) 1 else 2,
        )
    }

    fun label(raw: String?): String = when (raw) {
        null -> "Sin datos"
        "Bosque" -> "Bosque de robles"
        "Ruinas" -> "Ruinas"
        else -> "Parcela $raw"
    }

    /** El 21 de diciembre cae 50 días después del 1 de noviembre. */
    fun decemberFollowsTheNovemberLoop(): Boolean {
        val start = on(LocalDate.of(2026, 11, 1))
        val later = on(LocalDate.of(2026, 12, 21))
        return start.liuliu == later.liuliu && start.flurita == later.flurita
    }
}
