package com.guiaheartopia.app.domain

/** Normalización común de rutas de imagen locales ( CreatureImagePaths / AficionesImagePaths ). */
object ContentPaths {
    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return null
        return raw.removePrefix("asset:").removePrefix("content:").removePrefix("/")
    }
}
