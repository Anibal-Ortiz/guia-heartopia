package com.guiaheartopia.app.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiaheartopia.app.data.GuideRepository
import com.guiaheartopia.app.data.SeedCatalog
import com.guiaheartopia.app.domain.AnimalAnnotation
import com.guiaheartopia.app.domain.CatalogMarks
import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.CollectionProgress
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.Greeting
import com.guiaheartopia.app.domain.GuideSnapshot
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.HobbyCatalog
import com.guiaheartopia.app.domain.ActiveCrop
import com.guiaheartopia.app.domain.AficionesMarks
import com.guiaheartopia.app.domain.CreatureAppearing
import com.guiaheartopia.app.domain.CreatureWindowAlarm
import com.guiaheartopia.app.domain.DexCategoryFilters
import com.guiaheartopia.app.domain.DexSchedule
import com.guiaheartopia.app.data.HeartodexCatalog
import com.guiaheartopia.app.data.SkyCatalog
import com.guiaheartopia.app.domain.HobbyProgress
import com.guiaheartopia.app.domain.PlayerProfile
import com.guiaheartopia.app.domain.SampleIds
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import com.guiaheartopia.app.domain.BackupCodec
import com.guiaheartopia.app.domain.ImportResult
import com.guiaheartopia.app.domain.ReminderPrefs
import com.guiaheartopia.app.reminders.ReminderScheduler
import com.guiaheartopia.app.data.AvatarStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GuideUi(
    val ready: Boolean = false,
    val profile: PlayerProfile = PlayerProfile(),
    val animals: List<AnimalRecord> = emptyList(),
    val annotations: Map<String, AnimalAnnotation> = emptyMap(),
    val sighted: Set<String> = emptySet(),
    val collectibles: List<Collectible> = emptyList(),
    val found: Set<String> = emptySet(),
    val certified: Set<String> = emptySet(),
    val categoryProgress: List<com.guiaheartopia.app.domain.CategoryProgress> = emptyList(),
    val collectionFraction: Float = 0f,
    val hobbies: List<Hobby> = emptyList(),
    val huertoFound: Set<String> = emptySet(),
    val huertoCertified: Set<String> = emptySet(),
    val florFound: Set<String> = emptySet(),
    val florCertified: Set<String> = emptySet(),
    val recetaFound: Set<String> = emptySet(),
    val recetaCertified: Set<String> = emptySet(),
    val activeCrops: List<ActiveCrop> = emptyList(),
    val redeemedCodes: Set<String> = emptySet(),
    val dexFilters: Map<String, DexCategoryFilters> = emptyMap(),
    val creatureWindowAlarms: List<CreatureWindowAlarm> = emptyList(),
    val greeting: String = "",
    val todayLabel: String = "",
)

class GuideViewModel(
    private val repository: GuideRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val appContext: Context? = null,
) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now(zone))
    private val dateFormat = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))

    val ui = combine(repository.snapshots, today) { snap, day ->
        present(snap, day)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GuideUi())

    init {
        refreshDay()
    }

    fun refreshDay() {
        today.value = LocalDate.now(zone)
        viewModelScope.launch {
            repository.update { snap ->
                val hobbies = ensureHobbies(snap)
                if (hobbies == snap.hobbies && snap.hobbiesInitialized) snap
                else snap.copy(hobbies = hobbies, hobbiesInitialized = true)
            }
        }
    }

    fun dismissReminderPrompt() {
        viewModelScope.launch {
            repository.update { snap ->
                val reminders = snap.profile.reminders.copy(promptDismissed = true)
                snap.copy(profile = snap.profile.copy(reminders = reminders))
            }
        }
    }

    fun saveReminders(reminders: ReminderPrefs) {
        val withoutRoutine = reminders.copy(routineEnabled = false)
        viewModelScope.launch {
            repository.update { current ->
                current.copy(profile = current.profile.copy(reminders = withoutRoutine))
            }
            appContext?.let { ctx ->
                ReminderScheduler.sync(ctx, repository.snapshots.first().profile)
            }
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
            viewModelScope.launch {
                val snap = parsed.snapshot
                val profile = snap.profile.copy(
                    reminders = snap.profile.reminders.copy(routineEnabled = false),
                )
                appContext?.let { AvatarStore.restore(it, profile.avatarJpegBase64) }
                repository.replace(snap.copy(profile = profile))
            }
        }
        onResult(parsed)
    }

    fun saveProfile(profile: PlayerProfile) {
        viewModelScope.launch {
            repository.update {
                it.copy(profile = profile.copy(onboarded = true, name = profile.name.trim()))
            }
        }
    }

    fun updateRegion(
        countryId: String,
        zoneId: String,
        originId: String = SampleIds.WEATHER_ORIGIN,
        regionKey: String = "",
    ) {
        val resolved = when (originId) {
            SampleIds.LEGACY_WEATHER_ORIGIN, "" -> SampleIds.WEATHER_ORIGIN
            else -> originId
        }
        viewModelScope.launch {
            repository.update { current ->
                current.copy(
                    profile = current.profile.copy(
                        countryId = countryId,
                        zoneId = zoneId,
                        regionKey = regionKey,
                        weatherOriginId = resolved,
                    ),
                )
            }
            appContext?.let { ctx ->
                ReminderScheduler.sync(ctx, repository.snapshots.first().profile)
            }
        }
    }

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
                val next = CatalogMarks.toggleFound(marks(snap), id)
                snap.copy(foundIds = next.found.toList(), certifiedIds = next.certified.toList())
            }
        }
    }

    fun markCodesSeen(codes: List<String>) {
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(profile = snap.profile.copy(seenCodes = codes.distinct()))
            }
        }
    }

    fun saveAvatar(uri: Uri) {
        val context = appContext ?: return
        viewModelScope.launch {
            val encoded = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input) ?: return@withContext null
                    val jpeg = AvatarStore.centerCropJpeg(bitmap)
                    bitmap.recycle()
                    AvatarStore.save(context, jpeg)
                }
            } ?: return@launch
            repository.update { snap ->
                snap.copy(profile = snap.profile.copy(avatarJpegBase64 = encoded))
            }
        }
    }

    fun clearAvatar() {
        val context = appContext ?: return
        AvatarStore.clear(context)
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(profile = snap.profile.copy(avatarJpegBase64 = null))
            }
        }
    }

    fun toggleCertified(id: String, category: String) {
        viewModelScope.launch {
            val context = appContext
            if (context != null) {
                val entry = HeartodexCatalog.load(context).firstOrNull { it.id == id }
                if (entry != null && !CatalogMarks.certifiable(entry)) return@launch
            }
            repository.update { snap ->
                val next = CatalogMarks.toggleCertified(marks(snap), id, category)
                snap.copy(foundIds = next.found.toList(), certifiedIds = next.certified.toList())
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
                            hobby.maxLevel,
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

    fun toggleHuertoFound(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                val marks = huertoMarks(snap)
                val next = AficionesMarks.toggleFound(marks, id)
                snap.copy(
                    huertoFoundIds = next.found.toList(),
                    huertoCertifiedIds = next.certified.toList(),
                )
            }
        }
    }

    fun toggleHuertoCertified(id: String, certifiable: Boolean) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = AficionesMarks.toggleCertified(huertoMarks(snap), id, certifiable)
                snap.copy(
                    huertoFoundIds = next.found.toList(),
                    huertoCertifiedIds = next.certified.toList(),
                )
            }
        }
    }

    fun toggleFlorFound(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = AficionesMarks.toggleFound(florMarks(snap), id)
                snap.copy(florFoundIds = next.found.toList(), florCertifiedIds = next.certified.toList())
            }
        }
    }

    fun toggleFlorCertified(id: String, certifiable: Boolean) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = AficionesMarks.toggleCertified(florMarks(snap), id, certifiable)
                snap.copy(florFoundIds = next.found.toList(), florCertifiedIds = next.certified.toList())
            }
        }
    }

    fun toggleRecetaFound(id: String) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = AficionesMarks.toggleFound(recetaMarks(snap), id)
                snap.copy(recetaFoundIds = next.found.toList(), recetaCertifiedIds = next.certified.toList())
            }
        }
    }

    fun toggleRecetaCertified(id: String, certifiable: Boolean) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = AficionesMarks.toggleCertified(recetaMarks(snap), id, certifiable)
                snap.copy(recetaFoundIds = next.found.toList(), recetaCertifiedIds = next.certified.toList())
            }
        }
    }

    fun plantSiembra(siembraId: String, nombre: String, growthMinutes: Int) {
        val context = appContext ?: return
        if (growthMinutes <= 0) return
        viewModelScope.launch {
            repository.update { snap ->
                val now = System.currentTimeMillis()
                val ready = now + growthMinutes * 60_000L
                val crop = ActiveCrop(
                    id = "crop-${UUID.randomUUID()}",
                    siembraId = siembraId,
                    nombre = nombre,
                    plantedAtEpochMilli = now,
                    readyAtEpochMilli = ready,
                )
                snap.copy(activeCrops = snap.activeCrops.filterNot { it.siembraId == siembraId } + crop)
            }
            ReminderScheduler.sync(context, repository.snapshots.first().profile)
        }
    }

    fun cancelCrop(cropId: String) {
        val context = appContext ?: return
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(activeCrops = snap.activeCrops.filterNot { it.id == cropId })
            }
            ReminderScheduler.sync(context, repository.snapshots.first().profile)
        }
    }

    fun saveDexFilters(category: String, filters: DexCategoryFilters) {
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(dexFilters = snap.dexFilters + (category to filters))
            }
        }
    }

    fun setCodeRedeemed(code: String, redeemed: Boolean) {
        viewModelScope.launch {
            repository.update { snap ->
                val next = snap.redeemedCodeIds.toMutableSet()
                if (redeemed) next.add(code) else next.remove(code)
                snap.copy(redeemedCodeIds = next.toList())
            }
        }
    }

    fun scheduleCreatureWindowAlarm(creatureId: String, creatureName: String) {
        val context = appContext ?: return
        viewModelScope.launch {
            val profile = repository.snapshots.first().profile
            val zone = DexSchedule.zoneOf(profile.zoneId)
            val sky = SkyCatalog.load(context)
            val entry = HeartodexCatalog.load(context).firstOrNull { it.id == creatureId } ?: return@launch
            val next = CreatureAppearing.nextWindow(entry.schedule, entry.weather, sky, zone) ?: return@launch
            val alarm = CreatureWindowAlarm(
                creatureId = creatureId,
                creatureName = creatureName,
                fireAtEpochMilli = next.start.toInstant().toEpochMilli(),
            )
            repository.update { snap ->
                snap.copy(
                    creatureWindowAlarms = snap.creatureWindowAlarms.filterNot { it.creatureId == creatureId } + alarm,
                )
            }
            ReminderScheduler.sync(context, repository.snapshots.first().profile)
        }
    }

    fun cancelCreatureWindowAlarm(creatureId: String) {
        val context = appContext ?: return
        viewModelScope.launch {
            repository.update { snap ->
                snap.copy(creatureWindowAlarms = snap.creatureWindowAlarms.filterNot { it.creatureId == creatureId })
            }
            ReminderScheduler.sync(context, repository.snapshots.first().profile)
        }
    }

    private fun present(snap: GuideSnapshot, day: LocalDate): GuideUi {
        val animals = SeedCatalog.animals.filter { it.id !in snap.hiddenSeedAnimalIds } + snap.customAnimals
        val collectibles = SeedCatalog.collectibles.filter { it.id !in snap.hiddenSeedCollectibleIds } +
            snap.customCollectibles
        val found = snap.foundIds.toSet()
        val hobbies = ensureHobbies(snap)
        return GuideUi(
            ready = true,
            profile = snap.profile.copy(reminders = snap.profile.reminders.copy(routineEnabled = false)),
            animals = animals,
            annotations = snap.annotations,
            sighted = snap.sightedIds.toSet(),
            collectibles = collectibles,
            found = found,
            certified = snap.certifiedIds.toSet(),
            categoryProgress = CollectionProgress.byCategory(collectibles, found),
            collectionFraction = CollectionProgress.overall(collectibles, found),
            hobbies = hobbies,
            huertoFound = snap.huertoFoundIds.toSet(),
            huertoCertified = snap.huertoCertifiedIds.toSet(),
            florFound = snap.florFoundIds.toSet(),
            florCertified = snap.florCertifiedIds.toSet(),
            recetaFound = snap.recetaFoundIds.toSet(),
            recetaCertified = snap.recetaCertifiedIds.toSet(),
            activeCrops = snap.activeCrops,
            redeemedCodes = snap.redeemedCodeIds.toSet(),
            dexFilters = snap.dexFilters,
            creatureWindowAlarms = snap.creatureWindowAlarms,
            greeting = Greeting.line(LocalTime.now(zone).hour, snap.profile.name),
            todayLabel = day.format(dateFormat),
        )
    }

    private fun ensureHobbies(snap: GuideSnapshot): List<Hobby> =
        HobbyCatalog.migrate(snap.hobbies, hobbyOverrides())

    private fun hobbyOverrides(): Map<String, Int> {
        val context = appContext ?: return emptyMap()
        val bytes = com.guiaheartopia.app.data.AndroidContentLibrary(context).effectiveBytes("hobbies.json")
            ?: return emptyMap()
        return HobbyCatalog.parseOverrides(bytes.decodeToString())
    }

    private fun huertoMarks(snap: GuideSnapshot) =
        AficionesMarks.Marks(snap.huertoFoundIds.toSet(), snap.huertoCertifiedIds.toSet())

    private fun florMarks(snap: GuideSnapshot) =
        AficionesMarks.Marks(snap.florFoundIds.toSet(), snap.florCertifiedIds.toSet())

    private fun recetaMarks(snap: GuideSnapshot) =
        AficionesMarks.Marks(snap.recetaFoundIds.toSet(), snap.recetaCertifiedIds.toSet())

    private fun marks(snap: GuideSnapshot): CatalogMarks.Marks =
        CatalogMarks.Marks(snap.foundIds.toSet(), snap.certifiedIds.toSet())

    private fun splitLines(raw: String): List<String> =
        raw.split('\n', ',').map { it.trim() }.filter { it.isNotEmpty() }

    companion object {
        fun factory(repository: GuideRepository, context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GuideViewModel(repository, appContext = context.applicationContext) as T
            }
    }
}
