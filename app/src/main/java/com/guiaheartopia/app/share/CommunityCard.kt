package com.guiaheartopia.app.share

import com.guiaheartopia.app.domain.SkyKind
import java.time.LocalDate
import java.time.YearMonth

/** Tipo de imagen shareable que genera el módulo Comunidad. */
enum class CommunityImageKind { CALENDAR, WEATHER }

/** Modelo puro de una tarjeta; no depende de Compose ni de Android. */
data class CommunityCard(
    val kind: CommunityImageKind,
    val title: String,
    val subtitle: String,
    val countryName: String,
    val zoneLabel: String,
    val zoneId: String,
    val clockLabel: String,
    val selectedDay: CommunityDay,
    val month: YearMonth? = null,
    val monthTitle: String = "",
    val cells: List<CommunityCalendarCell> = emptyList(),
    val weekLabels: List<String> = CommunityCalendarGrid.WEEK_LABELS,
    val frames: List<CommunityFrame> = emptyList(),
    val weatherSummary: String = "",
    val legend: List<String> = emptyList(),
    val footer: String = "Datos de la comunidad, no oficiales",
    val sourceLabel: String = "",
)

/** Día con ubicaciones de Fluorita y Liuliu. */
data class CommunityDay(
    val date: LocalDate,
    val title: String,
    val liuliuLabel: String,
    val fluritaLabel: String,
    val certaintyLabel: String,
    val hasData: Boolean,
)

/** Celda del mes. `null` en date = celda de relleno. */
data class CommunityCalendarCell(
    val date: LocalDate?,
    val dayNumber: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val liuliuShort: String?,
    val fluritaShort: String?,
    val hasEvent: Boolean,
    val hasData: Boolean,
)

/** Una franja del día del servidor. */
data class CommunityFrame(
    val frameKey: String,
    val title: String,
    val timeRange: String,
    val label: String,
    val emoji: String,
    val kind: SkyKind,
    val detail: String,
    val hasData: Boolean,
    val segments: List<CommunityClimateSegment>,
)

data class CommunityClimateSegment(val kind: SkyKind, val weight: Int)
