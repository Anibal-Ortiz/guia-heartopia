package com.guiaheartopia.app.domain

import java.time.ZoneId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupEnvelope(
    val format: Int,
    val app: String,
    val snapshot: GuideSnapshot,
)

sealed class ImportResult {
    data class Ok(val snapshot: GuideSnapshot) : ImportResult()
    data class Invalid(val reason: String) : ImportResult()
}

object BackupCodec {
    const val FORMAT = 1
    const val APP = "guia-heartopia"
    private const val MAX_RAW_CHARS = 5 * 1024 * 1024
    private const val MAX_ITEMS = 20_000
    private const val MAX_TEXT = 4_000

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun export(snapshot: GuideSnapshot): String =
        json.encodeToString(BackupEnvelope.serializer(), BackupEnvelope(FORMAT, APP, snapshot))

    fun import(raw: String): ImportResult {
        if (raw.length > MAX_RAW_CHARS) {
            return ImportResult.Invalid("El respaldo es demasiado grande.")
        }
        val envelope = try {
            json.decodeFromString(BackupEnvelope.serializer(), raw)
        } catch (_: Exception) {
            return ImportResult.Invalid("El archivo no es un JSON de esta guía.")
        }
        if (envelope.app != APP) return ImportResult.Invalid("Este archivo no pertenece a esta guía.")
        if (envelope.format != FORMAT) {
            return ImportResult.Invalid("Versión de respaldo ${envelope.format}. Esta app lee la versión $FORMAT.")
        }
        if (!withinBounds(envelope.snapshot)) {
            return ImportResult.Invalid("El respaldo contiene demasiados datos o textos muy largos.")
        }
        val profile = envelope.snapshot.profile
        if (profile.name.isBlank() && !profile.onboarded) {
            return ImportResult.Invalid("El respaldo no trae un perfil utilizable.")
        }
        // Un zoneId inventado no debe crashear la app al importar.
        val safeProfile = runCatching { ZoneId.of(profile.zoneId) }
            .fold(
                onSuccess = { profile },
                onFailure = { profile.copy(zoneId = ZoneId.systemDefault().id) },
            )
        return ImportResult.Ok(
            envelope.snapshot.copy(profile = safeProfile.copy(onboarded = true)),
        )
    }

    /** Cotas defensivas: un respaldo manipulado no debe poder agotar memoria ni UI. */
    private fun withinBounds(s: GuideSnapshot): Boolean {
        val lists = listOf(
            s.customAnimals, s.sightedIds, s.hiddenSeedAnimalIds, s.customCollectibles,
            s.foundIds, s.certifiedIds, s.hiddenSeedCollectibleIds, s.hobbies,
            s.huertoFoundIds, s.huertoCertifiedIds, s.florFoundIds, s.florCertifiedIds,
            s.recetaFoundIds, s.recetaCertifiedIds, s.activeCrops, s.redeemedCodeIds,
            s.creatureWindowAlarms, s.profile.seenCodes,
        )
        if (lists.any { it.size > MAX_ITEMS }) return false
        if (s.annotations.size > MAX_ITEMS || s.dexFilters.size > MAX_ITEMS) return false
        if (s.routine.tasks.size > MAX_ITEMS || s.routine.completedIds.size > MAX_ITEMS) return false

        val texts = sequence {
            yield(s.profile.name); yield(s.profile.placeName); yield(s.profile.regionKey)
            yield(s.profile.weatherOriginId); yield(s.routine.epochDay.toString())
            s.profile.seenCodes.forEach { yield(it) }
            s.customAnimals.forEach {
                yield(it.id); yield(it.name); yield(it.group); yield(it.summary)
                yield(it.likedWeather); yield(it.sourceNote); yield(it.imageUrl)
                it.favoriteFoods.forEach { value -> yield(value) }
                it.feederLocations.forEach { value -> yield(value) }
            }
            s.customCollectibles.forEach { yield(it.id); yield(it.name); yield(it.detail); yield(it.sourceNote) }
            s.hobbies.forEach { yield(it.id); yield(it.name); yield(it.note) }
            s.annotations.forEach { (key, value) ->
                yield(key); yield(value.note)
                value.foods.forEach { food -> yield(food) }
                value.feeders.forEach { feeder -> yield(feeder) }
            }
            s.activeCrops.forEach { yield(it.siembraId); yield(it.nombre) }
            s.creatureWindowAlarms.forEach { yield(it.creatureId); yield(it.creatureName) }
        }
        return texts.all { it.length <= MAX_TEXT }
    }
}
