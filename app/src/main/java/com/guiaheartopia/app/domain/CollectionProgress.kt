package com.guiaheartopia.app.domain

object CollectionProgress {
    fun byCategory(items: List<Collectible>, found: Set<String>): List<CategoryProgress> =
        items.groupBy { it.category }
            .map { (category, list) ->
                CategoryProgress(
                    category = category,
                    found = list.count { it.id in found },
                    total = list.size,
                )
            }
            .sortedBy { it.category }

    fun overall(items: List<Collectible>, found: Set<String>): Float {
        if (items.isEmpty()) return 0f
        return items.count { it.id in found }.toFloat() / items.size
    }
}

object HobbyProgress {
    fun clamp(level: Int, maxLevel: Int): Pair<Int, Int> {
        val max = maxLevel.coerceAtLeast(1)
        return level.coerceIn(0, max) to max
    }

    fun fraction(level: Int, maxLevel: Int): Float {
        val (lvl, max) = clamp(level, maxLevel)
        return lvl.toFloat() / max
    }
}

object Greeting {
    fun line(hour: Int, name: String): String {
        val who = name.trim().ifEmpty { "viajero" }
        val hello = when (hour) {
            in 5..11 -> "Buenos días"
            in 12..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return "$hello, $who"
    }
}
