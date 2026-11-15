# Análisis de la importación del 9 de noviembre de 2026

Fuente autoritativa: ZIP `guia-heartopia-clean-src`, raíz `guia-heartopia/`, 100 archivos, SHA256 `dbf41a0044c2c7c9436ca6e9d7aae72fa26926d24725f1305f16ddcbed439d43`. Se importó en una rama nueva desde `main` (`4307018`), sin reescribir historial. Comparación también contra v0.5.1 (`8ad8036`). No se corrigió ningún fallo.

Se conservó `.github/workflows/build.yml`, que no venía en el ZIP. No se versionaron `local.properties` ni `app/generated-release-manifest/` (siguen en `.gitignore`). El ZIP no traía el bit de ejecución de `gradlew`; el contenido es el de `main` y se dejó ejecutable.

La versión no cambió: `versionName` 0.5.1, `versionCode` 7 (`app/build.gradle.kts`).

## Qué cambió

Frente a `main`, 22 archivos modificados y 4 nuevos: `AppIcons.kt`, `CreatureImage.kt`, `gradle/gradle-daemon-jvm.properties`, `tools/merge_images.py`. El calendario (`assets/calendar/clima-2026.json`), el motor de píxel, anuncios, respaldo (`BackupCodec` formato 1) y los textos de créditos no vinieron distintos.

Frente a v0.5.1 el delta es el mismo salvo `colors.xml` y `themes.xml`, que coinciden con v0.5.1. Importar el archivo encima de `main` sustituye el tema acogedor ya publicado (colores Material y `Theme.kt`).

## Avances

- Navegación inferior igual: Inicio, Rutina, Guía, Píxel, Aficiones. Ajustes sigue en la barra superior. Créditos siguen solo en Ajustes.
- Inicio se rediseñó: saludo, país, tramo de juego, colección por categoría (130 peces, 114 pájaros, 106 insectos, 11 fauna), flurita, Liuliu y accesos a Guía, Clima y Aficiones. Ya no abre Mapa, Rutina ni Píxel desde ahí. El menú «Hobbies» de `main` pasa a «Aficiones».
- Guía, clima (Mes / Semana / Lista), rutina, píxel, aficiones y ajustes siguen. «Ahora» y «Disponible ahora» siguen usando horario y, si hay dato, clima (`SkyWeather.availableNow`).
- Catálogo: los 361 ids y el resto de campos no cambian. Solo se añade `imageUrl`. Las 361 fichas apuntan a `https://www.heartodex.com/...webp`. Coil 3.2.0 las carga en runtime; si la URL está vacía, queda el glifo vectorial. `tools/merge_images.py` arma esas URLs y no descarga bytes.
- Los animales de ejemplo también reciben URL remota. El horario UTC de criaturas en `DexSchedule` ya coincidía con la referencia confirmada (Noche 05–11, Amanecer 11–17, Día 17–23, Tarde 23–05 UTC; Chile UTC−3 el 2026-11-06: 02–08 / 08–14 / 14–20 / 20–02). Tras esta revisión se alinearon comentarios, leyenda de la guía y pruebas. Etiquetas de clima del catálogo: Soleado → Despejado, Lluvioso → Lluvia.
- Respaldo compatible: campos nuevos con valor por defecto, `ignoreUnknownKeys = true`, mismos ids.

## Mapa y rutina

No se eliminaron. `MapScreen` y la ruta `map` siguen. El botón «Mapa» de la guía y el acceso de Inicio desaparecieron, y `onOpenMap` ya no se llama: la pantalla queda sin entrada visible. La rutina sigue en la barra inferior, en Ajustes y en la lógica diaria. Inicio calcula las tareas hechas (`done`) y no las muestra. No se implementó el retiro pendiente.

## Riesgos, sin corregir

1. Calendario de clima y franjas de criaturas son independientes (clima en bloques 00/06/12/18 UTC). `SkyWeatherTest` en la línea 52 sigue usando mediodía UTC como si fuera el tramo «Day» del catálogo; a las 12:00 UTC el tramo de criatura es Amanecer (11:00–17:00 UTC). Ese fallo es ajeno a la corrección de documentación de horarios.
2. Créditos desactualizados. Ajustes sigue diciendo que no se copiaron imágenes, pero la app pide 361 imágenes de heartodex por red. Eso choca con la decisión previa de no usar esas imágenes. No se añadieron ni se quitaron en este import.
3. Si la red falla, `CreatureImage` no vuelve al glifo: el glifo solo aparece con URL vacía.
4. La fauna de ejemplo sigue rotulada Soleado / Lluvioso, mientras el catálogo dice Despejado / Lluvia.
5. `gradle/gradle-daemon-jvm.properties` pide JDK 25 (Foojay). Esta compilación usó el JDK 21 ya instalado.

## Pruebas y compilación

`:app:testDebugUnitTest` (importación): 50 pruebas, 42 correctas, 8 fallos. Tras actualizar `DexScheduleTest` a la referencia correcta, las pruebas de horario deberían pasar; puede quedar un fallo en `SkyWeatherTest` por el instante de prueba (ver punto 1).

`:app:assembleDebug` terminó. APK local `app/build/outputs/apk/debug/Guia-Heartopia-0.5.1-debug.apk`, 19 844 287 bytes. No se publicó. Gradle acaba en fallo porque los tests fallan. No se ejecutaron pruebas de instrumentación ni se revisó la interfaz en un dispositivo. Este entorno no tenía SDK ni `local.properties`; se instaló la plataforma `android-37.0` solo para compilar, fuera del repositorio.
