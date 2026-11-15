package com.guiaheartopia.app.domain

object CatalogLabels {
    fun category(raw: String): String = when (raw) {
        "Pájaros" -> "Aves"
        else -> raw
    }
}
