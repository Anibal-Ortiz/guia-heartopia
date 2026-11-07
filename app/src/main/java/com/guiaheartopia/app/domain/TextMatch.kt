package com.guiaheartopia.app.domain

import java.text.Normalizer

/** Compara texto sin acentos ni mayúsculas. El nombre en inglés solo entra en la búsqueda. */
object TextMatch {
    fun fold(value: String): String {
        val stripped = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return stripped.lowercase()
    }

    fun contains(haystack: String, needle: String): Boolean {
        val query = fold(needle.trim())
        if (query.isEmpty()) return true
        return fold(haystack).contains(query)
    }
}
