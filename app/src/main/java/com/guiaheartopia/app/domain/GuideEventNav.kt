package com.guiaheartopia.app.domain

import com.guiaheartopia.app.data.ContentCache
import com.guiaheartopia.app.data.DexEntry
import com.guiaheartopia.app.data.CreatureCatalog

object GuideEventNav {
    private val cachedTab = ContentCache.Slot<Int>()
    fun dexTabIndex(
        events: EventosDocument?,
        catalog: List<DexEntry>,
        eventKey: String,
    ): Int {
        if (eventKey.isBlank()) return 0
        val event = events?.eventos.orEmpty().firstOrNull {
            it.id.equals(eventKey, ignoreCase = true) || it.nombre.equals(eventKey, ignoreCase = true)
        }
        val meta = event?.categoria?.trim().orEmpty()
        GuideSections.dexCategories.forEachIndexed { index, cat ->
            if (cat.equals(meta, ignoreCase = true) || CatalogLabels.category(cat).equals(meta, ignoreCase = true)) {
                return index
            }
        }
        val haystack = (event?.nombre ?: eventKey) + " " + eventKey
        when {
            haystack.contains("insecto", ignoreCase = true) ->
                return GuideSections.dexCategories.indexOf("Insectos").coerceAtLeast(0)
            haystack.contains("ave", ignoreCase = true) || haystack.contains("pájaro", ignoreCase = true) ->
                return GuideSections.dexCategories.indexOf("Pájaros").coerceAtLeast(0)
            haystack.contains("pez", ignoreCase = true) || haystack.contains("pesca", ignoreCase = true) ->
                return GuideSections.dexCategories.indexOf("Peces").coerceAtLeast(0)
        }
        val id = event?.id?.ifBlank { event.nombre }.orEmpty().ifBlank { eventKey }
        val name = event?.nombre.orEmpty().ifBlank { eventKey }
        val counts = GuideSections.dexCategories.associateWith { cat ->
            catalog.count { it.category == cat && dexEntryMatchesEvent(it, id, name) }
        }
        val best = counts.maxByOrNull { it.value }?.key ?: "Peces"
        return GuideSections.dexCategories.indexOf(best).coerceAtLeast(0)
    }

    /** Cacheada por revisión de contenido, contexto y clave de evento. */
    fun dexTabIndexFromContext(context: android.content.Context, eventKey: String): Int {
        val app = context.applicationContext
        val key = Triple(ContentCache.generation(), app, eventKey)
        return cachedTab.get(key) {
            val raw = com.guiaheartopia.app.data.AndroidContentLibrary(context)
                .effectiveBytes("calendar/eventos.json")
                ?.decodeToString()
            val events = ReminderPlan.parseEvents(raw)
            val catalog = CreatureCatalog.load(context)
            dexTabIndex(events, catalog, eventKey)
        }
    }
}