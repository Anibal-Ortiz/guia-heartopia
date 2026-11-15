package com.guiaheartopia.app.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

object HobbyCatalog {
    const val COCINA = "Cocina"
    const val SIEMBRA = "Siembra"
    const val PESCA = "Pesca"
    const val AVES = "Observación de aves"
    const val INSECTOS = "Captura de insectos"

    const val CAP_COCINA = 13
    const val CAP_SIEMBRA = 13
    const val CAP_PESCA = 14
    const val CAP_AVES = 14
    const val CAP_INSECTOS = 14

    val fixed: List<Hobby> = listOf(
        Hobby("hob-cocina", COCINA, maxLevel = CAP_COCINA),
        Hobby("hob-siembra", SIEMBRA, maxLevel = CAP_SIEMBRA),
        Hobby("hob-pesca", PESCA, maxLevel = CAP_PESCA),
        Hobby("hob-aves", AVES, maxLevel = CAP_AVES),
        Hobby("hob-insectos", INSECTOS, maxLevel = CAP_INSECTOS),
    )

    fun caps(overrides: Map<String, Int> = emptyMap()): Map<String, Int> {
        val base = mapOf(
            "hob-cocina" to CAP_COCINA,
            "hob-siembra" to CAP_SIEMBRA,
            "hob-pesca" to CAP_PESCA,
            "hob-aves" to CAP_AVES,
            "hob-insectos" to CAP_INSECTOS,
        )
        if (overrides.isEmpty()) return base
        return base.mapValues { (id, cap) -> overrides[id]?.takeIf { it >= 1 } ?: cap }
    }

    fun parseOverrides(raw: String?): Map<String, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val element = Json.parseToJsonElement(raw)
            val obj = element as? JsonObject ?: return emptyMap()
            obj.mapNotNull { (key, value) ->
                val number = (value as? JsonPrimitive)?.intOrNull ?: return@mapNotNull null
                if (number < 1) null else key to number
            }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun guideCap(category: String): Int? = when (category) {
        "Peces" -> CAP_PESCA
        "Pájaros", "Aves" -> CAP_AVES
        "Insectos" -> CAP_INSECTOS
        else -> null
    }

    fun forCategory(category: String): String? = when (category) {
        "Peces" -> PESCA
        "Pájaros", "Aves" -> AVES
        "Insectos" -> INSECTOS
        else -> null
    }

    fun migrate(existing: List<Hobby>, overrides: Map<String, Int> = emptyMap()): List<Hobby> {
        val limits = caps(overrides)
        return fixed.map { template ->
            val source = existing.firstOrNull { matches(it, template) }
            val cap = limits[template.id] ?: template.maxLevel
            template.copy(
                level = (source?.level ?: 0).coerceIn(0, cap),
                maxLevel = cap,
                note = source?.note.orEmpty(),
            )
        }
    }

    private fun matches(saved: Hobby, template: Hobby): Boolean {
        if (saved.id == template.id || saved.name == template.name) return true
        if (template.id == "hob-siembra") {
            return saved.id == "hob-jardin" || saved.name == "Jardinería" || saved.name == "Jardineria"
        }
        return false
    }
}

object CreatureAvailability {
    fun levelAllowed(level: Int?, cap: Int?): Boolean {
        if (cap == null) return true
        return (level ?: 0) <= cap
    }

    fun hobbyLevel(category: String, hobbies: List<Hobby>): Int? {
        val name = HobbyCatalog.forCategory(category) ?: return null
        return hobbies.firstOrNull { it.name == name }?.level ?: 0
    }

    fun availableNow(category: String, level: Int?, hobbies: List<Hobby>, timeAndWeatherOk: Boolean): Boolean {
        if (!timeAndWeatherOk) return false
        val cap = hobbyLevel(category, hobbies) ?: return true
        return levelAllowed(level, cap)
    }
}
