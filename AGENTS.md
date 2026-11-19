# AGENTS.md — guia-heartopia (WikiFan para Heartopia)

## Stack y Arquitectura
- **Plataforma:** Android nativo en **Kotlin** y **Jetpack Compose** (Material 3). La UI es 100% Compose: no existe `res/layout/activity_main.xml`.
- **Monetización:** Google **AdMob** (banners adaptativos).
- **Datos:** Catálogos e ítems cargados desde manifiesto JSON remoto alojado en GitHub Pages (`anibalortizdev.github.io`).
- **CI/CD:** GitHub Actions compila el APK debug mediante Gradle.

## Mapa de Archivos Clave (ir directo sin escanear el repo)
- `app/src/main/java/com/guiaheartopia/app/MainActivity.kt`: Raíz Compose (`Scaffold`); el banner AdMob vive en su `bottomBar`, encima de `PixelNavBar`.
- `app/src/main/java/**/Ads.kt`: Configuración, tamaño adaptativo y ciclo de vida de anuncios AdMob.
- `app/src/main/java/**/HomeScreen.kt`: Pantalla principal en Jetpack Compose, estructura de scroll y navegación.
- `app/src/main/java/com/guiaheartopia/app/ui/guide/GuideScreens.kt`: Pantallas de catálogo y filtros (bichos, peces, recetas) con barra de búsqueda reactiva.

## Reglas de Ahorro de Tokens y Contexto
1. **Exclusiones estrictas:** NUNCA listes, leas ni busques dentro de `build/`, `app/build/`, `.gradle/`, `.idea/`, `.git/` ni binarios (`.apk`, `.jar`, `.png`, `.webp`).
2. **Alcance acotado:** Lee y modifica únicamente los archivos directamente implicados en la tarea (idealmente de 1 a 3 archivos por iteración).
3. **Salida limpia en terminal:** Usa siempre `--console=plain` al ejecutar Gradle para evitar barras de progreso ANSI que llenan la ventana de contexto.

## Convenciones Técnicas Obligatorias
- **Jetpack Compose y Campos de Texto:**
    - Respeta el *state hoisting* y evita recomposiciones innecesarias.
    - En barras de búsqueda y filtros (`TextField` / `OutlinedTextField`), mantén el estado del texto síncrono en memoria (sin retrasos de `Flow`/IO antes de actualizar el `value` del input) para evitar que el cursor se reinicie al principio al escribir.
    - El banner AdMob está en el `bottomBar` del `Scaffold` de `MainActivity.kt`, que reserva su espacio: **no** añadas `contentPadding` inferior adicional en `LazyColumn` / `LazyVerticalGrid` para compensar el banner, porque lo dejaría con un hueco doble. Usa solo el padding normal de la pantalla.
- **AdMob (`ads/Ads.kt` y `MainActivity.kt`):**
    - No modifiques IDs de aplicación ni IDs de bloques de anuncios (`ca-app-pub-...`).
    - Cualquier ajuste de responsividad del banner debe mantener el contenedor visible y cumplir políticas de AdMob (sin superposición sobre elementos interactivos).
- **Archivos protegidos:** No modifiques `build.gradle.kts`, `libs.versions.toml`, `gradle.properties`, `AndroidManifest.xml` ni workflows de `.github/` salvo que se pida explícitamente.

## Compilación y Verificación (Windows)
- **Compilar APK Debug:**
  ```powershell
  .\gradlew.bat assembleDebug --console=plain
  ```

- **Credenciales protegidas:** NUNCA leas, muestres ni modifiques archivos `.jks`, `.keystore`, `keystore.properties` ni `CREDENCIALES-UPLOAD.txt`.