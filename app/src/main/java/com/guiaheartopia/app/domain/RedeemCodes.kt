package com.guiaheartopia.app.domain

import java.time.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RedeemCode(
    val code: String = "",
    val recompensa: String = "",
    val fechaInicio: String? = null,
    val fechaVencimiento: String? = null,
    val fuente: String? = null,
)

object RedeemCodes {
    const val PATH = "codes/codigos.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String?): List<RedeemCode> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RedeemCode>>(raw)
                .filter { it.code.isNotBlank() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isActive(code: RedeemCode, today: LocalDate): Boolean {
        val start = parseDate(code.fechaInicio)
        val end = parseDate(code.fechaVencimiento)
        if (start != null && today.isBefore(start)) return false
        if (end != null && today.isAfter(end)) return false
        return true
    }

    fun isExpired(code: RedeemCode, today: LocalDate): Boolean {
        val end = parseDate(code.fechaVencimiento) ?: return false
        return today.isAfter(end)
    }

    fun active(codes: List<RedeemCode>, today: LocalDate): List<RedeemCode> =
        codes.filter { isActive(it, today) }

    fun expired(codes: List<RedeemCode>, today: LocalDate): List<RedeemCode> =
        codes.filter { isExpired(it, today) }

    fun unseen(active: List<RedeemCode>, seen: Set<String>): List<RedeemCode> =
        active.filter { it.code !in seen }

    fun unredeemed(active: List<RedeemCode>, redeemed: Set<String>): List<RedeemCode> =
        active.filter { it.code !in redeemed }

    fun parseDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        val date = raw.trim().take(10)
        return runCatching { LocalDate.parse(date) }.getOrNull()
    }
}
