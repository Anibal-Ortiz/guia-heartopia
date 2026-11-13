package com.guiaheartopia.app.domain

import java.time.ZoneId

data class CountryZone(val zoneId: String, val label: String) {
    val key: String get() = "$zoneId|$label"
}

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

    val all: List<Country> by lazy {
        americas + listOf(
        country("ES", "España", "Europe/Madrid" to "Península", "Atlantic/Canary" to "Canarias"),
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
    }

    fun byId(id: String): Country = all.firstOrNull { it.id == id } ?: chile

    fun zone(countryId: String, zoneId: String, regionKey: String = ""): CountryZone {
        val country = byId(countryId)
        return country.zones.firstOrNull { it.key == regionKey }
            ?: country.zones.firstOrNull { it.zoneId == zoneId }
            ?: country.zones.first()
    }

    fun label(countryId: String, zoneId: String, regionKey: String = ""): String =
        zone(countryId, zoneId, regionKey).label

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

    private val americas: List<Country> by lazy { listOf(
        country("AG", "Antigua y Barbuda", "America/Antigua" to "Antigua y Barbuda"),
        argentina,
        country("BS", "Bahamas", "America/Nassau" to "Bahamas"),
        country("BB", "Barbados", "America/Barbados" to "Barbados"),
        country("BZ", "Belice", "America/Belize" to "Belice"),
        country("BO", "Bolivia", "America/La_Paz" to "Bolivia"),
        brazil,
        canada,
        chile,
        country("CO", "Colombia", "America/Bogota" to "Colombia"),
        country("CR", "Costa Rica", "America/Costa_Rica" to "Costa Rica"),
        country("CU", "Cuba", "America/Havana" to "Cuba"),
        country("DM", "Dominica", "America/Dominica" to "Dominica"),
        country("EC", "Ecuador", "America/Guayaquil" to "Ecuador continental", "Pacific/Galapagos" to "Galápagos"),
        country("SV", "El Salvador", "America/El_Salvador" to "El Salvador"),
        unitedStates,
        country("GD", "Granada", "America/Grenada" to "Granada"),
        country("GL", "Groenlandia", "America/Nuuk" to "Groenlandia (oeste)", "America/Scoresbysund" to "Ittoqqortoormiit", "America/Thule" to "Qaanaaq"),
        country("GF", "Guayana Francesa", "America/Cayenne" to "Guayana Francesa"),
        country("GT", "Guatemala", "America/Guatemala" to "Guatemala"),
        country("GY", "Guyana", "America/Guyana" to "Guyana"),
        country("HT", "Haití", "America/Port-au-Prince" to "Haití"),
        country("HN", "Honduras", "America/Tegucigalpa" to "Honduras"),
        country("JM", "Jamaica", "America/Jamaica" to "Jamaica"),
        mexico,
        country("NI", "Nicaragua", "America/Managua" to "Nicaragua"),
        country("PA", "Panamá", "America/Panama" to "Panamá"),
        country("PY", "Paraguay", "America/Asuncion" to "Paraguay"),
        country("PE", "Perú", "America/Lima" to "Perú"),
        country("PR", "Puerto Rico", "America/Puerto_Rico" to "Puerto Rico"),
        country("DO", "República Dominicana", "America/Santo_Domingo" to "República Dominicana"),
        country("KN", "San Cristóbal y Nieves", "America/St_Kitts" to "San Cristóbal y Nieves"),
        country("VC", "San Vicente y las Granadinas", "America/St_Vincent" to "San Vicente y las Granadinas"),
        country("LC", "Santa Lucía", "America/St_Lucia" to "Santa Lucía"),
        country("SR", "Surinam", "America/Paramaribo" to "Surinam"),
        country("TT", "Trinidad y Tobago", "America/Port_of_Spain" to "Trinidad y Tobago"),
        country("UY", "Uruguay", "America/Montevideo" to "Uruguay"),
        country("VE", "Venezuela", "America/Caracas" to "Venezuela"),
    )
    }

    private val argentina = country(
        "AR",
        "Argentina",
        "America/Argentina/Buenos_Aires" to "Buenos Aires",
        "America/Argentina/Buenos_Aires" to "Ciudad Autónoma de Buenos Aires",
        "America/Argentina/Catamarca" to "Catamarca",
        "America/Argentina/Cordoba" to "Chaco",
        "America/Argentina/Catamarca" to "Chubut",
        "America/Argentina/Cordoba" to "Córdoba",
        "America/Argentina/Cordoba" to "Corrientes",
        "America/Argentina/Cordoba" to "Entre Ríos",
        "America/Argentina/Cordoba" to "Formosa",
        "America/Argentina/Jujuy" to "Jujuy",
        "America/Argentina/Salta" to "La Pampa",
        "America/Argentina/La_Rioja" to "La Rioja",
        "America/Argentina/Mendoza" to "Mendoza",
        "America/Argentina/Cordoba" to "Misiones",
        "America/Argentina/Salta" to "Neuquén",
        "America/Argentina/Salta" to "Río Negro",
        "America/Argentina/Salta" to "Salta",
        "America/Argentina/San_Juan" to "San Juan",
        "America/Argentina/San_Luis" to "San Luis",
        "America/Argentina/Cordoba" to "Santa Fe",
        "America/Argentina/Rio_Gallegos" to "Santa Cruz",
        "America/Argentina/Cordoba" to "Santiago del Estero",
        "America/Argentina/Tucuman" to "Tucumán",
        "America/Argentina/Ushuaia" to "Tierra del Fuego",
    )

    private val brazil = country(
        "BR",
        "Brasil",
        "America/Rio_Branco" to "Acre",
        "America/Maceio" to "Alagoas",
        "America/Belem" to "Amapá",
        "America/Manaus" to "Amazonas",
        "America/Eirunepe" to "Amazonas (oeste)",
        "America/Bahia" to "Bahía",
        "America/Fortaleza" to "Ceará",
        "America/Sao_Paulo" to "Distrito Federal",
        "America/Sao_Paulo" to "Espírito Santo",
        "America/Sao_Paulo" to "Goiás",
        "America/Fortaleza" to "Maranhão",
        "America/Cuiaba" to "Mato Grosso",
        "America/Campo_Grande" to "Mato Grosso do Sul",
        "America/Sao_Paulo" to "Minas Gerais",
        "America/Belem" to "Pará",
        "America/Santarem" to "Pará (oeste)",
        "America/Fortaleza" to "Paraíba",
        "America/Sao_Paulo" to "Paraná",
        "America/Recife" to "Pernambuco",
        "America/Noronha" to "Fernando de Noronha",
        "America/Fortaleza" to "Piauí",
        "America/Sao_Paulo" to "Río de Janeiro",
        "America/Fortaleza" to "Río Grande del Norte",
        "America/Sao_Paulo" to "Río Grande del Sur",
        "America/Porto_Velho" to "Rondônia",
        "America/Boa_Vista" to "Roraima",
        "America/Sao_Paulo" to "Santa Catarina",
        "America/Sao_Paulo" to "São Paulo",
        "America/Maceio" to "Sergipe",
        "America/Araguaina" to "Tocantins",
    )

    private val canada = country(
        "CA",
        "Canadá",
        "America/Edmonton" to "Alberta",
        "America/Vancouver" to "Columbia Británica",
        "America/Dawson_Creek" to "Columbia Británica (noreste)",
        "America/Winnipeg" to "Manitoba",
        "America/Halifax" to "Nuevo Brunswick",
        "America/St_Johns" to "Terranova",
        "America/Goose_Bay" to "Labrador",
        "America/Halifax" to "Nueva Escocia",
        "America/Yellowknife" to "Territorios del Noroeste",
        "America/Iqaluit" to "Nunavut (este)",
        "America/Rankin_Inlet" to "Nunavut (centro)",
        "America/Cambridge_Bay" to "Nunavut (oeste)",
        "America/Toronto" to "Ontario",
        "America/Winnipeg" to "Ontario (noroeste)",
        "America/Halifax" to "Isla del Príncipe Eduardo",
        "America/Toronto" to "Quebec",
        "America/Regina" to "Saskatchewan",
        "America/Whitehorse" to "Yukón",
    )

    private val mexico = country(
        "MX",
        "México",
        "America/Mexico_City" to "Aguascalientes",
        "America/Tijuana" to "Baja California",
        "America/Mazatlan" to "Baja California Sur",
        "America/Merida" to "Campeche",
        "America/Mexico_City" to "Chiapas",
        "America/Chihuahua" to "Chihuahua",
        "America/Ciudad_Juarez" to "Chihuahua (frontera)",
        "America/Mexico_City" to "Ciudad de México",
        "America/Mexico_City" to "Coahuila",
        "America/Matamoros" to "Coahuila (frontera)",
        "America/Mexico_City" to "Colima",
        "America/Mexico_City" to "Durango",
        "America/Mexico_City" to "Guanajuato",
        "America/Mexico_City" to "Guerrero",
        "America/Mexico_City" to "Hidalgo",
        "America/Mexico_City" to "Jalisco",
        "America/Mexico_City" to "Estado de México",
        "America/Mexico_City" to "Michoacán",
        "America/Mexico_City" to "Morelos",
        "America/Mazatlan" to "Nayarit",
        "America/Bahia_Banderas" to "Nayarit (Bahía de Banderas)",
        "America/Monterrey" to "Nuevo León",
        "America/Mexico_City" to "Oaxaca",
        "America/Mexico_City" to "Puebla",
        "America/Mexico_City" to "Querétaro",
        "America/Cancun" to "Quintana Roo",
        "America/Mexico_City" to "San Luis Potosí",
        "America/Mazatlan" to "Sinaloa",
        "America/Hermosillo" to "Sonora",
        "America/Mexico_City" to "Tabasco",
        "America/Mexico_City" to "Tamaulipas",
        "America/Matamoros" to "Tamaulipas (frontera)",
        "America/Mexico_City" to "Tlaxcala",
        "America/Mexico_City" to "Veracruz",
        "America/Merida" to "Yucatán",
        "America/Mexico_City" to "Zacatecas",
    )

    private val unitedStates = country(
        "US",
        "Estados Unidos",
        "America/Chicago" to "Alabama",
        "America/Anchorage" to "Alaska",
        "America/Adak" to "Alaska (Aleutianas)",
        "America/Phoenix" to "Arizona",
        "America/Denver" to "Arizona (Nación Navajo)",
        "America/Chicago" to "Arkansas",
        "America/Los_Angeles" to "California",
        "America/New_York" to "Carolina del Norte",
        "America/New_York" to "Carolina del Sur",
        "America/Denver" to "Colorado",
        "America/New_York" to "Connecticut",
        "America/Chicago" to "Dakota del Norte",
        "America/Denver" to "Dakota del Norte (oeste)",
        "America/Chicago" to "Dakota del Sur",
        "America/Denver" to "Dakota del Sur (oeste)",
        "America/New_York" to "Delaware",
        "America/New_York" to "Distrito de Columbia",
        "America/New_York" to "Florida",
        "America/Chicago" to "Florida (oeste)",
        "America/New_York" to "Georgia",
        "Pacific/Honolulu" to "Hawái",
        "America/Boise" to "Idaho",
        "America/Los_Angeles" to "Idaho (norte)",
        "America/Chicago" to "Illinois",
        "America/Indiana/Indianapolis" to "Indiana",
        "America/Chicago" to "Indiana (noroeste y suroeste)",
        "America/Chicago" to "Iowa",
        "America/Chicago" to "Kansas",
        "America/Denver" to "Kansas (oeste)",
        "America/Kentucky/Louisville" to "Kentucky (este)",
        "America/Chicago" to "Kentucky (oeste)",
        "America/Chicago" to "Luisiana",
        "America/New_York" to "Maine",
        "America/New_York" to "Maryland",
        "America/New_York" to "Massachusetts",
        "America/Detroit" to "Míchigan",
        "America/Menominee" to "Míchigan (oeste)",
        "America/Chicago" to "Minnesota",
        "America/Chicago" to "Misisipi",
        "America/Chicago" to "Misuri",
        "America/Denver" to "Montana",
        "America/Chicago" to "Nebraska",
        "America/Denver" to "Nebraska (oeste)",
        "America/Los_Angeles" to "Nevada",
        "America/New_York" to "Nueva Jersey",
        "America/New_York" to "Nueva York",
        "America/New_York" to "Nuevo Hampshire",
        "America/Denver" to "Nuevo México",
        "America/New_York" to "Ohio",
        "America/Chicago" to "Oklahoma",
        "America/Los_Angeles" to "Oregón",
        "America/Boise" to "Oregón (este)",
        "America/New_York" to "Pensilvania",
        "America/New_York" to "Rhode Island",
        "America/New_York" to "Tennessee (este)",
        "America/Chicago" to "Tennessee (oeste)",
        "America/Chicago" to "Texas",
        "America/Denver" to "Texas (El Paso)",
        "America/Denver" to "Utah",
        "America/New_York" to "Vermont",
        "America/New_York" to "Virginia",
        "America/New_York" to "Virginia Occidental",
        "America/Los_Angeles" to "Washington",
        "America/Chicago" to "Wisconsin",
        "America/Denver" to "Wyoming",
    )

    private fun country(iso: String, name: String, vararg zones: Pair<String, String>) = Country(
        id = iso,
        iso = iso,
        name = name,
        zones = zones.map { CountryZone(it.first, it.second) },
    )
}
