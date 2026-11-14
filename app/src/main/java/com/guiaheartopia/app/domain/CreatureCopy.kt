package com.guiaheartopia.app.domain

/**
 * Texto breve escrito para esta guía. No reproduce fichas del juego ni de otras webs.
 * Los nombres de lugar se dejan tal cual, porque son datos.
 */
object CreatureCopy {
    fun visible(description: String): String {
        val text = description.trim()
        if (text.isEmpty() || isFiller(text)) return ""
        return text
    }

    fun isFiller(text: String): Boolean {
        val value = text.trim()
        return value.contains("de la guía") && value.contains("Horario y clima van en los datos")
    }
}

object StoreCopy {
    const val UNOFFICIAL =
        "App no oficial, no afiliada ni respaldada por los creadores de Heartopia."
}
