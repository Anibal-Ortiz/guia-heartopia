package com.guiaheartopia.app.domain

/** Hábitat para teñir el icono propio de la categoría. No usa imágenes del juego. */
enum class Habitat {
    MAR,
    AGUA,
    BOSQUE,
    RUINAS,
    PUEBLO,
    NIVEL,
}

object CreatureMark {
    fun habitat(locations: List<String>, level: Int?): Habitat {
        val text = locations.joinToString(" ").lowercase()
        return when {
            listOf("mar", "océano", "oceano", "costa", "playa", "muelle", "ballena", "wharf").any { it in text } -> Habitat.MAR
            listOf("río", "rio", "lago", "agua").any { it in text } -> Habitat.AGUA
            listOf("bosque", "roble", "isla", "pino").any { it in text } -> Habitat.BOSQUE
            listOf("ruina", "volcán", "volcan", "cráter", "crater").any { it in text } -> Habitat.RUINAS
            listOf("suburb", "pueblo", "plaza", "pradera", "flor", "residencial", "molino").any { it in text } -> Habitat.PUEBLO
            else -> Habitat.NIVEL
        }.let { found ->
            if (found != Habitat.NIVEL || level == null) found else Habitat.NIVEL
        }
    }
}
