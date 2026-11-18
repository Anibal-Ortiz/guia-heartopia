package com.guiaheartopia.app.domain

/** Alternar marcas de encontrado/certificado, compartido por CatalogMarks y AficionesMarks. */
object Marks {
    fun toggle(set: Set<String>, id: String): Set<String> =
        if (id in set) set - id else set + id

    data class Values(val found: Set<String>, val certified: Set<String>)

    fun toggleFound(marks: Values, id: String): Values = if (id in marks.found) {
        marks.copy(found = marks.found - id, certified = marks.certified - id)
    } else {
        marks.copy(found = marks.found + id)
    }

    fun toggleCertified(marks: Values, id: String, certifiable: Boolean): Values {
        if (!certifiable) return marks
        return if (id in marks.certified) {
            marks.copy(certified = marks.certified - id)
        } else {
            marks.copy(found = marks.found + id, certified = marks.certified + id)
        }
    }
}
