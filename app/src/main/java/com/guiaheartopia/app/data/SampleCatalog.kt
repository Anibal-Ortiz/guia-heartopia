package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.RoutineTask
import com.guiaheartopia.app.domain.SampleIds
import com.guiaheartopia.app.domain.WeatherOrigin
import com.guiaheartopia.app.domain.WeatherSlot

/**
 * Contenido de ejemplo. No son datos oficiales de Heartopia.
 * Para sustituir el clima, edita [SampleWeather] y conserva [verified] = false
 * hasta tener una fuente que se pueda citar.
 */
object SeedCatalog {
    val routine: List<RoutineTask> = listOf(
        RoutineTask("sug-recordar", "Anotar lo que quieras recordar hoy"),
        RoutineTask("sug-animal", "Revisar una ficha de animal o de comedero"),
        RoutineTask("sug-hobby", "Actualizar el nivel de un hobby"),
        RoutineTask("sug-pixel", "Dejar lista una referencia de pixel art"),
    )

    private const val FOOD = "Glosa al español de las guías en inglés. No es el texto oficial del juego."

    val animals: List<AnimalRecord> = listOf(
        animal("panda", "Panda", listOf("Bambú (Bamboo)", "Manzana (Apple)", "Maíz (Corn)")),
        animal("capibara", "Capibara", listOf("Frambuesa (Raspberry)", "Tomate (Tomato)", "Uva (Grape)")),
        animal("conejo", "Conejo", listOf("Hierba (Weed)", "Zanahoria (Carrot)", "Fresa (Strawberry)")),
        animal("zorro", "Zorro", listOf("Carne (Meat)", "Perca americana (Largemouth bass)", "Perca europea (European perch)")),
        animal("nutria", "Nutria marina", listOf("Camarón oriental (Oriental shrimp)", "Camarón común (Common shrimp / prawn)", "Mejillón (Mussel)")),
        animal("huron", "Hurón", listOf("Huevo (Egg)", "Lubina (Sea bass)", "Gobio (Goby)")),
        animal("ciervo", "Ciervo sika", listOf("Rama (Branch / twig)", "Lechuga (Lettuce)", "Ensalada de la casa (House salad)")),
        animal("alpaca", "Alpaca", listOf("Trigo (Wheat)", "Piña (Pineapple)", "Arándano (Blueberry)")),
    )

    private fun animal(id: String, name: String, foods: List<String>) = AnimalRecord(
        id = id,
        name = name,
        group = "Animales silvestres",
        summary = "Tres comidas en las que coinciden tres guías. El comedero no: Polygon, Dexerto y las wikis citan zonas distintas.",
        favoriteFoods = foods,
        feederLocations = emptyList(),
        verified = false,
        foodsVerified = true,
        locationVerified = false,
        sourceNote = VerifiedCatalog.FOOD_SOURCES + " " + FOOD,
    )

    val collectibles: List<Collectible> = listOf(
        cited("pez-angel-azul", "Ángel de mar azul", "Peces", "Blue Sea Angel. Mar antiguo, todo el día, durante el evento. Una sola guía."),
        cited("pez-sepia-dorada", "Sepia dorada", "Peces", "Golden Cuttlefish. Mar antiguo. Una sola guía."),
        cited("ins-escarabajo", "Escarabajo pelotero mandarina", "Insectos", "Mandarin Dung Beetle. Bosque de robles. Una sola guía."),
        cited("ave-cuervo", "Cuervo manzana", "Pájaros", "Apple Raven. Acantilado de piedra. Una sola guía."),
    )

    private fun cited(id: String, name: String, category: String, detail: String) = Collectible(
        id = id,
        name = name,
        category = category,
        detail = detail,
        verified = false,
        sourceNote = "heartopia.th.gl/guides/event-fish-bugs-and-birds (1 nov 2026). Sin segunda fuente. El cierre del evento tampoco coincide con build-heartopia.com.",
    )

    val hobbies: List<Hobby> = listOf(
        Hobby("hob-cocina", "Cocina"),
        Hobby("hob-jardin", "Jardinería"),
        Hobby("hob-mascotas", "Mascotas"),
        Hobby("hob-pesca", "Pesca"),
        Hobby("hob-manual", "Manualidades"),
    )
}

object SampleWeather {
    val origins: List<WeatherOrigin> = listOf(
        WeatherOrigin(
            id = SampleIds.WEATHER_ORIGIN,
            name = "Origen de ejemplo",
            zoneId = "UTC",
            note = "Pendiente de verificar. No es un servidor confirmado de Heartopia.",
        ),
    )

    /** El estado horario del clima no está contrastado. Las ubicaciones de noviembre viven en VerifiedCatalog. */
    val slots: List<WeatherSlot> = emptyList()
}
