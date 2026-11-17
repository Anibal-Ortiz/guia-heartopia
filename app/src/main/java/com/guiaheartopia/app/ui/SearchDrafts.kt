package com.guiaheartopia.app.ui

/**
 * Borradores en memoria de los buscadores.
 *
 * El texto de un buscador no puede depender de estados persistidos (DataStore/Flow): sus ecos
 * llegan con retraso y reescriben lo que la persona ya escribió (p. ej. "caracol" termina como
 * "cara" o "carol"). Aquí se guarda el último texto tecleado y sobrevive a recomposiciones o
 * recreaciones de la pantalla.
 */
object SearchDrafts {
    private val values = HashMap<String, String>()

    fun read(key: String): String? = values[key]

    fun write(key: String, text: String) {
        values[key] = text
    }

    fun clear(key: String) {
        values.remove(key)
    }
}
