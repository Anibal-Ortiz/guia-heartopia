package com.guiaheartopia.app.domain

import java.time.ZoneId

data class CountryZone(val zoneId: String, val label: String)

data class Country(
    val id: String,
    val iso: String,
    val name: String,
    val zones: List<CountryZone>,
)

/**
 * Países frecuentes para el calendario. La lista vive en un solo sitio para ampliarla.
 * Las zonas son identificadores IANA reales; no son servidores del juego.
 */
object Countries {
    val chile = Country(
        id = "CL",
        iso = "CL",
        name = "Chile",
        zones = listOf(
            CountryZone("America/Santiago", "Chile continental"),
            CountryZone("America/Punta_Arenas", "Magallanes"),
            CountryZone("Pacific/Easter", "Rapa Nui"),
        ),
    )

    val all: List<Country> = listOf(
        chile,
        country("AR", "Argentina", "America/Argentina/Buenos_Aires" to "Buenos Aires"),
        country("PE", "Perú", "America/Lima" to "Lima"),
        country("CO", "Colombia", "America/Bogota" to "Bogotá"),
        country(
            "MX",
            "México",
            "America/Mexico_City" to "Centro",
            "America/Cancun" to "Quintana Roo",
            "America/Mazatlan" to "Pacífico",
            "America/Tijuana" to "Noroeste",
            "America/Hermosillo" to "Sonora",
        ),
        country(
            "BR",
            "Brasil",
            "America/Sao_Paulo" to "São Paulo",
            "America/Manaus" to "Manaus",
            "America/Noronha" to "Fernando de Noronha",
        ),
        country("ES", "España", "Europe/Madrid" to "Península", "Atlantic/Canary" to "Canarias"),
        country(
            "US",
            "Estados Unidos",
            "America/New_York" to "Este",
            "America/Chicago" to "Centro",
            "America/Denver" to "Montaña",
            "America/Los_Angeles" to "Pacífico",
            "Pacific/Honolulu" to "Hawái",
        ),
        country(
            "CA",
            "Canadá",
            "America/Toronto" to "Este",
            "America/Vancouver" to "Pacífico",
            "America/Halifax" to "Atlántico",
        ),
        country("GB", "Reino Unido", "Europe/London" to "Reino Unido"),
        country("FR", "Francia", "Europe/Paris" to "Francia"),
        country("DE", "Alemania", "Europe/Berlin" to "Alemania"),
        country("JP", "Japón", "Asia/Tokyo" to "Japón"),
        country(
            "AU",
            "Australia",
            "Australia/Sydney" to "Este",
            "Australia/Adelaide" to "Centro",
            "Australia/Perth" to "Oeste",
        ),
    )

    fun byId(id: String): Country = all.firstOrNull { it.id == id } ?: chile

    fun zoneLabel(zoneId: String): String =
        all.asSequence().flatMap { it.zones.asSequence() }.firstOrNull { it.zoneId == zoneId }?.label.orEmpty()

    /**
     * País y zona iniciales. Usa el país del idioma o la zona del reloj del dispositivo
     * solo cuando coinciden con esta lista. No consulta ubicación.
     * Si no hay coincidencia fiable, empieza en Chile / America/Santiago.
     */
    fun resolve(localeCountry: String?, deviceZone: ZoneId): Pair<String, String> {
        val iso = localeCountry?.trim()?.uppercase().orEmpty()
        val byIso = all.firstOrNull { it.iso.equals(iso, ignoreCase = true) }
        if (byIso != null) {
            val zone = byIso.zones.firstOrNull { it.zoneId == deviceZone.id }?.zoneId
                ?: byIso.zones.first().zoneId
            return byIso.id to zone
        }
        val byZone = all.firstOrNull { country -> country.zones.any { it.zoneId == deviceZone.id } }
        if (byZone != null) return byZone.id to deviceZone.id
        return chile.id to chile.zones.first().zoneId
    }

    private fun country(iso: String, name: String, vararg zones: Pair<String, String>) = Country(
        id = iso,
        iso = iso,
        name = name,
        zones = zones.map { CountryZone(it.first, it.second) },
    )
}
