package com.guiaheartopia.app.pixel

/** Cómo encajar la imagen en la proporción de la cuadrícula. */
enum class FitMode(val label: String) {
    CROP("Recortar"),
    FIT("Encajar"),
    STRETCH("Estirar"),
}

data class ConvertOptions(
    val cols: Int,
    val rows: Int,
    val dithering: Boolean = false,
    /** 0 = sin límite. Si es mayor, se quedan los colores de la paleta más usados. */
    val maxColors: Int = 0,
    val fitMode: FitMode = FitMode.CROP,
    /** Píxeles transparentes = casilla vacía. */
    val transparentAsEmpty: Boolean = true,
    val borderColorArgb: Int = 0xFFFFFFFF.toInt(),
    /** true = casilla incluida. null = todas. Reservado para un recorte futuro. */
    val mask: BooleanArray? = null,
    val label: String = "${cols}×$rows",
    /** Fondo blanco conectado al borde = casilla vacía. */
    val skipWhiteBackground: Boolean = true,
    /** Cada casilla toma el color que más se repite, para no mezclar las líneas. */
    val crispLines: Boolean = true,
    /**
     * Rectángulo fuente (x0, y0, x1, y1) que sustituye a [fitMode] si no es null.
     * Puede salirse de la imagen; fuera se usa [borderColorArgb].
     */
    val srcRect: FloatArray? = null,
)
