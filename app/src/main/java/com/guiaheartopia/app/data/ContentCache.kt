package com.guiaheartopia.app.data

import java.util.concurrent.ConcurrentHashMap

/**
 * Caché en memoria de los catálogos derivados del contenido publicado.
 *
 * La clave incluye una generación que cambia cada vez que se publica contenido
 * ([ContentCache.invalidate]) o cuando la UI llama a `ContentSignals.note`.
 * Así no cambian las APIs públicas de los loaders y la caché se renueva sola
 * cuando cambia la revisión del contenido.
 */
object ContentCache {
    @Volatile
    private var generation: Long = 0L

    private val bytes = ConcurrentHashMap<String, Slot<ByteArray?>>()

    fun generation(): Long = generation

    fun invalidate() {
        generation++
        bytes.clear()
    }

    /** Un valor memoizado mientras la clave no cambie. El compute recibe la clave actual. */
    class Slot<T> {
        @Volatile
        private var key: Any? = null

        @Volatile
        private var value: T? = null

        fun get(key: Any, compute: () -> T): T {
            if (this.key == key) {
                @Suppress("UNCHECKED_CAST")
                return value as T
            }
            return synchronized(this) {
                if (this.key != key) {
                    value = compute()
                    this.key = key
                }
                @Suppress("UNCHECKED_CAST")
                value as T
            }
        }

        fun clear() {
            synchronized(this) {
                key = null
                value = null
            }
        }
    }

    /** Caché de bytes efectivos por ruta, renovada en cada publicación. */
    fun bytes(path: String, compute: () -> ByteArray?): ByteArray? =
        bytes.computeIfAbsent(path) { Slot<ByteArray?>() }.get(generation(), compute)

    fun key(context: android.content.Context): Any =
        generation() to context.applicationContext
}
