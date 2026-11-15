package com.guiaheartopia.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class DexCategoryFilters(
    val query: String = "",
    val place: String = "",
    val onlyNow: Boolean = false,
    val registered: String = "todos",
    val certified: String = "todos",
    val weather: String = "Todos",
    val sortByLevel: Boolean = false,
    val chosenLevel: Int = -1,
    val eventFocus: String = "",
)

@Serializable
data class CreatureWindowAlarm(
    val creatureId: String,
    val creatureName: String,
    val fireAtEpochMilli: Long,
)

data class GuideLaunch(
    val onlyNow: Boolean = false,
    val registered: String = "",
    val eventFocus: String = "",
)

fun mergeGuideLaunch(base: DexCategoryFilters, launch: GuideLaunch): DexCategoryFilters =
    base.copy(
        onlyNow = if (launch.onlyNow) true else base.onlyNow,
        registered = launch.registered.ifBlank { base.registered },
        eventFocus = launch.eventFocus.ifBlank { base.eventFocus },
    )

fun dexEntryMatchesEvent(entry: com.guiaheartopia.app.data.DexEntry, eventId: String, eventName: String): Boolean {
    val tag = entry.event?.trim().orEmpty()
    if (tag.isBlank()) return false
    return tag.equals(eventId, ignoreCase = true) || tag.equals(eventName, ignoreCase = true)
}