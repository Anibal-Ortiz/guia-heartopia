# Guía Heartopia

Guía companion **no oficial** para Heartopia. Sirve para anotar la partida, llevar una rutina personal, consultar un calendario de climas y preparar una referencia de pixel art para pintar a mano.

No está afiliada a los creadores ni a los editores de Heartopia. No controla el juego, no pulsa la pantalla y no promete que el lienzo real coincida con esta cuadrícula.

## Requisitos

- JDK 17 o superior (el proyecto compila con JDK 21).
- Android SDK con la plataforma **37.2** y Build-Tools **37.0.0**.
- `local.properties` con `sdk.dir`. Hay un ejemplo en `local.properties.example`.

```bash
cp local.properties.example local.properties
# edita sdk.dir
```

`minSdk` 26, `compileSdk` y `targetSdk` 37, `versionCode` 1, `versionName` 0.1.0.
El plugin de Android es 9.5.1 y Gradle 9.6, porque las bibliotecas actuales de Compose piden compilar contra la API 37.

## Ejecutar

```bash
./gradlew :app:assembleDebug
```

El APK de pruebas queda en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

En un dispositivo o emulador:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Verificación hecha en esta versión

En este entorno, con JDK 21 y el Android SDK de la plataforma 37:

- `./gradlew :app:testDebugUnitTest` terminó con 26 pruebas y 0 fallos.
- `./gradlew :app:assembleDebug` generó `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew :app:compileDebugAndroidTestKotlin` compiló la prueba de UI de la bienvenida.
- No hay emulador ni dispositivo, así que `connectedDebugAndroidTest` no se ejecutó.

## Pruebas

```bash
./gradlew :app:testDebugUnitTest
```

Cubren el reinicio de la rutina por fecha local, la conversión de zonas (Chile, Nueva York y una zona sin horario de verano), el conversor de pixel art y el progreso de coleccionables.

La prueba de UI de la bienvenida está en `app/src/androidTest` y necesita un dispositivo:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Sin emulador esa tarea no se puede ejecutar. Compilarla no sustituye el resultado en un dispositivo.

## Mapa de pantallas

| Pantalla | Cómo se abre | Qué hace ahora |
| --- | --- | --- |
| Bienvenida | Primer arranque | Nombre, lugar, franja habitual, país y zona. Enlaza privacidad y términos. |
| Inicio | Barra inferior | Saludo, fecha, progreso del día y accesos. |
| Rutina | Barra inferior | Tareas marcables, progreso, reinicio al cambiar la fecha local y «Restaurar tareas del día». |
| Guía | Barra inferior | Pestañas Animales y Coleccionables. Desde aquí también se abre el clima. |
| Ficha de animal | Al tocar un animal | Comidas, comederos, notas tuyas y avistado. |
| Clima | Inicio o Guía | Mes, país, zona, origen horario, filtros de flurita y Liuliu, detalle del día. |
| Pixel | Barra inferior | Elegir imagen, cuadrícula, colores, conversión local, zoom, coordenadas, leyenda y PNG. |
| Hobbies | Barra inferior | Cocina, jardinería, mascotas y otros, con nivel editable. |
| Ajustes | Barra superior | Perfil, país, zona, anuncios, privacidad y términos. |

La barra inferior tiene cinco destinos. Ajustes y el calendario no cabían ahí sin esconderlos: Ajustes está siempre arriba y el calendario está en Inicio y en Guía.

## Estado inicial de cada módulo

| Módulo | Estado |
| --- | --- |
| Perfil | Funcional y guardado en el dispositivo. |
| Rutina | Funcional. La lista inicial es una sugerencia personal, no la rutina oficial. |
| Animales | Estructura lista. Las fichas de ejemplo no traen comidas ni lugares reales. |
| Coleccionables | Seguimiento local por categoría. Los ítems de ejemplo no son oficiales. |
| Clima | Conversión horaria real. Franjas, climas, flurita y Liuliu de ejemplo, pendientes de verificar. |
| Pixel art | Conversión local usable. Paleta y tamaños comunitarios, no oficiales. |
| Hobbies | Progreso personal. Los niveles no vienen de una tabla del juego. |
| Anuncios | Banner de prueba en debug, después del consentimiento. Release sin IDs no pide anuncios. |

## Dónde están los anuncios

Solo la variante **debug** lleva IDs, y son los oficiales de prueba de Google:

- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner: `ca-app-pub-3940256099942544/6300978111`

Están en `app/src/debug/AndroidManifest.xml` y en `app/build.gradle.kts` (`buildTypes.debug`). No hay un App ID ni un bloque inventados.

La variante **release** lee, si existen, estas variables de `local.properties` o del entorno:

- `ADMOB_APP_ID`
- `ADMOB_BANNER_AD_UNIT_ID`

Si faltan, `ADS_ENABLED` queda en false, no se escribe un App ID y no se llama a `MobileAds.initialize`. No subas esos valores al repositorio.

### Consentimiento

El orden es: terminar la bienvenida, consultar UMP, y solo entonces `MobileAds.initialize`. El banner no aparece en la bienvenida ni antes de que el SDK avise que está listo.

- Si la persona no consiente y UMP dice que no se pueden pedir anuncios, no hay banner.
- Si la consulta falla, la solicitud usa anuncios no personalizados (`npa=1`).
- La app no marca el consentimiento como otorgado.
- Para depurar el formulario se puede definir `UMP_DEBUG_GEOGRAPHY` (`EEA`, `NOT_EEA`, `REGULATED_US_STATE`, `OTHER`) y, si hace falta, `UMP_TEST_DEVICE_ID`. Vacío significa que no se fuerza la geografía.

Antes de publicar hace falta un **mensaje de privacidad GDPR/regiones reguladas creado y publicado en la consola de AdMob** (Privacidad y mensajes). Sin ese mensaje, el formulario no llega a los usuarios del EEE en producción. También faltan el nombre de quien responde, un correo y el dominio: la política dentro de la app lo dice.

### APK de pruebas y AAB de release

```bash
./gradlew :app:assembleDebug
```

Ese APK usa IDs de prueba. No lo subas a Play como si fuera la ficha definitiva.

```bash
./gradlew :app:bundleRelease
```

`bundleRelease` genera un Android App Bundle solo cuando tiene sentido publicarlo. La firma **no** vive en el repositorio. Si quieres firmar, define fuera de git:

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Sin esas claves el bundle, si llega a generarse, queda sin firmar. No generes ni subas un keystore al repo. Esta versión no publica nada en Play.

`versionCode` es 1 y `versionName` es `0.1.0`. Súbelos en `app/build.gradle.kts` antes de cada envío.

### Play Data Safety y app-ads.txt

Cuando haya anuncios reales, el formulario de Data Safety debe declarar que Google AdMob puede tratar el identificador de publicidad (`AD_ID`) y datos técnicos del dispositivo al pedir un anuncio. La guía en sí no tiene cuenta ni analítica propia: el progreso se queda en el teléfono.

`app-ads.txt` queda pendiente del dominio y del ID de editor reales. No hay un archivo con un publisher ID de relleno. La línea, el día que existan, tiene esta forma y nada más:

```text
google.com, pub-XXXXXXXXXXXXXXXX, DIRECT, f08c47fec0942fa0
```

`f08c47fec0942fa0` es el identificador de Google como autoridad de ventas, no un ID de esta app.

Permisos de la app: `INTERNET`, `ACCESS_NETWORK_STATE` y `AD_ID` (lo exige el SDK de anuncios). No hay permiso de ubicación ni de almacenamiento amplio.

## Pixel art

El núcleo está en `app/src/main/java/com/guiaheartopia/app/pixel` y se puede sustituir por `ImageToPixel` sin rehacer la pantalla.

1. La imagen se decodifica a ARGB y se limita a 1600 px.
2. Box-filter con alfa, en modo recortar, encajar o estirar (`srcRect` existe en el conversor para un recorte posterior).
3. Color más cercano en CIELAB (ΔE76²) sobre la paleta.
4. Se puede quedar con los N colores más usados.
5. Contornos nítidos por defecto, o dithering Floyd–Steinberg.
6. El blanco conectado al borde puede quedar vacío.

La paleta es una **paleta aproximada de referencia**, de origen comunitario. No es oficial. Los tamaños predefinidos tampoco son una especificación del lienzo: se pueden cambiar con los controles.

La guía se ve con zoom, desplazamiento, grilla y números cuando la casilla es legible. Al tocar se muestran columna, fila y color. Exportar guarda un PNG en `Imágenes/GuiaHeartopia` mediante MediaStore en Android 10 o superior. En Android 8 y 9 el archivo queda en el directorio de imágenes de la propia app, sin permiso extra.

## Clima, flurita y Liuliu

La conversión está en `WeatherTime` y usa `java.time.ZoneId`. No hay desfases UTC fijos. En un hueco horario java.time adelanta la hora local; en un solapamiento usa el desfase anterior.

Los datos están en `app/src/main/java/com/guiaheartopia/app/data/SampleCatalog.kt`, objeto `SampleWeather`. Para actualizarlos:

1. Añade un `WeatherOrigin` con su `ZoneId` real si llega a conocerse el servidor. No inventes el nombre.
2. Añade `WeatherSlot` con la hora civil de ese origen, el estado y, si la fuente lo dice, lugar y aparición de `flurita` o `Liuliu`.
3. Deja `verified = false` hasta que la fuente se pueda citar. La interfaz muestra «Pendiente de verificar».

No se corrigió «flurita» a «Fluorita» ni se interpretó «Liuliu»: no hay una fuente oficial en esta versión que fije el nombre en español, el lugar o el horario. Hay sitios de fans con calendarios y ubicaciones, y no coinciden entre ellos; por eso no se copiaron como si fueran el calendario del juego.

El país y la zona se eligen en la bienvenida y en Ajustes, y el calendario usa esa misma selección. Si el idioma o la zona del teléfono están en la lista, se proponen al empezar. No se pide permiso de ubicación.

## Contenido pendiente de verificar

- Nombres, comidas y comederos de animales.
- Catálogo de coleccionables.
- Calendario de climas, servidor de origen, lugares y aparición de flurita y Liuliu.
- Paleta de color y tamaños de lienzo.
- Niveles oficiales de hobbies, si el juego los tiene. Aquí el número es solo el que anota quien juega.

## Estructura

```text
app/src/main/java/com/guiaheartopia/app
  MainActivity.kt          navegación, borde a borde e inicio de anuncios
  ads/                     UMP y banner; el fallo no bloquea la app
  data/                    DataStore y catálogos de ejemplo
  domain/                  rutina, progreso, países y zonas
  pixel/                   conversor, paleta, visor y exportación
  ui/                      pantallas Compose
app/src/main/assets/legal  privacidad y términos que también abre Ajustes
```

La persistencia es DataStore Preferences. La última cuadrícula de pixel art se guarda en el almacenamiento interno de la app.
