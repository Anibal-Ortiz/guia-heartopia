package com.guiaheartopia.app.domain

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

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun export(snapshot: GuideSnapshot): String =
        json.encodeToString(BackupEnvelope.serializer(), BackupEnvelope(FORMAT, APP, snapshot))

    fun import(raw: String): ImportResult {
        val envelope = try {
            json.decodeFromString(BackupEnvelope.serializer(), raw)
        } catch (_: Exception) {
            return ImportResult.Invalid("El archivo no es un JSON de esta guía.")
        }
        if (envelope.app != APP) return ImportResult.Invalid("Este archivo no pertenece a Guía Heartopia.")
        if (envelope.format != FORMAT) {
            return ImportResult.Invalid("Versión de respaldo ${envelope.format}. Esta app lee la versión $FORMAT.")
        }
        if (envelope.snapshot.profile.name.isBlank() && !envelope.snapshot.profile.onboarded) {
            return ImportResult.Invalid("El respaldo no trae un perfil utilizable.")
        }
        return ImportResult.Ok(envelope.snapshot.copy(profile = envelope.snapshot.profile.copy(onboarded = true)))
    }
}
