package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.SampleIds
import com.guiaheartopia.app.domain.WeatherOrigin
import com.guiaheartopia.app.domain.WeatherSlot

/**
 * Contenido de ejemplo. No son datos oficiales de Heartopia.
 * Para sustituir el clima, edita [SampleWeather] y conserva [verified] = false
 * hasta tener una fuente que se pueda citar.
 */
object SeedCatalog {
    val animals: List<AnimalRecord> = listOf(
        animal("panda", "Panda", listOf("Bambú", "Manzana", "Maíz"), listOf("Plataformas"), "Lluvioso", "asset:catalog/images/sample-panda.webp"),
        animal("capibara", "Capibara", listOf("Frambuesa", "Tomate", "Uva"), listOf("Lago volcánico", "Ruinas"), "Arcoíris, Lluvioso", "asset:catalog/images/sample-capibara.webp"),
        animal("conejo", "Conejo", listOf("Hierba", "Zanahoria", "Fresa"), listOf("Suburbio"), "Soleado", "asset:catalog/images/sample-conejo.webp"),
        animal("zorro", "Zorro", listOf("Carne", "Perca americana", "Perca de río"), listOf("Florimolino", "Costa purpura", "Lago de la pradera"), "Arcoíris", "asset:catalog/images/sample-zorro.webp"),
        animal("nutria", "Nutria marina", listOf("Camarón azul", "Camarón marino", "Mejillón"), listOf("Plaza del pueblo pesquero"), "Lluvioso", "asset:catalog/images/sample-nutria.webp"),
        animal("huron", "Hurón", listOf("Huevo", "Lubina marina", "Gobio"), listOf("Rio de crepusculo"), "Arcoíris", "asset:catalog/images/sample-huron.webp"),
        animal("ciervo", "Ciervo sika", listOf("Rama", "Lechuga", "Ensalada de la casa"), listOf("Lago del bosque"), "Soleado", "asset:catalog/images/sample-ciervo.webp"),
        animal("alpaca", "Alpaca", listOf("Trigo", "Piña", "Arándano"), listOf("Costa purpura"), "Soleado", "asset:catalog/images/sample-alpaca.webp"),
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

    val hobbies: List<Hobby> = com.guiaheartopia.app.domain.HobbyCatalog.fixed
}

object SampleWeather {
    val origins: List<WeatherOrigin> = listOf(
        WeatherOrigin(
            id = SampleIds.WEATHER_ORIGIN,
            name = "Hora del servidor (UTC)",
            zoneId = "UTC",
            note = "Si el calendario trae tramos, están en UTC y se convierten a la zona IANA elegida. " +
                "El archivo de clima incluido no trae tramos por fecha: se usa el paquete publicado.",
        ),
    )

    /** Reservado: no hay slots de muestra; el calendario real está en assets. */
    val slots: List<WeatherSlot> = emptyList()
}
