package com.guiaheartopia.app.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Ficha de huerto o cocina. Todos los campos menos el nombre son opcionales:
 * si el archivo no los trae, la pantalla no los inventa.
 */
@Serializable
data class HobbyEntry(
    val id: String = "",
    val nombre: String = "",
    val imagen: String? = null,
    val ingredientes: List<String> = emptyList(),
    val tiempoCrecimiento: String? = null,
    val temporada: String? = null,
    val precioVenta: String? = null,
)

@Serializable
private data class HobbyEntryFile(val items: List<HobbyEntry> = emptyList())

object HobbyContent {
    val huertoPaths: List<String> = listOf(AficionesContent.siembrasPath, AficionesContent.floresPath)
    val cocinaPaths: List<String> = listOf(AficionesContent.recetasPath)

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String?): List<HobbyEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        val text = raw.trim()
        return try {
            val parsed = if (text.startsWith("[")) {
                json.decodeFromString<List<HobbyEntry>>(text)
            } else {
                json.decodeFromString(HobbyEntryFile.serializer(), text).items
            }
            parsed.filter { it.nombre.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun search(items: List<HobbyEntry>, query: String): List<HobbyEntry> {
        val q = query.trim()
        if (q.isEmpty()) return items
        return items.filter { entry ->
            TextMatch.contains(entry.nombre, q) ||
                entry.ingredientes.any { TextMatch.contains(it, q) } ||
                (entry.temporada?.let { TextMatch.contains(it, q) } == true)
        }
    }
}
