package com.guiaheartopia.app.domain

/** Normalización común de rutas de imagen locales ( CreatureImagePaths / AficionesImagePaths ). */
object ContentPaths {
    /**
     * Las fichas usan las imágenes publicadas en el paquete de contenido
     * (rutas `catalog/images` con extensión .webp). Si el archivo no está descargado se muestra el
     * dibujo genérico de la categoría.
     */
    const val IMAGES_ENABLED = true

    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return null
        return raw.removePrefix("asset:").removePrefix("content:").removePrefix("/")
    }
}
