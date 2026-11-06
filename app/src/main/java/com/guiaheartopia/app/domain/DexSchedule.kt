package com.guiaheartopia.app.domain

/** Heartodex no publica a qué hora empieza cada tramo. Solo el día completo se puede marcar como «Ahora». */
object DexSchedule {
    val allDay = setOf("Day", "Dawn", "Dusk", "Night")

    fun availableAllDay(schedule: List<String>): Boolean = allDay.all { it in schedule }

    fun scheduleEs(value: String): String = when (value) {
        "Day" -> "Día"
        "Dawn" -> "Amanecer"
        "Dusk" -> "Atardecer"
        "Night" -> "Noche"
        else -> value
    }

    fun weatherEs(value: String): String = when (value) {
        "Sunny" -> "Soleado"
        "Rainy" -> "Lluvia"
        "Rainbow" -> "Arcoíris"
        else -> value
    }
}
