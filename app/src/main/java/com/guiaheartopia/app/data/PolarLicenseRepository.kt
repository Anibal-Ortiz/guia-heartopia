package com.guiaheartopia.app.data

import android.content.Context
import android.os.Build
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private const val BASE_URL = "https://api.polar.sh/v1/customer-portal/license-keys"
private const val ORGANIZATION_ID = "3d34cb7b-7a7e-4e5c-a0ac-7801756f8566"
private const val CONNECT_TIMEOUT_MS = 20_000
private const val READ_TIMEOUT_MS = 20_000
private const val HTTP_UNPROCESSABLE_ENTITY = 422
private const val MAX_RESPONSE_CHARS = 64 * 1024

internal const val VALIDATION_INTERVAL_MS = 72L * 60 * 60 * 1000

internal const val MESSAGE_ACTIVATION_LIMIT =
    "Este código ya alcanzó el límite máximo de 2 dispositivos activos"
internal const val MESSAGE_INVALID_CODE =
    "El código ingresado no es válido o ya no está activo"
internal const val MESSAGE_RATE_LIMIT =
    "Demasiados intentos. Espera un momento e inténtalo de nuevo."
internal const val MESSAGE_SERVICE_UNAVAILABLE =
    "El servicio no está disponible. Inténtalo de nuevo más tarde."

private const val MESSAGE_NETWORK =
    "No se pudo conectar. Revisa tu conexión e inténtalo de nuevo."
private const val MESSAGE_GENERIC_FAILURE =
    "No se pudo verificar el código. Inténtalo de nuevo."
private const val MESSAGE_DEACTIVATE_FAILURE =
    "No se pudo desvincular este dispositivo. Inténtalo de nuevo."

class PolarLicenseRepository private constructor(
    private val preferences: LicensePreferences,
) {
    private val mutex = Mutex()

    suspend fun activateLicense(rawKey: String): Result<Unit> = mutex.withLock {
        val key = rawKey.trim()
        if (key.isEmpty()) {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_INVALID_CODE))
        }

        val payload = buildJsonObject {
            put("key", key)
            put("organization_id", ORGANIZATION_ID)
            put("label", deviceLabel())
        }.toString()

        val response = try {
            post("/activate", payload)
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_NETWORK))
        } catch (_: Exception) {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_GENERIC_FAILURE))
        }

        if (response.code != HttpURLConnection.HTTP_OK) {
            return@withLock Result.failure(
                LicenseAccessException(activationErrorMessage(response.code, response.body)),
            )
        }

        val root = parseObject(response.body)
        val activationId = root?.string("id").orEmpty()
        val status = (root?.get("license_key") as? JsonObject)?.string("status")
        if (activationId.isBlank() || status != "granted") {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_INVALID_CODE))
        }

        preferences.activate(key, activationId, System.currentTimeMillis())
        Result.success(Unit)
    }

    suspend fun validateLicenseSilently() {
        mutex.withLock {
            val state = preferences.stateFlow.first()
            if (!state.isPro) return@withLock
            if (state.licenseKey.isBlank() || state.activationId.isBlank()) {
                preferences.clear()
                return@withLock
            }

            val now = System.currentTimeMillis()
            if (now - state.lastValidatedAt < VALIDATION_INTERVAL_MS) return@withLock

            val payload = buildJsonObject {
                put("key", state.licenseKey)
                put("organization_id", ORGANIZATION_ID)
                put("activation_id", state.activationId)
            }.toString()

            val response = try {
                post("/validate", payload)
            } catch (error: CancellationException) {
                throw error
            } catch (_: IOException) {
                return@withLock
            } catch (_: Exception) {
                return@withLock
            }

            when (response.code) {
                HttpURLConnection.HTTP_OK -> {
                    when (parseObject(response.body)?.string("status")) {
                        "granted" -> preferences.touchValidated(now)
                        "revoked", "disabled", "expired" -> preferences.clear()
                        else -> Unit
                    }
                }

                HttpURLConnection.HTTP_NOT_FOUND,
                HTTP_UNPROCESSABLE_ENTITY -> preferences.clear()

                else -> Unit
            }
        }
    }

    suspend fun deactivateCurrentDevice(): Result<Unit> = mutex.withLock {
        val state = preferences.stateFlow.first()
        if (state.licenseKey.isBlank() || state.activationId.isBlank()) {
            preferences.clear()
            return@withLock Result.success(Unit)
        }

        val payload = buildJsonObject {
            put("key", state.licenseKey)
            put("organization_id", ORGANIZATION_ID)
            put("activation_id", state.activationId)
        }.toString()

        val response = try {
            post("/deactivate", payload)
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_DEACTIVATE_FAILURE))
        } catch (_: Exception) {
            return@withLock Result.failure(LicenseAccessException(MESSAGE_DEACTIVATE_FAILURE))
        }

        when (response.code) {
            HttpURLConnection.HTTP_OK,
            HttpURLConnection.HTTP_NO_CONTENT,
            HttpURLConnection.HTTP_NOT_FOUND -> {
                preferences.clear()
                Result.success(Unit)
            }

            else -> Result.failure(LicenseAccessException(MESSAGE_DEACTIVATE_FAILURE))
        }
    }

    private suspend fun post(path: String, payload: String): HttpResponse =
        withContext(Dispatchers.IO) {
            val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                doOutput = true
                useCaches = false
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            try {
                connection.outputStream.use { output ->
                    output.write(payload.toByteArray(Charsets.UTF_8))
                }
                val code = connection.responseCode
                val body = when {
                    code == HttpURLConnection.HTTP_NO_CONTENT -> ""
                    code in 200..299 -> connection.inputStream
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use(::readBounded)
                        .orEmpty()

                    else -> connection.errorStream
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use(::readBounded)
                        .orEmpty()
                }
                HttpResponse(code, body)
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        @Volatile
        private var instance: PolarLicenseRepository? = null

        fun get(context: Context): PolarLicenseRepository =
            instance ?: synchronized(this) {
                instance ?: PolarLicenseRepository(LicensePreferences.get(context)).also {
                    instance = it
                }
            }
    }
}

internal fun activationErrorMessage(code: Int, body: String): String = when {
    code == 429 -> MESSAGE_RATE_LIMIT
    code in 500..599 -> MESSAGE_SERVICE_UNAVAILABLE
    code == 400 || code == 403 || code == 404 || code == 422 -> {
        if (mentionsActivationLimit(body)) MESSAGE_ACTIVATION_LIMIT else MESSAGE_INVALID_CODE
    }

    else -> MESSAGE_INVALID_CODE
}

private fun mentionsActivationLimit(body: String): Boolean {
    val lower = body.lowercase()
    return lower.contains("activation limit") ||
        (lower.contains("limit") && !lower.contains("rate limit"))
}

private fun deviceLabel(): String =
    "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android" }

/** Nunca lee una respuesta sin límite: evita que un servidor roto agote memoria. */
private fun readBounded(reader: java.io.Reader): String {
    val buffer = CharArray(8 * 1024)
    val out = StringBuilder()
    var total = 0
    while (true) {
        val read = reader.read(buffer)
        if (read < 0) break
        total += read
        if (total > MAX_RESPONSE_CHARS) break
        out.append(buffer, 0, read)
    }
    return out.toString()
}

private fun parseObject(body: String): JsonObject? =
    runCatching { Json.parseToJsonElement(body) }.getOrNull() as? JsonObject

private fun JsonObject.string(name: String): String? =
    (get(name) as? JsonPrimitive)?.contentOrNull

private data class HttpResponse(val code: Int, val body: String)

private class LicenseAccessException(message: String) : Exception(message)
