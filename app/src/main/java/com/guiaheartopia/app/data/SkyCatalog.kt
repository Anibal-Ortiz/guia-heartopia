package com.guiaheartopia.app.data

import android.content.Context
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.domain.ServerSlot
import com.guiaheartopia.app.domain.SkyKind
import com.guiaheartopia.app.domain.SkyStatus
import com.guiaheartopia.app.domain.SkyTable
import com.guiaheartopia.app.domain.WeatherCycle
import java.io.File
import java.time.LocalTime
import java.time.MonthDay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Alias público: el modelo vive en domain, la ruta antigua sigue funcionando. */
typealias SkyTable = com.guiaheartopia.app.domain.SkyTable

@Serializable
data class SkyFile(
    val reviewedOn: String = "",
    val patch: String = "",
    val serverZone: String = "UTC",
    val hourBasis: String = "",
    val days: List<SkyDayFile> = emptyList(),
)

@Serializable
data class SkyDayFile(
    val date: String = "",
    val monthDay: String = "",
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

/**
 * Único origen del clima: el archivo publicado `calendar/clima.json`.
 *
 * No se lee nada de `assets`: sin contenido descargado la tabla queda vacía y
 * la interfaz muestra «Sin datos de clima». El ciclo se resuelve a la zona
 * IANA del perfil, la misma que usa la pantalla principal.
 */
object SkyCatalog {
    private const val PATH = "calendar/clima.json"
    private val json = Json { ignoreUnknownKeys = true }
    private val cached = ContentCache.Slot<SkyTable>()

    fun load(context: Context, zoneId: String): SkyTable =
        cached.get(ContentCache.key(context) to zoneId) { loadUncached(context, zoneId) }

    /** Fuerza una recarga en la próxima lectura. */
    fun invalidate() = cached.clear()

    private fun loadUncached(context: Context, zoneId: String): SkyTable {
        val raw = runCatching {
            File(AndroidContentStore.liveDir(context), "files/$PATH").readText()
        }.getOrNull() ?: return SkyTable("", "", emptyMap())
        return fromJsonOrNull(raw, zoneId) ?: SkyTable("", "", emptyMap())
    }

    /** API pública: un archivo corrupto devuelve una tabla vacía sin lanzar. */
    fun fromJson(raw: String, zoneId: String): SkyTable =
        fromJsonOrNull(raw, zoneId) ?: SkyTable("", "", emptyMap())

    fun fromJson(raw: String): SkyTable = fromJson(raw, "America/Santiago")

    private fun fromJsonOrNull(raw: String, zoneId: String): SkyTable? = runCatching {
        val file = json.decodeFromString(SkyFile.serializer(), raw)
        val zone = DexSchedule.zoneOf(zoneId)
        val days = LinkedHashMap<MonthDay, List<ServerSlot>>()
        file.days.forEach { day ->
            val monthDay = monthDayOf(day.monthDay.ifBlank { day.date }) ?: return@forEach
            val slots = day.slots.mapNotNull { slot ->
                val start = runCatching { LocalTime.parse(slot.start) }.getOrNull() ?: return@mapNotNull null
                val end = if (slot.end == "24:00") {
                    LocalTime.MIDNIGHT
                } else {
                    runCatching { LocalTime.parse(slot.end) }.getOrNull() ?: return@mapNotNull null
                }
                ServerSlot(
                    start = start,
                    end = end,
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
            days[monthDay] = slots
        }
        SkyTable(file.reviewedOn, file.patch, emptyMap(), WeatherCycle(days), zone)
    }.getOrNull()

    private fun monthDayOf(raw: String): MonthDay? {
        val parts = raw.split("-")
        return when (parts.size) {
            2 -> runCatching { MonthDay.of(parts[0].toInt(), parts[1].toInt()) }.getOrNull()
            3 -> runCatching { MonthDay.of(parts[1].toInt(), parts[2].toInt()) }.getOrNull()
            else -> null
        }
    }

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
