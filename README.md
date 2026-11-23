# Guía Heartopia

Guía companion **no oficial** para Heartopia. No está afiliada a los creadores ni a los editores del juego: no controla la partida, no pulsa la pantalla y no promete que los datos de terceros sean oficiales.

Versión **1.1.3** (`versionCode` 46, `versionName` 1.1.3).

## Qué hay en esta versión

- Cuatro pestañas en la barra inferior: **Inicio**, **Aficiones**, **Códigos** y **Calendario**. **Ajustes** se abre desde la barra superior.
- Menú de **comentarios y preguntas** en la barra superior: envía el mensaje desde la app (sin abrir el correo) a un formulario de Google Forms del desarrollador.
- Aviso de **actualización disponible en Google Play** con botón que abre la ficha de la app.
- Catálogos, bichos, peces, recetas, clima y códigos se cargan desde el manifiesto remoto publicado en `https://anibalortizdev.github.io/guia-heartopia-contenido/`. No se copiaron imágenes del juego.
- Perfil, rutina, avisos y progreso se guardan solo en el dispositivo.
- Alrededor de **40 pruebas unitarias** (`app/src/test`) más una prueba de UI que necesita dispositivo (`app/src/androidTest`).
- El manifiesto de release se genera con una tarea Gradle, no al configurar el proyecto; la CI lo verifica sin secretos.
- Firma de release sin secretos: la compilación falla con un mensaje claro, salvo que se pida explícitamente `-PallowDebugSignedRelease=true` para una prueba local.

### Histórico resumido

- 0.5.1: clima de noviembre y diciembre de 2026 en cuatro tramos, cruzado entre fuentes de la comunidad; conversión con `java.time` y horario de verano.
- 0.5.0: calendario de noviembre y diciembre estimado con ciclo de 50 días; iconos dibujados por la app.
- 0.4.1: nombres, lugares, climas y horarios del catálogo recopilados para esta guía; el nombre en inglés solo sirve para buscar.
- 0.4.0: tramos del juego en UTC fijo, mostrados en la zona elegida.
- 0.3.0: catálogo inicial de criaturas (extracción 2026-11-06), sin scraping de fichas individuales.
- 0.2.0: interfaz con Plus Jakarta Sans (OFL); se dejó el estilo pixelado para la herramienta de cuadrícula.
- 0.1.0: primera versión, con bienvenida, rutina, guía, clima y referencia de pixel art.

## Requisitos

- JDK 17 o superior (el proyecto compila con JDK 21).
- Android SDK con plataforma **37** y Build-Tools **37.0.0**.
- `local.properties` con `sdk.dir`:

```bash
cp local.properties.example local.properties
# edita sdk.dir
```

`minSdk` 26, `targetSdk` 36, `compileSdk` 37. Android Gradle Plugin 9.5.1, Kotlin 2.3.21 y Gradle 9.6.

## Ejecutar y verificar

```bash
./gradlew :app:assembleDebug --console=plain
```

El APK queda en `app/build/outputs/apk/debug/` (con el nombre derivado de `appVersion`, por ejemplo `Guia-Heartopia-1.1.3-debug.apk`).

```bash
adb install -r app/build/outputs/apk/debug/<apk-generado>.apk
```

Comprobaciones que no necesitan secretos:

```bash
./gradlew :app:testDebugUnitTest --console=plain
./gradlew :app:lintDebug --console=plain
./gradlew :app:verifyReleaseManifest --console=plain
```

`verifyReleaseManifest` genera el manifiesto de release en una tarea Gradle y comprueba que es XML válido, que declara `APPLICATION_ID` cuando los anuncios están activos y que elimina `MobileAdsInitProvider` cuando no los hay. Las mismas cuatro comprobaciones (más `assembleDebug`) las ejecuta `.github/workflows/build.yml` con `--console=plain`, `permissions: contents: read`, concurrencia con cancelación y límite de 20 minutos.

La prueba de UI de bienvenida está en `app/src/androidTest` y requiere dispositivo o emulador:

```bash
./gradlew :app:connectedDebugAndroidTest --console=plain
```

## Mapa de pantallas

| Pantalla | Cómo se abre | Qué hace |
| --- | --- | --- |
| Inicio | Pestaña inferior | Saludo, fecha, progreso del día y accesos. |
| Aficiones | Pestaña inferior | Cocina, jardinería, mascotas y otros, con nivel que anota quien juega. |
| Códigos | Pestaña inferior | Códigos canjeables y su estado. |
| Calendario | Pestaña inferior | Mes, país, zona, clima, flurita y Liuliu, detalle del día. |
| Guía | Desde Inicio | Catálogo de animales y coleccionables, con búsqueda y filtros. |
| Ajustes | Barra superior | Perfil, país, zona, anuncios, avisos, privacidad y términos. |
| Bienvenida | Primer arranque | Nombre, lugar, franja habitual, país y zona; enlaza privacidad y términos. |

## Estado de los módulos

| Módulo | Estado |
| --- | --- |
| Perfil | Funcional, guardado en el dispositivo. |
| Rutina | Funcional; la lista inicial es una sugerencia personal, no la oficial. |
| Guía | Catálogo local/integrado con búsqueda reactiva; los datos vienen del manifiesto remoto. |
| Aficiones | Progreso personal; los niveles no vienen de una tabla del juego. |
| Códigos | Lista y seguimiento local de códigos. |
| Calendario | Conversión horaria real; varios datos siguen marcados como pendientes de verificar. |
| Anuncios | Banner adaptativo en debug con IDs de prueba; en release solo con IDs reales configurados. |

## Anuncios

La variante **debug** usa los IDs oficiales de prueba de Google, definidos en `app/build.gradle.kts` (`buildTypes.debug`) y en `app/src/debug/AndroidManifest.xml`:

- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner: `ca-app-pub-3940256099942544/6300978111`

La variante **release** lee, si existen, estas claves de `local.properties` o del entorno:

- `ADMOB_APP_ID`
- `ADMOB_BANNER_AD_UNIT_ID`
- `ADMOB_INTERSTITIAL_AD_UNIT_ID`
- `ADMOB_REWARDED_CALENDAR_UNIT_ID`
- `ADMOB_REWARDED_ADFREE_UNIT_ID`

No hay IDs de producción inventados en el repositorio. Si el App ID o el banner están vacíos o parecen marcadores (`xxxx`, `yyyy`, `placeholder`, `tu_id`, `reemplazar`, `ejemplo`, `admob_…`), release cae a los IDs de prueba de Google y `ADS_USE_GOOGLE_TEST_IDS` queda en `true`. Esa detección de placeholders vive en `admobPlaceholder()` en `app/build.gradle.kts` y se expone a Kotlin como `BuildConfig.ADS_CONFIGURED` (`true` en debug por ser IDs de prueba válidos; en release, `true` solo con IDs reales). `AdMobIds.kt` podría usar ese booleano en lugar de repetir la comprobación en runtime.

El orden de inicialización es: terminar la bienvenida, consultar UMP y solo entonces `MobileAds.initialize`. El banner no aparece antes del consentimiento. Si UMP dice que no se pueden pedir anuncios, no hay banner; **si la consulta falla, el SDK no se inicializa** y Ajustes ofrece «Reintentar consentimiento de anuncios». No se usan anuncios no personalizados (`npa=1`) como fallback automático, pero la ruta está preparada: `AdConfig.personalizedAds = false` o `AdsController.setPersonalizedAds(false)` conmutan todas las peticiones a `npa=1`. El flujo se simula en `ConsentSimulationTest` con escenarios EEA (aceptar, rechazar, error). Para depurar el formulario se pueden definir `UMP_DEBUG_GEOGRAPHY` (`EEA`, `NOT_EEA`, `REGULATED_US_STATE`, `OTHER`) y `UMP_TEST_DEVICE_ID`.

Quedan intervenciones humanas antes de publicar (detalle en `docs/PUBLISHING-CHECKLIST.md`):

- Publicar el mensaje de privacidad GDPR/regiones reguladas en la consola de AdMob (Privacidad y mensajes), con la política enlazada.
- Confirmar el nombre de quien responde en la política (el correo ya está actualizado a `anibalortiz.dev@gmail.com`); el dominio ya está publicado.
- Completar Data Safety en Play Console: datos de anuncios de Google, correo y mensaje opcionales del menú de comentarios, y código, identificador de activación, modelo e IP enviados al servicio de verificación; sin backend propio ni analítica propia.
- Verificar en la ficha de AdMob que el dominio del desarrollador es `anibalortizdev.github.io`, que ya sirve `app-ads.txt` con el publisher real (no se genera un archivo de relleno).

Permisos declarados: `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` y `SCHEDULE_EXACT_ALARM`. `USE_EXACT_ALARM` se elimina con `tools:node="remove"` y `MobileAdsInitProvider` queda desactivado para que el SDK no se inicialice solo.

## Copias de seguridad

`android:allowBackup="true"` está acotado por `app/src/main/res/xml/backup_rules.xml` (API < 31) y `app/src/main/res/xml/data_extraction_rules.xml` (API 31+). Se excluyen de la nube y de la transferencia de dispositivo los DataStore de recompensas, perfil y acceso (`ad_rewards`, `guia_heartopia`, `content_checks`, `license`) y las SharedPreferences `guia-anuncios-nav`, `guia-avisos` y `content-sync`. El receptor de arranque `BootReminderReceiver` exige `android.permission.RECEIVE_BOOT_COMPLETED`.

## Firma de release

```bash
./gradlew :app:bundleRelease --console=plain
```

La firma no vive en el repositorio. Para el AAB de Play se usan `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` y `RELEASE_KEY_PASSWORD`; también existe la ruta `upload` con `upload.properties` en `~/.wikifan-heartopia/`. Para la distro se usan `DISTRO_STORE_PASSWORD`, `DISTRO_KEY_ALIAS` y `DISTRO_KEY_PASSWORD` con el keystore `~/.wikifan-heartopia/release.jks`. Todas se leen con `secret(...)` desde `local.properties` o el entorno.

Si no hay secretos de firma, `bundleRelease`/`assembleRelease` **fallan**; solo se permite firmar con el keystore de debug mediante `-PallowDebugSignedRelease=true`, y ese artefacto no es publicable. `testDebugUnitTest`, `lintDebug`, `verifyReleaseManifest` y `assembleDebug` no se ven afectados.

`keystore.properties` y `CREDENCIALES-UPLOAD.txt` están ignorados por git. No subas keystores, contraseñas ni credenciales al repositorio.

## Contenido pendiente de verificar

- Nombres, comidas y comederos de animales.
- Catálogo de coleccionables y códigos.
- Clima, servidor de origen, lugares y aparición de flurita y Liuliu.
- Niveles oficiales de aficiones, si el juego los tiene.

El país y la zona se eligen en la bienvenida y en Ajustes; el calendario usa esa misma selección y no pide permiso de ubicación. Las fuentes citadas en el histórico son Tidebell y otras guías de la comunidad; «flurita» y «Liuliu» no se corrigen porque no hay fuente oficial que fije su nombre en español, lugar u horario.

## Estructura

```text
app/src/main/java/com/guiaheartopia/app
  MainActivity.kt          raíz Compose, navegación e inicio de anuncios
  ads/                     UMP, banners e historial de navegación
  data/                    DataStore, repositorio y descarga del manifiesto remoto
  domain/                  perfil, catálogos, clima, búsqueda y reglas de avisos
  reminders/               receptores de arranque/cambio de hora y WorkManager
  ui/                      pantallas Compose (codes, guide, hobbies, home, map,
                           settings, theme, weather, welcome, …)
app/src/main/assets/legal   privacidad y términos que también abre Ajustes
app/src/main/res/xml        reglas de copia de seguridad y FileProvider
```

La persistencia es DataStore Preferences. El contenido remoto se guarda en el almacenamiento interno de la app.
> Flujo probado desde OpenCode Mobile.
