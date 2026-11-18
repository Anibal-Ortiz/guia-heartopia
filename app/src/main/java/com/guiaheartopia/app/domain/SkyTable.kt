package com.guiaheartopia.app.domain

import java.time.LocalDate

/**
 * Tabla de clima ya parseada. Vive en domain para que las capas de clima no
 * dependan del módulo de datos (expuesta además como `data.SkyTable` mediante
 * typealias para no romper las firmas públicas existentes).
 */
data class SkyTable(
    val reviewedOn: String,
    val patch: String,
    val days: Map<LocalDate, SkyDay>,
) {
    fun on(date: LocalDate): SkyDay? = days[date]

    fun reviewLabel(): String {
        val parts = reviewedOn.split("-")
        val shown = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else reviewedOn
        return "Clima revisado el $shown; puede cambiar con actualizaciones del juego"
    }
}
