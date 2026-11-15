package com.guiaheartopia.app.domain

/**
 * Identificador estable de un aviso: tipo, evento y hora de inicio.
 * El mismo texto no basta: dos avisos distintos pueden compartir título
 * y el hash del texto reemplazaba la notificación anterior.
 */
object ReminderIds {
    fun weather(label: String, startEpochMilli: Long): String =
        "clima-${slug(label)}-$startEpochMilli"

    fun hobby(eventId: String, startEpochMilli: Long): String =
        "aficion-${slug(eventId)}-$startEpochMilli"

    fun seasonStart(eventId: String): String = "temporada-inicio-${slug(eventId)}"

    fun seasonDay(eventId: String): String = "temporada-dia-${slug(eventId)}"

    fun crop(siembraId: String, plantedAtEpochMilli: Long): String =
        "cultivo-${slug(siembraId)}-$plantedAtEpochMilli"

    /**
     * Id de notificación positivo y estable. No usa el texto visible,
     * así dos avisos a la misma hora no se pisan en la barra.
     */
    fun notificationId(workName: String): Int {
        var hash = 0x811c9dc5.toInt()
        workName.forEach { ch ->
            hash = hash xor ch.code
            hash *= 0x01000193
        }
        val positive = hash and 0x7fffffff
        return if (positive == 0) 1 else positive
    }

    fun slug(raw: String): String =
        raw.lowercase()
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ñ', 'n')
            .map { ch -> if (ch.isLetterOrDigit()) ch else '-' }
            .joinToString("")
            .replace(Regex("-{2,}"), "-")
            .trim('-')
            .ifBlank { "aviso" }
}
