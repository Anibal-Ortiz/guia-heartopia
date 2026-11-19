package com.guiaheartopia.app.domain

import kotlinx.serialization.Serializable

/** Ficha del catálogo de criaturas. Modelo de dominio (expuesto como `data.DexEntry` por typealias). */
@Serializable
data class DexEntry(
    val id: String,
    val category: String,
    val name: String,
    val nameEn: String = "",
    val location: List<String> = emptyList(),
    val schedule: List<String> = emptyList(),
    val weather: List<String> = emptyList(),
    val event: String? = null,
    val level: Int? = null,
    val shadow: String? = null,
    val imageUrl: String = "",
    val description: String = "",
    val source: String = "",
    val sourceUrl: String = "",
    val extractedOn: String = "",
)
