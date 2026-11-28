package com.guiaheartopia.app.share

import java.time.format.DateTimeFormatter
import java.util.Locale

/** Nombres de archivo seguros para `cacheDir/exports`. */
object CommunityImageFilename {
    private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun sanitize(raw: String): String = raw
        .lowercase(Locale.ROOT)
        .map { if (it.isLetterOrDigit()) it else '-' }
        .joinToString("")
        .replace(Regex("-+"), "-")
        .trim('-')
        .ifBlank { "imagen" }

    fun forCard(card: CommunityCard): String = when (card.kind) {
        CommunityImageKind.CALENDAR -> {
            val month = card.month?.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ?: card.selectedDay.date.format(ISO_DATE)
            sanitize("fluorita-liuliu-calendario-$month") + ".png"
        }
        CommunityImageKind.WEATHER ->
            sanitize("clima-fluorita-liuliu-${card.selectedDay.date.format(ISO_DATE)}") + ".png"
    }
}
