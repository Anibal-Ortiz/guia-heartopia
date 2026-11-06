package com.guiaheartopia.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class DexEntry(
    val id: String,
    val category: String,
    val name: String,
    val nameEs: String? = null,
    val location: List<String> = emptyList(),
    val schedule: List<String> = emptyList(),
    val weather: List<String> = emptyList(),
    val event: String? = null,
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

    fun load(context: Context): List<DexEntry> =
        context.assets.open("catalog/heartodex.json").bufferedReader().use { reader ->
            json.decodeFromString(DexFile.serializer(), reader.readText()).entries
        }
}
