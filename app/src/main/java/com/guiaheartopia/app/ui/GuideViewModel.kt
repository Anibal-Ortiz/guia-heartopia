package com.guiaheartopia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SeedCatalog
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.CollectionProgress
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.Greeting
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyProgress
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.RoutineLogic
import com.guiaheartopia.app.domain.RoutineState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.ReminderPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GuideUi(
    val ready: Boolean = false,
    val profile: PlayerProfile = PlayerProfile(),
    val routine: RoutineState = RoutineState(),
    val routineFraction: Float = 0f,
    val animals: List<AnimalRecord> = emptyList(),
    val annotations: Map<String, AnimalAnnotation> = emptyMap(),
    val sighted: Set<String> = emptySet(),
    val collectibles: List<Collectible> = emptyList(),
    val found: Set<String> = emptySet(),
    val categoryProgress: List<com.guiaheartopia.app.domain.CategoryProgress> = emptyList(),
    val collectionFraction: Float = 0f,
    val hobbies: List<Hobby> = emptyList(),
    val greeting: String = "",
    val todayLabel: String = "",
)

class GuideViewModel(
    private val repository: GuideRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now(zone))
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es"))

    val ui = combine(repository.snapshots, today) { snap, day ->
        present(snap, day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuideUi())

    init {
        refreshDay()
    }

    fun refreshDay() {
        val day = LocalDate.now(zone)
        today.value = day
        viewModelScope.launch {
            repository.update { snap ->
                val routine = RoutineLogic.prepare(snap.routine, day, SeedCatalog.routine)
                val hobbies = ensureHobbies(snap)
                if (routine == snap.routine && hobbies == snap.hobbies && snap.hobbiesInitialized) snap
                else snap.copy(routine = routine, hobbies = hobbies, hobbiesInitialized = true)
            }
        }
    }

    fun saveReminders(reminders: ReminderPrefs) {
        viewModelScope.launch {
            repository.update { snap -> snap.copy(profile = snap.profile.copy(reminders = reminders)) }
        }
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            onReady(BackupCodec.export(repository.snapshots.first()))
        }
    }

    fun importBackup(raw: String, onResult: (ImportResult) -> Unit) {
        val parsed = BackupCodec.import(raw)
        if (parsed is ImportResult.Ok) {
            viewModelScope.launch { repository.replace(parsed.snapshot) }
        }
        onResult(parsed)
    }

    fun saveProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            repository.update { it.copy(profile = profile.copy(onboarded = true, name = profile.name.trim())) }
        }
    }

    fun updateRegion(countryId: String, zoneId: String, originId: String) {
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(
                    profile = snap.profile.copy(
                        countryId = countryId,
                        zoneId = zoneId,
                        weatherOriginId = originId,
                    ),
                )
            }
        }
    }

    fun toggleTask(id: String) = updateRoutine { RoutineLogic.toggle(it, id) }

    fun restoreRoutine() = updateRoutine { RoutineLogic.restoreDay(it) }

    fun addTask(title: String) = updateRoutine { RoutineLogic.addTask(it, title, "task-${UUID.randomUUID()}") }

    fun removeTask(id: String) = updateRoutine { RoutineLogic.removeTask(it, id) }

    fun toggleSighted(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                val set = snap.sightedIds.toMutableSet()
                if (!set.add(id)) set.remove(id)
                snap.copy(sightedIds = set.toList())
            }
        }
    }

    fun saveAnnotation(animalId: String, annotation: AnimalAnnotation) {
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(annotations = snap.annotations + (animalId to annotation))
            }
        }
    }

    fun addAnimal(name: String, group: String, foods: String, feeders: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val record = AnimalRecord(
            id = "user-${UUID.randomUUID()}",
            name = clean,
            group = group.trim().ifEmpty { "Mis fichas" },
            summary = "Ficha escrita por ti. No es un dato oficial.",
            favoriteFoods = splitLines(foods),
            feederLocations = splitLines(feeders),
            verified = false,
            playerOwned = true,
        )
        viewModelScope.launch {
            repository.update { it.copy(customAnimals = it.customAnimals + record) }
        }
    }

    fun toggleFound(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                val set = snap.foundIds.toMutableSet()
                if (!set.add(id)) set.remove(id)
                snap.copy(foundIds = set.toList())
            }
        }
    }

    fun addCollectible(name: String, category: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val item = Collectible(
            id = "user-${UUID.randomUUID()}",
            name = clean,
            category = category.trim().ifEmpty { "Mis notas" },
            detail = "Nota tuya. No es un dato oficial.",
            verified = false,
            playerOwned = true,
        )
        viewModelScope.launch {
            repository.update { it.copy(customCollectibles = it.customCollectibles + item) }
        }
    }

    fun setHobby(id: String, level: Int? = null, maxLevel: Int? = null, note: String? = null) {
        viewModelScope.launch {
            repository.update { snap ->
                val hobbies = ensureHobbies(snap).map { hobby ->
                    if (hobby.id != id) hobby
                    else {
                        val (lvl, max) = HobbyProgress.clamp(
                            level ?: hobby.level,
                            maxLevel ?: hobby.maxLevel,
                        )
                        hobby.copy(level = lvl, maxLevel = max, note = note ?: hobby.note)
                    }
                }
                snap.copy(hobbies = hobbies, hobbiesInitialized = true)
            }
        }
    }

    fun addHobby(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            repository.update { snap ->
                val hobby = Hobby(id = "hob-${UUID.randomUUID()}", name = clean)
                snap.copy(hobbies = ensureHobbies(snap) + hobby, hobbiesInitialized = true)
            }
        }
    }

    fun removeHobby(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(hobbies = ensureHobbies(snap).filterNot { it.id == id }, hobbiesInitialized = true)
            }
        }
    }

    private fun updateRoutine(block: (RoutineState) -> RoutineState) {
        viewModelScope.launch {
            repository.update { snap ->
                val prepared = RoutineLogic.prepare(snap.routine, LocalDate.now(zone), SeedCatalog.routine)
                snap.copy(routine = block(prepared))
            }
        }
    }

    private fun present(snap: GuideSnapshot, day: LocalDate): GuideUi {
        val routine = RoutineLogic.prepare(snap.routine, day, SeedCatalog.routine)
        val animals = SeedCatalog.animals.filter { it.id !in snap.hiddenSeedAnimalIds } + snap.customAnimals
        val collectibles = SeedCatalog.collectibles.filter { it.id !in snap.hiddenSeedCollectibleIds } +
            snap.customCollectibles
        val found = snap.foundIds.toSet()
        val hobbies = ensureHobbies(snap)
        return GuideUi(
            ready = true,
            profile = snap.profile,
            routine = routine,
            routineFraction = RoutineLogic.fraction(routine),
            animals = animals,
            annotations = snap.annotations,
            sighted = snap.sightedIds.toSet(),
            collectibles = collectibles,
            found = found,
            categoryProgress = CollectionProgress.byCategory(collectibles, found),
            collectionFraction = CollectionProgress.overall(collectibles, found),
            hobbies = hobbies,
            greeting = Greeting.line(LocalTime.now(zone).hour, snap.profile.name),
            todayLabel = day.format(dateFormat),
        )
    }

    private fun ensureHobbies(snap: GuideSnapshot): List<Hobby> =
        if (snap.hobbiesInitialized) snap.hobbies else SeedCatalog.hobbies

    private fun splitLines(raw: String): List<String> =
        raw.split('\n', ',').map { it.trim() }.filter { it.isNotEmpty() }

    companion object {
        fun factory(repository: GuideRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GuideViewModel(repository) as T
            }
    }
}
