package com.guiaheartopia.app.pixel

/**
 * Punto de extensión del conversor. La guía manual llama a esta interfaz;
 * se puede sustituir la implementación sin tocar la pantalla.
 */
fun interface ImageToPixel {
    fun convert(src: ArgbImage, palette: List<PaletteColor>, options: ConvertOptions): PixelProject
}
