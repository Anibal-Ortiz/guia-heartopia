package com.guiaheartopia.app.pixel

import kotlinx.serialization.Serializable

/**
 * Un color de la paleta aproximada.
 * [enabled] en false lo deja fuera de la conversión.
 */
@Serializable
data class PaletteColor(
    val hex: String,
    val name: String,
    val group: Int,
    val shade: Int,
    val enabled: Boolean = true,
) {
    val argb: Int get() = parseHex(hex) ?: 0xFF000000.toInt()

    companion object {
        fun parseHex(s: String): Int? {
            var h = s.trim().removePrefix("#")
            if (h.length == 3) h = h.map { "$it$it" }.joinToString("")
            if (h.length != 6) return null
            return h.toLongOrNull(16)?.let { (0xFF000000 or it).toInt() }
        }

        fun normalizeHex(s: String): String? =
            parseHex(s)?.let { String.format("#%06X", it and 0xFFFFFF) }
    }
}
