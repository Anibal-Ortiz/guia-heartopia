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
        animal("panda", "Panda", listOf("Bambú", "Manzana", "Maíz"), listOf("Plataformas"), "Lluvioso"),
        animal("capibara", "Capibara", listOf("Frambuesa", "Tomate", "Uva"), listOf("Lago volcánico", "Ruinas"), "Arcoíris, Lluvioso"),
        animal("conejo", "Conejo", listOf("Hierba", "Zanahoria", "Fresa"), listOf("Suburbio"), "Soleado"),
        animal("zorro", "Zorro", listOf("Carne", "Perca americana", "Perca de río"), listOf("Florimolino", "Costa purpura", "Lago de la pradera"), "Arcoíris"),
        animal("nutria", "Nutria marina", listOf("Camarón azul", "Camarón marino", "Mejillón"), listOf("Plaza del pueblo pesquero"), "Lluvioso"),
        animal("huron", "Hurón", listOf("Huevo", "Lubina marina", "Gobio"), listOf("Rio de crepusculo"), "Arcoíris"),
        animal("ciervo", "Ciervo sika", listOf("Rama", "Lechuga", "Ensalada de la casa"), listOf("Lago del bosque"), "Soleado"),
        animal("alpaca", "Alpaca", listOf("Trigo", "Piña", "Arándano"), listOf("Costa purpura"), "Soleado"),
    )

    private fun animal(id: String, name: String, foods: List<String>, places: List<String>, weather: String) = AnimalRecord(
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
