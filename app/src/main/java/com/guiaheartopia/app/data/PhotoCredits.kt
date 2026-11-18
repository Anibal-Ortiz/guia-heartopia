package com.guiaheartopia.app.data

import android.content.Context
import com.guiaheartopia.app.domain.CatalogLabels
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PhotoCreditEntry(
    val id: String,
    val category: String = "",
    val name: String = "",
    val scientificName: String = "",
    val author: String = "",
    val license: String = "",
    val licenseUrl: String = "",
    val sourcePage: String = "",
    val nota: String = "",
)

@Serializable
data class PhotoCreditsFile(
    val entries: List<PhotoCreditEntry> = emptyList(),
)

object PhotoCredits {
    private val json = Json { ignoreUnknownKeys = true }

    const val PATH = "catalog/creditos.json"

    val sectionOrder = listOf("Peces", "Aves", "Insectos", "Fauna")
    private val cachedById = ContentCache.Slot<Map<String, PhotoCreditEntry>>()

    fun load(context: Context): PhotoCreditsFile? {
        val bytes = AndroidContentLibrary(context).effectiveBytes(PATH) ?: return null
        return runCatching {
            json.decodeFromString(PhotoCreditsFile.serializer(), bytes.decodeToString())
        }.getOrNull()
    }

    fun byId(context: Context): Map<String, PhotoCreditEntry> =
        cachedById.get(ContentCache.key(context)) {
            load(context)?.entries?.associateBy { it.id }.orEmpty()
        }

    fun sectionLabel(category: String): String = CatalogLabels.category(category)

    fun grouped(entries: List<PhotoCreditEntry>): List<Pair<String, List<PhotoCreditEntry>>> {
        val bySection = entries.groupBy { sectionLabel(it.category.ifBlank { "Fauna" }) }
        return sectionOrder.mapNotNull { section ->
            val list = bySection[section]?.sortedBy { it.name.lowercase() } ?: return@mapNotNull null
            if (list.isEmpty()) null else section to list
        }
    }

    fun authorLabel(author: String): String =
        author.trim().ifBlank { "sin atribución requerida" }

    fun caption(entry: PhotoCreditEntry): String =
        "Foto: ${authorLabel(entry.author)} · ${entry.license.ifBlank { "—" }}"
}
