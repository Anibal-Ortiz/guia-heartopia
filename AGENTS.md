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

## Publicación de contenido (repo `anibalortizdev/guia-heartopia-contenido`)
- El sitio `https://anibalortizdev.github.io/guia-heartopia-contenido/` se publica desde el repo **`anibalortizdev/guia-heartopia-contenido`**, rama `main`, con `content/` en la raíz. El repo `Anibal-Ortiz/guia-heartopia-contenido` **no existe**.
- **`content/manifest.json` debe calcular `size` y `sha256` desde los BLOBS de Git**, no desde los archivos del área de trabajo:
  ```bash
  git cat-file -p HEAD:content/<ruta>   # bytes exactos que sirve GitHub Pages
  ```
  En Windows, `core.autocrlf` convierte los saltos a CRLF en el disco; si se hashea ese archivo, Pages sirve LF y la app da `VERIFY_FAILED`. Recalcular **todas** las entradas desde blobs antes de subir el manifiesto.
- GitHub Pages/CDN cachea por ruta: las descargas de la app llevan `?cb=<timestamp>` (ver `AndroidContentStore.httpGet`). Para verificar desde fuera, usar también un query único; tras un push, Pages tarda unos minutos en reflejar el cambio.
- **Slugs de archivos:** usar siempre `ReminderIds.slug()` para nombrar imágenes de ingredientes. Colapsa `--` en `-` y quita guiones al principio/final: `Dulce (cualquier color)` → `dulce-cualquier-color.webp` (NO `dulce--cualquier-color-.webp`). Un slug mal calculado deja la imagen genérica.
- Cuando se suban imágenes nuevas, **no tocar `contentVersion`** hasta que se pida explícitamente. Cuando se pida: recalcular hashes desde blobs, subir `contentVersion` y verificar con `?cb=` que Pages devuelve esa versión.

## Compilación y Verificación (Windows)
- **Compilar APK Debug:**
  ```powershell
  .\gradlew.bat assembleDebug --console=plain
  ```

- **Credenciales protegidas:** NUNCA leas, muestres ni modifiques archivos `.jks`, `.keystore`, `keystore.properties` ni `CREDENCIALES-UPLOAD.txt`.