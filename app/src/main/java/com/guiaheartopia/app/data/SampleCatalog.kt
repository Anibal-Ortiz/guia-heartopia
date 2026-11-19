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
        animal("panda", "Panda", listOf("Bambú", "Manzana", "Maíz"), listOf("Plataformas"), "Lluvioso", ""),
        animal("capibara", "Capibara", listOf("Frambuesa", "Tomate", "Uva"), listOf("Lago volcánico", "Ruinas"), "Arcoíris, Lluvioso", ""),
        animal("conejo", "Conejo", listOf("Hierba", "Zanahoria", "Fresa"), listOf("Suburbio"), "Soleado", ""),
        animal("zorro", "Zorro", listOf("Carne", "Perca americana", "Perca de río"), listOf("Florimolino", "Costa purpura", "Lago de la pradera"), "Arcoíris", ""),
        animal("nutria", "Nutria marina", listOf("Camarón azul", "Camarón marino", "Mejillón"), listOf("Plaza del pueblo pesquero"), "Lluvioso", ""),
        animal("huron", "Hurón", listOf("Huevo", "Lubina marina", "Gobio"), listOf("Rio de crepusculo"), "Arcoíris", ""),
        animal("ciervo", "Ciervo sika", listOf("Rama", "Lechuga", "Ensalada de la casa"), listOf("Lago del bosque"), "Soleado", ""),
        animal("alpaca", "Alpaca", listOf("Trigo", "Piña", "Arándano"), listOf("Costa purpura"), "Soleado", ""),
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
