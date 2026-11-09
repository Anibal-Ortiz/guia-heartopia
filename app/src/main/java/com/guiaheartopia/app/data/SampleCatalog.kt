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

    val animals: List<AnimalRecord> = listOf(
        animal("panda", "Panda", listOf("Bambú", "Manzana", "Maíz"), listOf("Plataformas"), "Lluvioso", "https://www.heartodex.com/_astro/1783738206153-animal-panda_105qed.webp"),
        animal("capibara", "Capibara", listOf("Frambuesa", "Tomate", "Uva"), listOf("Lago volcánico", "Ruinas"), "Arcoíris, Lluvioso", "https://www.heartodex.com/_astro/1783738150016-animal-capibara_qxpUA.webp"),
        animal("conejo", "Conejo", listOf("Hierba", "Zanahoria", "Fresa"), listOf("Suburbio"), "Soleado", "https://www.heartodex.com/_astro/1783738158505-animal-conejo_1tCt1y.webp"),
        animal("zorro", "Zorro", listOf("Carne", "Perca americana", "Perca de río"), listOf("Florimolino", "Costa purpura", "Lago de la pradera"), "Arcoíris", "https://www.heartodex.com/_astro/1783738126393-animal-zorro_WO9be.webp"),
        animal("nutria", "Nutria marina", listOf("Camarón azul", "Camarón marino", "Mejillón"), listOf("Plaza del pueblo pesquero"), "Lluvioso", "https://www.heartodex.com/_astro/1783738186224-animal-nutriamarina_Z1eE5Nl.webp"),
        animal("huron", "Hurón", listOf("Huevo", "Lubina marina", "Gobio"), listOf("Rio de crepusculo"), "Arcoíris", "https://www.heartodex.com/_astro/1783738180133-animal-huron_ZCdP02.webp"),
        animal("ciervo", "Ciervo sika", listOf("Rama", "Lechuga", "Ensalada de la casa"), listOf("Lago del bosque"), "Soleado", "https://www.heartodex.com/_astro/1783738201643-animal-ciervosika_1WSxKk.webp"),
        animal("alpaca", "Alpaca", listOf("Trigo", "Piña", "Arándano"), listOf("Costa purpura"), "Soleado", "https://www.heartodex.com/_astro/1783738190730-animal-alpaca_gDg6z.webp"),
    )

    private fun animal(
        id: String,
        name: String,
        foods: List<String>,
        places: List<String>,
        weather: String,
        imageUrl: String = "",
    ) = AnimalRecord(
        id = id,
        name = name,
        group = "Fauna",
        summary = "",
        favoriteFoods = foods,
        feederLocations = places,
        likedWeather = weather,
        verified = false,
        foodsVerified = true,
        locationVerified = true,
        imageUrl = imageUrl,
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
