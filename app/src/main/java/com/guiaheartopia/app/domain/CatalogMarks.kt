package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.domain.Marks as SharedMarks

object CatalogMarks {
    /**
     * Las criaturas del juego base llevan etiquetas de mecánica ("Pesca marina",
     * "Atraer Bichos", "Cola de Sirena", "Nido de las Aves", río/lago/mar, común…). El contenido
     * de la app las guarda en el campo `event`, pero NO son eventos de duración limitada:
     * esas criaturas sí son certificables. Solo se excluyen los eventos reales.
     */
    val BaseGameTags: Set<String> = listOf(
        "común", "comun", "common", "comum",
        "río", "rio", "river", "lago", "lake", "océano", "oceano", "mar", "sea",
        "cascada", "waterfall", "secreto", "secret",
        "pesca marina", "marine fishing", "sea fishing", "pesca marinha", "pesca no mar",
        "atraer bichos", "attract bugs", "atrair insetos", "bug attractant", "bait the insects",
        "cola de sirena", "mermaid tail", "cauda de sereia", "mermaid fish attractor", "atrativo de sereia",
        "nido de las aves", "bird nest", "ninho de passaro", "ninho de passaros",
        "nest of hundreds", "revoada ao ninho",
        "freshwater", "saltwater", "agua dulce", "agua salada",
    ).map(TextMatch::fold).toSet()

    fun certifiable(category: String): Boolean = when (category) {
        "Peces", "Pájaros", "Aves", "Insectos" -> true
        else -> false
    }

    fun certifiable(entry: DexEntry): Boolean {
        if (!certifiable(entry.category)) return false
        val tag = entry.event?.trim().orEmpty()
        return tag.isEmpty() || TextMatch.fold(tag) in BaseGameTags
    }

    data class Marks(val found: Set<String>, val certified: Set<String>)

    fun toggleFound(marks: Marks, id: String): Marks {
        val next = SharedMarks.toggleFound(SharedMarks.Values(marks.found, marks.certified), id)
        return Marks(next.found, next.certified)
    }

    fun toggleCertified(marks: Marks, id: String, category: String): Marks {
        if (!certifiable(category)) return marks
        val next = SharedMarks.toggleCertified(SharedMarks.Values(marks.found, marks.certified), id, true)
        return Marks(next.found, next.certified)
    }

    fun tally(category: String, ids: List<String>, found: Set<String>, certified: Set<String>): CategoryProgress =
        CategoryProgress(
            category = category,
            found = ids.count { it in found },
            total = ids.size,
            certified = ids.count { it in certified },
        )
}
