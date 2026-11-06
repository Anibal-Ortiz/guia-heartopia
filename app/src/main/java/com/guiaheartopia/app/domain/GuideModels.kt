package com.guiaheartopia.app.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PlaySession(val label: String) {
    MANANA("Mañana"),
    TARDE("Tarde"),
    NOCHE("Noche"),
    VARIABLE("Variable"),
}

@Serializable
data class PlayerProfile(
    val onboarded: Boolean = false,
    val name: String = "",
    val placeName: String = "",
    val session: PlaySession = PlaySession.VARIABLE,
    val countryId: String = "CL",
    val zoneId: String = "America/Santiago",
    val weatherOriginId: String = SampleIds.WEATHER_ORIGIN,
    val pixelCols: Int = 32,
    val pixelRows: Int = 32,
    val reminders: ReminderPrefs = ReminderPrefs(),
)

object SampleIds {
    const val WEATHER_ORIGIN = "origen-ejemplo"
}

@Serializable
data class RoutineTask(
    val id: String,
    val title: String,
)

@Serializable
data class RoutineState(
    val epochDay: Long = 0,
    val tasks: List<RoutineTask> = emptyList(),
    val completedIds: List<String> = emptyList(),
    val initialized: Boolean = false,
)

@Serializable
data class AnimalRecord(
    val id: String,
    val name: String,
    val group: String,
    val summary: String,
    val favoriteFoods: List<String> = emptyList(),
    val feederLocations: List<String> = emptyList(),
    val likedWeather: String = "",
    val verified: Boolean = false,
    val foodsVerified: Boolean = false,
    val locationVerified: Boolean = false,
    val sourceNote: String = "",
    val playerOwned: Boolean = false,
)

@Serializable
data class AnimalAnnotation(
    val foods: List<String> = emptyList(),
    val feeders: List<String> = emptyList(),
    val note: String = "",
)

@Serializable
data class Collectible(
    val id: String,
    val name: String,
    val category: String,
    val detail: String,
    val verified: Boolean = false,
    val sourceNote: String = "",
    val playerOwned: Boolean = false,
)

@Serializable
data class Hobby(
    val id: String,
    val name: String,
    val level: Int = 0,
    val maxLevel: Int = 10,
    val note: String = "",
)

@Serializable
data class GuideSnapshot(
    val profile: PlayerProfile = PlayerProfile(),
    val routine: RoutineState = RoutineState(),
    val customAnimals: List<AnimalRecord> = emptyList(),
    val annotations: Map<String, AnimalAnnotation> = emptyMap(),
    val sightedIds: List<String> = emptyList(),
    val hiddenSeedAnimalIds: List<String> = emptyList(),
    val customCollectibles: List<Collectible> = emptyList(),
    val foundIds: List<String> = emptyList(),
    val hiddenSeedCollectibleIds: List<String> = emptyList(),
    val hobbies: List<Hobby> = emptyList(),
    val hobbiesInitialized: Boolean = false,
)

data class CategoryProgress(
    val category: String,
    val found: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else found.toFloat() / total
}
