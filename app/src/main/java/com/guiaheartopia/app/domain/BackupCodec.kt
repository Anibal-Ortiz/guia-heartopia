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
}
