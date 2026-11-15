package com.guiaheartopia.app.data

import android.content.Context
import com.guiaheartopia.app.domain.AficionesContent
import com.guiaheartopia.app.domain.FlorEntry
import com.guiaheartopia.app.domain.RecetaEntry
import com.guiaheartopia.app.domain.SiembraEntry

data class AficionesBundle(
    val siembras: List<SiembraEntry>,
    val flores: List<FlorEntry>,
    val recetas: List<RecetaEntry>,
) {
    val huertoReady: Boolean get() = siembras.isNotEmpty() || flores.isNotEmpty()
    val cocinaReady: Boolean get() = recetas.isNotEmpty()
}

object AficionesCatalog {
    fun load(context: Context): AficionesBundle {
        val library = AndroidContentLibrary(context)
        val siembras = AficionesContent.parseSiembras(
            library.effectiveBytes(AficionesContent.siembrasPath)?.decodeToString(),
        )
        val flores = AficionesContent.parseFlores(
            library.effectiveBytes(AficionesContent.floresPath)?.decodeToString(),
        )
        val recetas = AficionesContent.parseRecetas(
            library.effectiveBytes(AficionesContent.recetasPath)?.decodeToString(),
        )
        return AficionesBundle(siembras, flores, recetas)
    }
}