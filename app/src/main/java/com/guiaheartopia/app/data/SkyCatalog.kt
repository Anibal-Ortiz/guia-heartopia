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

    fun load(context: Context): SkyTable {
        val tables = ArrayList<SkyTable>()
        val bundled = context.assets.list("calendar").orEmpty()
            .filter { it.startsWith("clima-") && it.endsWith(".json") }
            .sorted()
        bundled.forEach { name ->
            context.assets.open("calendar/$name").bufferedReader().use { reader ->
                tables += fromJson(reader.readText())
            }
        }
        val overlayDir = File(AndroidContentStore.liveDir(context), "files/calendar")
        overlayDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("clima-") && it.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?.forEach { file -> tables += fromJson(file.readText()) }
        if (tables.isEmpty()) return SkyTable("", "", emptyMap())
        return mergeSkyTables(tables)
    }

    fun fromJson(raw: String): SkyTable {
        val file = json.decodeFromString(SkyFile.serializer(), raw)
        val days = file.days.map { day ->
            SkyDay(
                date = LocalDate.parse(day.date),
                slots = day.slots.map { slot ->
                    SkySlot(
                        start = LocalTime.parse(slot.start),
                        end = if (slot.end == "24:00") LocalTime.MIDNIGHT else LocalTime.parse(slot.end),
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
                },
            )
        }
        return SkyTable(file.reviewedOn, file.patch, days.associateBy { it.date })
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

data class SkyTable(
    val reviewedOn: String,
    val patch: String,
    val days: Map<LocalDate, SkyDay>,
) {
    fun on(date: LocalDate): SkyDay? = days[date]

    fun reviewLabel(): String {
        val parts = reviewedOn.split("-")
        val shown = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else reviewedOn
        return "Clima revisado el $shown; puede cambiar con actualizaciones del juego"
    }
}
