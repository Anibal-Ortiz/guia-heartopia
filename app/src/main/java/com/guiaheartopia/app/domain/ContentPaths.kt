package com.guiaheartopia.app.domain

/** Normalización común de rutas de imagen locales ( CreatureImagePaths / AficionesImagePaths ). */
object ContentPaths {
    /**
     * Las imágenes anteriores ya no se usan: mientras no se publiquen las nuevas,
     * las fichas muestran el dibujo genérico incluso si quedan archivos descargados.
     */
    const val IMAGES_ENABLED = false

    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return null
        return raw.removePrefix("asset:").removePrefix("content:").removePrefix("/")
    }
}
