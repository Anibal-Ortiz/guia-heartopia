package com.guiaheartopia.app.domain

data class CocinaIngredienteRef(
    val key: String,
    val displayName: String,
    val idSiembra: String? = null,
    val idReceta: String? = null,
    val nombre: String? = null,
    val imagen: String? = null,
)

data class CocinaIngredienteLine(
    val ref: CocinaIngredienteRef,
    val cantidad: Int? = null,
)

object CocinaIngredients {
    // Comodines que designan peces pero no son entradas concretas del catálogo.
    private val pecesGenericos = setOf("cualquier pez")

    fun linesForReceta(
        entry: RecetaEntry,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): List<CocinaIngredienteLine> {
        val out = ArrayList<CocinaIngredienteLine>()
        entry.ingredientes.forEach { ing ->
            val elegir = ing.elegir
            if (elegir != null && ing.opciones.isNotEmpty()) {
                ing.opciones.forEach { opt ->
                    val ref = refFromOpcion(opt, siembras, recetas)
                    out += CocinaIngredienteLine(ref)
                }
            } else {
                val ref = refFromIngrediente(ing, siembras, recetas)
                out += CocinaIngredienteLine(ref, ing.cantidad)
            }
        }
        return out
    }

    /**
     * Catálogo mostrado en la pestaña "Ingredientes" de Cocina.
     *
     * Solo incluye ingredientes que NO son platos, peces ni parte del huerto
     * (siembras/flores). Las recetas referencian platos con `idReceta` y
     * cultivos con `idSiembra`; esas referencias se descartan. Además, un
     * ingrediente declarado solo por nombre se descarta si ese nombre coincide
     * con un pez, plato, siembra o flor —incluidos comodines como «Cualquier
     * Pez»—, para que no se cuele ninguno de esos catálogos como si fuera un
     * ingrediente suelto.
     *
     * `linesForReceta` se deja intacto: el detalle de una receta sí debe mostrar
     * todos sus ingredientes, incluidos los que son platos o del huerto.
     */
    fun catalogUnique(
        recetas: List<RecetaEntry>,
        siembras: List<SiembraEntry>,
        flores: List<FlorEntry> = emptyList(),
        pecesNombres: Set<String> = emptySet(),
    ): List<CocinaIngredienteRef> {
        val nombresExcluidos = (recetas.map { it.nombre } +
            siembras.map { it.nombre } +
            flores.flatMap { flor -> listOf(flor.nombre) + flor.variantes.mapNotNull { it.nombre } } +
            pecesNombres +
            pecesGenericos)
            .filter { it.isNotBlank() }
            .map { it.trim().lowercase() }
            .toSet()

        val map = LinkedHashMap<String, CocinaIngredienteRef>()
        recetas.forEach { receta ->
            linesForReceta(receta, siembras, recetas).forEach { line ->
                val ref = line.ref
                if (ref.idReceta != null || ref.idSiembra != null) return@forEach
                if (ref.displayName.trim().lowercase() in nombresExcluidos) return@forEach
                map.putIfAbsent(ref.key, ref)
            }
        }
        return map.values.sortedBy { it.displayName.lowercase() }
    }

    fun recetasConIngrediente(
        recetas: List<RecetaEntry>,
        siembras: List<SiembraEntry>,
        key: String,
    ): List<RecetaEntry> = recetas.filter { receta ->
        linesForReceta(receta, siembras, recetas).any { it.ref.key == key }
    }

    fun resolveImagen(
        ref: CocinaIngredienteRef,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): String? {
        ref.imagen?.let { return it }
        ref.idReceta?.let { id ->
            recetas.firstOrNull { it.id == id }?.imagen?.let { return it }
        }
        ref.idSiembra?.let { id ->
            siembras.firstOrNull { it.id == id }?.imagen?.let { return it }
        }
        val nombre = ref.nombre?.takeIf { it.isNotBlank() }
        if (nombre != null) {
            return "cocina/images/ingredientes/${ReminderIds.slug(nombre)}.webp"
        }
        return null
    }

    fun refForIngrediente(
        ing: RecetaIngrediente,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): CocinaIngredienteRef = refFromIngrediente(ing, siembras, recetas)

    fun refForOpcion(
        opt: IngredienteOpcion,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): CocinaIngredienteRef = refFromOpcion(opt, siembras, recetas)

    private fun refFromIngrediente(
        ing: RecetaIngrediente,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): CocinaIngredienteRef {
        val name = AficionesContent.ingredienteNombre(ing, siembras, recetas)
        val key = ing.idReceta?.let { "receta:$it" }
            ?: ing.idSiembra?.let { "siembra:$it" }
            ?: "nombre:${ReminderIds.slug(name)}"
        return CocinaIngredienteRef(
            key = key,
            displayName = name,
            idSiembra = ing.idSiembra,
            idReceta = ing.idReceta,
            nombre = ing.nombre,
        )
    }

    private fun refFromOpcion(
        opt: IngredienteOpcion,
        siembras: List<SiembraEntry>,
        recetas: List<RecetaEntry>,
    ): CocinaIngredienteRef {
        val name = AficionesContent.opcionNombre(opt, siembras, recetas)
        val key = opt.idReceta?.let { "receta:$it" }
            ?: opt.idSiembra?.let { "siembra:$it" }
            ?: "nombre:${ReminderIds.slug(name)}"
        return CocinaIngredienteRef(
            key = key,
            displayName = name,
            idSiembra = opt.idSiembra,
            idReceta = opt.idReceta,
            nombre = opt.nombre,
        )
    }
}