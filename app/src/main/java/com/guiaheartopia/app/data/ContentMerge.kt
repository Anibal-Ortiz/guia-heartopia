package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.SkyDay
import java.time.LocalDate

/**
 * Combina cada ficha campo a campo: el overlay solo pisa los valores no vacíos,
 * así una actualización parcial no borra nombre, horario o imagen del catálogo base.
 */
fun mergeCreatures(base: List<DexEntry>, overlay: List<DexEntry>): List<DexEntry> {
    val byId = LinkedHashMap<String, DexEntry>()
    base.forEach { byId[it.id] = it }
    overlay.forEach { incoming ->
        val current = byId[incoming.id]
        byId[incoming.id] = if (current == null) incoming else mergeEntry(current, incoming)
    }
    return byId.values.toList()
}

private fun mergeEntry(base: DexEntry, over: DexEntry): DexEntry = DexEntry(
    id = base.id,
    category = text(base.category, over.category),
    name = text(base.name, over.name),
    nameEn = text(base.nameEn, over.nameEn),
    location = list(base.location, over.location),
    schedule = list(base.schedule, over.schedule),
    weather = list(base.weather, over.weather),
    event = over.event ?: base.event,
    level = over.level ?: base.level,
    shadow = over.shadow ?: base.shadow,
    imageUrl = text(base.imageUrl, over.imageUrl),
    description = text(base.description, over.description),
    source = text(base.source, over.source),
    sourceUrl = text(base.sourceUrl, over.sourceUrl),
    extractedOn = text(base.extractedOn, over.extractedOn),
)

private fun text(base: String, over: String): String = over.takeIf { it.isNotBlank() } ?: base

private fun list(base: List<String>, over: List<String>): List<String> =
    over.filter { it.isNotBlank() }.ifEmpty { base }

/** El clima descargado pisa la misma fecha del paquete base. Las fechas nuevas se suman. */
fun mergeSkyTables(tables: List<SkyTable>): SkyTable {
    val days = LinkedHashMap<LocalDate, SkyDay>()
    var reviewed = ""
    var patch = ""
    tables.forEach { table ->
        if (table.reviewedOn.isNotBlank()) reviewed = table.reviewedOn
        if (table.patch.isNotBlank()) patch = table.patch
        days.putAll(table.days)
    }
    return SkyTable(reviewed, patch, days)
}
