package com.guiaheartopia.app.ui

import java.util.concurrent.ConcurrentHashMap

/**
 * Borradores en memoria de los buscadores.
 *
 * El texto de un buscador no puede depender de estados persistidos (DataStore/Flow): sus ecos
 * llegan con retraso y reescriben lo que la persona ya escribió (p. ej. "caracol" termina como
 * "cara" o "carol"). Aquí se guarda el último texto tecleado y sobrevive a recomposiciones o
 * recreaciones de la pantalla.
 *
 * Se usa un mapa concurrente con expiración para que un borrador viejo no reaparezca semanas
 * después y para acotar el crecimiento memoria adentro.
 */
object SearchDrafts {
    private data class Entry(val text: String, val atMillis: Long)

    private const val TTL_MS = 30L * 60 * 1000
    private const val MAX_ENTRIES = 64

    private val values = ConcurrentHashMap<String, Entry>()

    fun read(key: String): String? {
        val entry = values[key] ?: return null
        if (System.currentTimeMillis() - entry.atMillis > TTL_MS) {
            values.remove(key, entry)
            return null
        }
        return entry.text
    }

    fun write(key: String, text: String) {
        values[key] = Entry(text, System.currentTimeMillis())
        if (values.size > MAX_ENTRIES) prune()
    }

    fun clear(key: String) {
        values.remove(key)
    }

    fun clearAll() {
        values.clear()
    }

    private fun prune() {
        val now = System.currentTimeMillis()
        values.entries.removeAll { now - it.value.atMillis > TTL_MS }
        if (values.size > MAX_ENTRIES) {
            values.entries.toList()
                .sortedBy { it.value.atMillis }
                .take(values.size - MAX_ENTRIES)
                .forEach { values.remove(it.key, it.value) }
        }
    }
}
