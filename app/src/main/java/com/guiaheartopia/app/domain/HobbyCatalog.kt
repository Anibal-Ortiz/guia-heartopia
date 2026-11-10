package com.guiaheartopia.app.domain

object HobbyCatalog {
    const val COCINA = "Cocina"
    const val SIEMBRA = "Siembra"
    const val PESCA = "Pesca"
    const val AVES = "Observación de aves"
    const val INSECTOS = "Captura de insectos"

    val fixed: List<Hobby> = listOf(
        Hobby("hob-cocina", COCINA),
        Hobby("hob-siembra", SIEMBRA),
        Hobby("hob-pesca", PESCA),
        Hobby("hob-aves", AVES),
        Hobby("hob-insectos", INSECTOS),
    )

    fun forCategory(category: String): String? = when (category) {
        "Peces" -> PESCA
        "Pájaros", "Aves" -> AVES
        "Insectos" -> INSECTOS
        else -> null
    }

    fun migrate(existing: List<Hobby>): List<Hobby> = fixed.map { template ->
        val source = existing.firstOrNull { matches(it, template) }
        template.copy(level = source?.level ?: 0, note = source?.note.orEmpty())
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
