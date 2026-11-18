package com.guiaheartopia.app.domain

import java.text.Normalizer
import java.util.concurrent.ConcurrentHashMap

/** Compara texto sin acentos ni mayúsculas. El nombre en inglés solo entra en la búsqueda. */
object TextMatch {
    private val marks: Regex = Regex("\\p{Mn}+")
    private val folded = ConcurrentHashMap<String, String>()

    fun fold(value: String): String {
        folded[value]?.let { return it }
        val result = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(marks, "")
            .lowercase()
        // Evita que la caché crezca sin límite con búsquedas del usuario.
        if (folded.size > 4096) folded.clear()
        folded[value] = result
        return result
    }

    fun contains(haystack: String, needle: String): Boolean {
        val query = fold(needle.trim())
        if (query.isEmpty()) return true
        return fold(haystack).contains(query)
    }
}
