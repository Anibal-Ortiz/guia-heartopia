package com.guiaheartopia.app.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SiembraEntry(
    val id: String = "",
    val nombre: String = "",
    val nivelSiembra: Int = 0,
    val precioSemilla: Int? = null,
    val fuenteSemilla: String? = null,
    val tiempoCrecimientoMinutos: Int = 0,
    val precioVentaEstrellas: List<Int> = emptyList(),
    val categoria: String? = null,
    val evento: String? = null,
    val maestriaCosechas: String? = null,
    val imagen: String? = null,
    val certificable: Boolean? = null,
)

@Serializable
data class FlorVariante(
    val color: String,
    val origen: String,
    val estrellas: Int? = null,
    val precioVenta: Int? = null,
    val id: String? = null,
    val nombre: String? = null,
    val imagen: String? = null,
)

@Serializable
data class FlorEntry(
    val id: String = "",
    val nombre: String = "",
    val nivelSiembra: Int? = null,
    val variantes: List<FlorVariante> = emptyList(),
    val imagen: String? = null,
    val tiempoCrecimientoMinutos: Int? = null,
    val precioSemilla: Int? = null,
    val categoria: String? = null,
)

@Serializable
data class IngredienteOpcion(
    val idSiembra: String? = null,
    val idReceta: String? = null,
    val nombre: String? = null,
)

@Serializable
data class RecetaIngrediente(
    val idSiembra: String? = null,
    val idReceta: String? = null,
    val nombre: String? = null,
    val cantidad: Int? = null,
    val elegir: Int? = null,
    val opciones: List<IngredienteOpcion> = emptyList(),
)

@Serializable
data class RecetaEntry(
    val id: String = "",
    val nombre: String = "",
    val nivelCocina: Int = 0,
    val energiaEstrellas: List<Int> = emptyList(),
    val precioVentaEstrellas: List<Int> = emptyList(),
    val ingredientes: List<RecetaIngrediente> = emptyList(),
    val categoria: String? = null,
    val evento: String? = null,
    val maestria: String? = null,
    val imagen: String? = null,
    val ingredientesCompletos: Boolean = true,
    val ingredientesPendientes: Int = 0,
    val certificable: Boolean? = null,
    val origen: String? = null,
)

@Serializable
private data class SiembraFile(val items: List<SiembraEntry> = emptyList())

@Serializable
private data class FlorFile(val items: List<FlorEntry> = emptyList())

@Serializable
private data class RecetaFile(val items: List<RecetaEntry> = emptyList())

@Serializable
data class ActiveCrop(
    val id: String,
    val siembraId: String,
    val nombre: String,
    val plantedAtEpochMilli: Long,
    val readyAtEpochMilli: Long,
)

object AficionesContent {
    val siembrasPath = "huerto/siembras.json"
    val floresPath = "huerto/flores.json"
    val recetasPath = "cocina/recetas.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun parseSiembras(raw: String?): List<SiembraEntry> =
        parseList(raw, { json.decodeFromString<List<SiembraEntry>>(it) }, { json.decodeFromString<SiembraFile>(it).items }) {
            it.nombre.isNotBlank() && it.id.isNotBlank()
        }

    fun parseFlores(raw: String?): List<FlorEntry> =
        parseList(raw, { json.decodeFromString<List<FlorEntry>>(it) }, { json.decodeFromString<FlorFile>(it).items }) {
            it.nombre.isNotBlank() && it.id.isNotBlank()
        }

    fun parseRecetas(raw: String?): List<RecetaEntry> =
        parseList(raw, { json.decodeFromString<List<RecetaEntry>>(it) }, { json.decodeFromString<RecetaFile>(it).items }) {
            it.nombre.isNotBlank() && it.id.isNotBlank()
        }

    private fun <T> parseList(
        raw: String?,
        decodeArray: (String) -> List<T>,
        decodeObject: (String) -> List<T>,
        keep: (T) -> Boolean,
    ): List<T> {
        if (raw.isNullOrBlank()) return emptyList()
        val text = raw.trim()
        return try {
            val parsed = if (text.startsWith("[")) decodeArray(text) else decodeObject(text)
            parsed.filter(keep)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun formatGrowthMinutes(minutes: Int): String {
        if (minutes <= 0) return "0 min"
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours == 0 -> "$mins min"
            mins == 0 -> "${hours} h"
            else -> "${hours} h ${mins} min"
        }
    }

    fun origenFlorLabel(origen: String): String = when (origen.lowercase()) {
        "tienda" -> "Tienda"
        "cruce" -> "Cruce"
        else -> origen
    }

    fun recetasQueUsanSiembra(recetas: List<RecetaEntry>, siembra: SiembraEntry): List<RecetaEntry> {
        val id = siembra.id
        val nombre = siembra.nombre
        return recetas.filter { receta ->
            receta.ingredientes.any { ing ->
                matchesSiembra(ing, id, nombre) ||
                    ing.opciones.any { opt -> matchesOpcion(opt, id, nombre) }
            }
        }
    }

    private fun matchesSiembra(ing: RecetaIngrediente, id: String, nombre: String): Boolean =
        (ing.idSiembra != null && ing.idSiembra == id) ||
            (ing.nombre != null && TextMatch.contains(nombre, ing.nombre))

    private fun matchesOpcion(opt: IngredienteOpcion, id: String, nombre: String): Boolean =
        (opt.idSiembra != null && opt.idSiembra == id) ||
            (opt.nombre != null && TextMatch.contains(nombre, opt.nombre))

    fun ingredienteLine(
        ing: RecetaIngrediente,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry> = emptyList(),
    ): String {
        val elegir = ing.elegir
        if (elegir != null && ing.opciones.isNotEmpty()) {
            val nombres = ing.opciones.map { opcionNombre(it, siembras, recetas) }.filter { it.isNotBlank() }
            return "Elige $elegir de los siguientes: ${nombres.joinToString(", ")}"
        }
        val qty = ing.cantidad?.let { " ×$it" }.orEmpty()
        return "${ingredienteNombre(ing, siembras, recetas)}$qty".trim()
    }

    fun ingredienteNombre(
        ing: RecetaIngrediente,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): String = ing.idSiembra?.let { siembras.firstOrNull { s -> s.id == it }?.nombre }
        ?: ing.idReceta?.let { recetas.firstOrNull { r -> r.id == it }?.nombre }
        ?: ing.nombre.orEmpty()

    fun opcionNombre(
        opt: IngredienteOpcion,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): String = opt.idSiembra?.let { siembras.firstOrNull { s -> s.id == it }?.nombre }
        ?: opt.idReceta?.let { recetas.firstOrNull { r -> r.id == it }?.nombre }
        ?: opt.nombre.orEmpty()

    fun siembraPorId(siembras: List<SiembraEntry>, id: String?): SiembraEntry? =
        id?.let { want -> siembras.firstOrNull { it.id == want } }

}

object AficionesMarks {
    data class Marks(val found: Set<String>, val certified: Set<String>)

    fun toggle(set: Set<String>, id: String): Set<String> =
        if (id in set) set - id else set + id

    fun toggleFound(marks: Marks, id: String): Marks =
        if (id in marks.found) marks.copy(found = marks.found - id, certified = marks.certified - id)
        else marks.copy(found = marks.found + id)

    fun toggleCertified(marks: Marks, id: String, certifiable: Boolean): Marks {
        if (!certifiable) return marks
        return if (id in marks.certified) {
            marks.copy(certified = marks.certified - id)
        } else {
            marks.copy(found = marks.found + id, certified = marks.certified + id)
        }
    }

    fun certifiableSiembra(entry: SiembraEntry): Boolean =
        entry.certificable != false &&
            !entry.categoria.equals("Evento", ignoreCase = true) &&
            entry.evento.isNullOrBlank()

    fun certifiableFlor(entry: FlorEntry): Boolean =
        !entry.categoria.equals("Evento", ignoreCase = true)

    fun certifiableReceta(entry: RecetaEntry): Boolean =
        entry.certificable != false &&
            !entry.categoria.equals("Evento", ignoreCase = true) &&
            entry.evento.isNullOrBlank()
}