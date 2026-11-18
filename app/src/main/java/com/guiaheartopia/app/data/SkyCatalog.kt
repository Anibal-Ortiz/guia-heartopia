package com.guiaheartopia.app.data

import android.content.Context
import com.guiaheartopia.app.domain.SkyDay
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkySlot
import com.guiaheartopia.app.domain.SkyStatus
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Alias público: el modelo vive en domain, la ruta antigua sigue funcionando. */
typealias SkyTable = com.guiaheartopia.app.domain.SkyTable

@Serializable
data class SkyFile(
    val reviewedOn: String = "",
    val patch: String = "",
    val serverZone: String = "UTC",
    val days: List<SkyDayFile> = emptyList(),
)

@Serializable
data class SkyDayFile(
    val date: String,
    val slots: List<SkySlotFile> = emptyList(),
)

@Serializable
data class SkySlotFile(
    val start: String,
    val end: String,
    val kind: String,
    val status: String,
    val place: String? = null,
    val special: String? = null,
    val label: String = "",
)

object SkyCatalog {
    private val json = Json { ignoreUnknownKeys = true }
    private val cached = ContentCache.Slot<SkyTable>()

    /** Caché invalidada automáticamente cuando cambia la revisión del contenido. */
    fun load(context: Context): SkyTable =
        cached.get(ContentCache.key(context)) { loadUncached(context) }

    /** Fuerza una recarga en la próxima lectura. */
    fun invalidate() = cached.clear()

    private fun loadUncached(context: Context): SkyTable {
        val tables = ArrayList<SkyTable>()
        val bundled = context.assets.list("calendar").orEmpty()
            .filter { it.startsWith("clima-") && it.endsWith(".json") }
            .sorted()
        bundled.forEach { name ->
            val raw = runCatching {
                context.assets.open("calendar/$name").bufferedReader().use { it.readText() }
            }.getOrNull() ?: return@forEach
            fromJsonOrNull(raw)?.let { tables += it }
        }
        val overlayDir = File(AndroidContentStore.liveDir(context), "files/calendar")
        overlayDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("clima-") && it.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?.forEach { file ->
                val raw = runCatching { file.readText() }.getOrNull() ?: return@forEach
                fromJsonOrNull(raw)?.let { tables += it }
            }
        if (tables.isEmpty()) return SkyTable("", "", emptyMap())
        return mergeSkyTables(tables)
    }

    /** API pública: un archivo corrupto devuelve una tabla vacía sin lanzar. */
    fun fromJson(raw: String): SkyTable =
        fromJsonOrNull(raw) ?: SkyTable("", "", emptyMap())

    /** Nunca lanza: un archivo corrupto se omite y se conservan los demás días. */
    private fun fromJsonOrNull(raw: String): SkyTable? = runCatching {
        val file = json.decodeFromString(SkyFile.serializer(), raw)
        val days = file.days.mapNotNull { day ->
            val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: return@mapNotNull null
            val slots = day.slots.mapNotNull { slot ->
                val start = runCatching { LocalTime.parse(slot.start) }.getOrNull() ?: return@mapNotNull null
                val endLocal = if (slot.end == "24:00") {
                    LocalTime.MIDNIGHT
                } else {
                    runCatching { LocalTime.parse(slot.end) }.getOrNull() ?: return@mapNotNull null
                }
                SkySlot(
                    start = start,
                    end = endLocal,
                    kind = SkyKind.entries.firstOrNull { it.name.equals(slot.kind, ignoreCase = true) }
                        ?: kindOf(slot.kind),
                    status = when (slot.status) {
                        "acuerdo", "incluido" -> SkyStatus.ACUERDO
                        else -> SkyStatus.SIN_DATOS
                    },
                    place = slot.place,
                    special = slot.special,
                    detail = slot.label,
                    rawKind = slot.kind,
                )
            }
            SkyDay(date = date, slots = slots)
        }
        SkyTable(file.reviewedOn, file.patch, days.associateBy { it.date })
    }.getOrNull()

    private fun kindOf(raw: String): SkyKind = when (raw) {
        "despejado" -> SkyKind.DESPEJADO
        "nublado" -> SkyKind.NUBLADO
        "soleado" -> SkyKind.SOLEADO
        "lluvia" -> SkyKind.LLUVIA
        "arcoiris" -> SkyKind.ARCOIRIS
        "arcoiris_luna" -> SkyKind.ARCOIRIS_LUNA
        "meteoro" -> SkyKind.METEORO
        "nieve" -> SkyKind.NIEVE
        "aurora" -> SkyKind.AURORA
        "calor" -> SkyKind.CALOR
        "petalos" -> SkyKind.PETALOS
        else -> SkyKind.SIN_DATOS
    }
}
