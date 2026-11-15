package com.guiaheartopia.app.domain

object AficionesImagePaths {
    private val allowedPrefixes = listOf("huerto/images/", "cocina/images/")

    fun candidates(imagen: String?): List<String> {
        val path = normalize(imagen) ?: return emptyList()
        if (!ContentPackage.isSafePath(path)) return emptyList()
        if (allowedPrefixes.none { path.startsWith(it) }) return emptyList()
        return listOf(path)
    }

    fun normalize(imagen: String?): String? {
        if (imagen.isNullOrBlank()) return null
        if (imagen.startsWith("http://") || imagen.startsWith("https://")) return null
        return imagen.removePrefix("asset:").removePrefix("content:").removePrefix("/")
    }
}