package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.DexEntry

object CatalogMarks {
    fun certifiable(category: String): Boolean = when (category) {
        "Peces", "Pájaros", "Aves", "Insectos" -> true
        else -> false
    }

    fun certifiable(entry: DexEntry): Boolean =
        certifiable(entry.category) && entry.event.isNullOrBlank()

    data class Marks(val found: Set<String>, val certified: Set<String>)

    fun toggleFound(marks: Marks, id: String): Marks = if (id in marks.found) {
        marks.copy(found = marks.found - id, certified = marks.certified - id)
    } else {
        marks.copy(found = marks.found + id)
    }

    fun toggleCertified(marks: Marks, id: String, category: String): Marks {
        if (!certifiable(category)) return marks
        return if (id in marks.certified) {
            marks.copy(certified = marks.certified - id)
        } else {
            marks.copy(found = marks.found + id, certified = marks.certified + id)
        }
    }

    fun tally(category: String, ids: List<String>, found: Set<String>, certified: Set<String>): CategoryProgress =
        CategoryProgress(
            category = category,
            found = ids.count { it in found },
            total = ids.size,
            certified = ids.count { it in certified },
        )
}
