package com.guiaheartopia.app.data

import com.guiaheartopia.app.domain.AnimalRecord
import com.guiaheartopia.app.domain.Collectible
import com.guiaheartopia.app.domain.Hobby
import com.guiaheartopia.app.domain.ResourceKeys
import com.guiaheartopia.app.domain.ResourceSighting
import com.guiaheartopia.app.domain.RoutineTask
import com.guiaheartopia.app.domain.SampleIds
import com.guiaheartopia.app.domain.WeatherKind
import com.guiaheartopia.app.domain.WeatherOrigin
import com.guiaheartopia.app.domain.WeatherSlot
import java.time.LocalDateTime

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
        AnimalRecord(
            id = "ejemplo-ave",
            name = "Ejemplo: ave del camino",
            group = "Aves (ejemplo)",
            summary = "Ficha de estructura. No es un animal del juego.",
        ),
        AnimalRecord(
            id = "ejemplo-mamifero",
            name = "Ejemplo: mamífero del bosque",
            group = "Mamíferos (ejemplo)",
            summary = "Sirve para probar búsqueda, filtros y la ficha.",
        ),
        AnimalRecord(
            id = "ejemplo-ribera",
            name = "Ejemplo: animal de ribera",
            group = "Ribera (ejemplo)",
            summary = "Las comidas y los comederos se completan cuando haya datos verificados.",
        ),
    )

    val collectibles: List<Collectible> = listOf(
        Collectible("ej-nat-1", "Ejemplo: pieza de naturaleza", "Naturaleza (ejemplo)", "Ítem de estructura, sin dato oficial."),
        Collectible("ej-nat-2", "Ejemplo: segunda pieza", "Naturaleza (ejemplo)", "Solo para ver el progreso de la categoría."),
        Collectible("ej-obj-1", "Ejemplo: objeto de casa", "Objetos (ejemplo)", "No corresponde a un objeto real del juego."),
        Collectible("ej-est-1", "Ejemplo: objeto de temporada", "Estacional (ejemplo)", "Pendiente de una lista oficial."),
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

    /**
     * Franjas de ejemplo en noviembre de 2026, en hora civil del origen (UTC).
     * Los estados solo enseñan los iconos. No son el calendario del juego.
     * Una franja a las 02:30 UTC cae en el día anterior en Chile continental (UTC−3/−4).
     */
    val slots: List<WeatherSlot> = listOf(
        slot("s-sol", 2, 8, 0, WeatherKind.SUN, emptyList()),
        slot(
            "s-lluvia",
            3,
            14,
            0,
            WeatherKind.RAIN,
            listOf(sighting(ResourceKeys.FLURITA, "flurita")),
        ),
        slot(
            "s-nube",
            4,
            18,
            30,
            WeatherKind.CLOUD,
            listOf(sighting(ResourceKeys.LIULIU, "Liuliu")),
        ),
        slot("s-tormenta", 5, 23, 30, WeatherKind.STORM, emptyList()),
        slot(
            "s-cambio-dia",
            6,
            2,
            30,
            WeatherKind.FOG,
            listOf(
                sighting(ResourceKeys.FLURITA, "flurita"),
                sighting(ResourceKeys.LIULIU, "Liuliu"),
            ),
        ),
        slot("s-nieve", 7, 11, 0, WeatherKind.SNOW, emptyList()),
        slot("s-duda", 8, 19, 0, WeatherKind.UNVERIFIED, emptyList()),
    )

    private fun sighting(key: String, name: String) = ResourceSighting(
        key = key,
        displayName = name,
        location = null,
        appearance = null,
        verified = false,
    )

    private fun slot(
        id: String,
        day: Int,
        hour: Int,
        minute: Int,
        kind: WeatherKind,
        sightings: List<ResourceSighting>,
    ) = WeatherSlot(
        id = id,
        originId = SampleIds.WEATHER_ORIGIN,
        originLocal = LocalDateTime.of(2026, 11, day, hour, minute),
        kind = kind,
        summary = "Ejemplo de franja. Pendiente de verificar.",
        verified = false,
        sightings = sightings,
    )
}
