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
        animal("panda", "Panda", listOf("Bambú (Bamboo)", "Manzana (Apple)", "Maíz (Corn)"), listOf("Forest Jump Puzzle"), "Rainy"),
        animal("capibara", "Capibara", listOf("Frambuesa (Raspberry)", "Tomate (Tomato)", "Uva (Grape)"), listOf("Crater Lake", "Ruins"), "Rainbow, Rainy"),
        animal("conejo", "Conejo", listOf("Hierba (Weed)", "Zanahoria (Carrot)", "Fresa (Strawberry)"), listOf("Suburbs"), "Sunny"),
        animal("zorro", "Zorro", listOf("Carne (Meat)", "Perca americana (Largemouth bass)", "Perca europea (European perch)"), listOf("Windmill Flower Field", "Amethyst Beach", "Meadow Lake"), "Rainbow"),
        animal("nutria", "Nutria marina", listOf("Camarón oriental (Oriental shrimp)", "Camarón común (Common shrimp / prawn)", "Mejillón (Mussel)"), listOf("Fishing Village Square"), "Rainy"),
        animal("huron", "Hurón", listOf("Huevo (Egg)", "Lubina (Sea bass)", "Gobio (Goby)"), listOf("Rosy River"), "Rainbow"),
        animal("ciervo", "Ciervo sika", listOf("Rama (Branch / twig)", "Lechuga (Lettuce)", "Ensalada de la casa (House salad)"), listOf("Forest Lake"), "Sunny"),
        animal("alpaca", "Alpaca", listOf("Trigo (Wheat)", "Piña (Pineapple)", "Arándano (Blueberry)"), listOf("Amethyst Beach"), "Sunny"),
    )

    private fun animal(id: String, name: String, foods: List<String>, places: List<String>, weather: String) = AnimalRecord(
        id = id,
        name = name,
        group = "Fauna",
        summary = "Comidas contrastadas en tres guías en inglés. El lugar y el clima preferido salen de heartodex, la fuente elegida para esta versión.",
        favoriteFoods = foods,
        feederLocations = places,
        likedWeather = weather,
        verified = false,
        foodsVerified = true,
        locationVerified = true,
        sourceNote = VerifiedCatalog.FOOD_SOURCES + " " + FOOD + " Lugar: https://www.heartodex.com/en/wild-animals/ (" + VerifiedCatalog.EXTRACTED + ").",
    )

    val collectibles: List<Collectible> = emptyList()

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
