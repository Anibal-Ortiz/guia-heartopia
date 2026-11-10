package com.guiaheartopia.app.domain

/**
 * Texto breve escrito para esta guía. No reproduce fichas del juego ni de otras webs.
 * Los nombres de lugar se dejan tal cual, porque son datos.
 */
object CreatureCopy {
    fun line(category: String, locations: List<String>): String {
        val where = locations.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
            .ifBlank { "varios puntos del mapa" }
        val kind = when (category) {
            "Peces" -> "Pez de la guía"
            "Pájaros" -> "Ave de la guía"
            "Insectos" -> "Insecto de la guía"
            "Fauna" -> "Animal de la guía"
            else -> "Criatura de la guía"
        }
        return "$kind. Se anota en $where. Horario y clima van en los datos, no en este texto."
    }
}

object StoreCopy {
    const val UNOFFICIAL =
        "App no oficial, no afiliada ni respaldada por los creadores de Heartopia."
}
