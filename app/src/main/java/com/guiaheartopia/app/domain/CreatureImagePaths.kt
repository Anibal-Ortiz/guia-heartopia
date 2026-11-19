package com.guiaheartopia.app.domain

/**
 * Rutas locales de una foto de criatura. La fauna a veces llega como
 * wildlife-&lt;id&gt; cuando el id ya empieza por wildlife-.
 */
object CreatureImagePaths {
    fun candidates(imageUrl: String?, id: String = ""): List<String> {
        if (!ContentPaths.IMAGES_ENABLED) return emptyList()
        val primary = normalize(imageUrl)
        val extra = mutableListOf<String>()
        if (id.isNotBlank()) {
            extra += "catalog/images/$id.webp"
            extra += "catalog/images/wildlife-$id.webp"
            if (id.startsWith("wildlife-")) {
                extra += "catalog/images/${id.removePrefix("wildlife-wildlife-")}.webp"
            }
        }
        if (primary != null && "wildlife-wildlife-" in primary) {
            extra += primary.replace("wildlife-wildlife-", "wildlife-")
        }
        return (listOfNotNull(primary) + extra)
            .distinct()
            .filter { ContentPackage.isSafePath(it) && it.startsWith("catalog/images/") }
    }

    fun normalize(imageUrl: String?): String? = ContentPaths.normalize(imageUrl)
}

object GuideSections {
    val dexCategories: List<String> = listOf("Peces", "Pájaros", "Insectos", "Fauna")
}
