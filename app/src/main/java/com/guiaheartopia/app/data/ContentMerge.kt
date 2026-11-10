package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.SkyDay
import java.time.LocalDate

fun mergeCreatures(base: List<DexEntry>, overlay: List<DexEntry>): List<DexEntry> {
    val byId = LinkedHashMap<String, DexEntry>()
    base.forEach { byId[it.id] = it }
    overlay.forEach { byId[it.id] = it }
    return byId.values.toList()
}

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
