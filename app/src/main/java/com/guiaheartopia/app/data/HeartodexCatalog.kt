package com.guiaheartopia.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    val source: String = "heartodex",
    val sourceUrl: String = "",
    val extractedOn: String = "",
)

@Serializable
data class DexFile(
    val source: String = "",
    val extractedOn: String = "",
    val note: String = "",
    val entries: List<DexEntry> = emptyList(),
)

object HeartodexCatalog {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): List<DexEntry> {
        val base = context.assets.open("catalog/heartodex.json").bufferedReader().use { reader ->
            json.decodeFromString(DexFile.serializer(), reader.readText()).entries
        }
        val overlay = AndroidContentStore.overlayFile(context, "catalog/creatures.json")
            ?: AndroidContentStore.overlayFile(context, "catalog/heartodex.json")
            ?: return base
        val extra = json.decodeFromString(DexFile.serializer(), overlay.readText()).entries
        return mergeCreatures(base, extra)
    }
}
