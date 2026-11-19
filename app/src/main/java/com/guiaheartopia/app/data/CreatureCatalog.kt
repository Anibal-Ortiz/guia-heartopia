package com.guiaheartopia.app.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Alias público: el modelo vive en domain, la ruta antigua sigue funcionando. */
typealias DexEntry = com.guiaheartopia.app.domain.DexEntry

@Serializable
data class DexFile(
    val source: String = "",
    val extractedOn: String = "",
    val note: String = "",
    val entries: List<DexEntry> = emptyList(),
)

object CreatureCatalog {
    private val json = Json { ignoreUnknownKeys = true }
    private val cached = ContentCache.Slot<List<DexEntry>>()

    /** Caché invalidada automáticamente cuando cambia la revisión del contenido. */
    fun load(context: Context): List<DexEntry> =
        cached.get(ContentCache.key(context)) { loadUncached(context) }

    fun invalidate() = cached.clear()

    private fun loadUncached(context: Context): List<DexEntry> {
        val base = runCatching {
            context.assets.open("catalog/creatures.json").bufferedReader().use { reader ->
                json.decodeFromString(DexFile.serializer(), reader.readText()).entries
            }
        }.getOrDefault(emptyList())
        val overlay = AndroidContentStore.overlayFile(context, "catalog/creatures.json")
            ?: return base
        val extra = runCatching {
            json.decodeFromString(DexFile.serializer(), overlay.readText()).entries
        }.getOrDefault(emptyList())
        return mergeCreatures(base, extra)
    }
}
