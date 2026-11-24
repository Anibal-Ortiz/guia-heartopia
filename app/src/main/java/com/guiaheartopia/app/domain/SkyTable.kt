package com.guiaheartopia.app.domain

import java.time.LocalDate
import java.time.ZoneId

/**
 * Tabla de clima ya parseada. Vive en domain para que las capas de clima no
 * dependan del módulo de datos (expuesta además como `data.SkyTable` mediante
 * typealias para no romper las firmas públicas existentes).
 *
 * Normalmente [days] está vacío y el clima se resuelve bajo demanda desde
 * [cycle], convirtiendo la hora del servidor América a la zona del perfil.
 */
data class SkyTable(
    val reviewedOn: String,
    val patch: String,
    val days: Map<LocalDate, SkyDay>,
    val cycle: WeatherCycle? = null,
    val zoneId: ZoneId? = null,
) {
    fun on(date: LocalDate): SkyDay? =
        days[date] ?: cycle?.dayOn(date, zoneId ?: return null)

    fun reviewLabel(): String {
        val parts = reviewedOn.split("-")
        val shown = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else reviewedOn
        return "Clima revisado el $shown; puede cambiar con actualizaciones del juego"
    }
}
