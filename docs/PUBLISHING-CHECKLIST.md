# Checklist antes de publicar

Estado del proyecto: la app compila (`assembleDebug`), pasa los 163 tests unitarios y `lintDebug` sin errores. La variante release **falla a propósito** si no hay secretos de firma (usa `-PallowDebugSignedRelease=true` solo para una prueba local no publicable).

## Ya resuelto en el repositorio / en la web

- Dominio del desarrollador: `anibalortizdev.github.io`.
- Política de privacidad publicada (v1.1.0, 18/11/2026): https://anibalortizdev.github.io/guia-heartopia-contenido/
- El botón «Política de privacidad» de la app abre esa URL (con copia local incluida en el APK como respaldo).
- `app-ads.txt`: publicado en https://anibalortizdev.github.io/app-ads.txt con el publisher real. **No** crear un archivo con un ID de relleno.
- Respuestas de Data Safety, IARC, público 13+ y anuncios: `docs/play-store/formularios.md`.
- Textos de la ficha: `docs/play-store/ficha.md`.
- Procedimiento de firma y copias de la clave de subida: `docs/play-store/firma.md`.
- IDs de AdMob de producción: `docs/monetizacion.md` y las claves `ADMOB_*` en `local.properties`/entorno. Nunca versionar esos valores.

## Pendiente de acción humana (fuera del código)

1. **Mensaje de privacidad UMP.** Crearlo y **publicarlo** en AdMob → *Privacidad y mensajes*, enlazando la política de privacidad. Sin mensaje publicado, el formulario no llega a usuarios del EEE/EE. UU. regulado. Verificar con `UMP_DEBUG_GEOGRAPHY` en debug.
2. **Política de privacidad.** Ya publicada y con el correo `anibalortiz.dev@gmail.com`. Opcional: sustituir «su desarrollador» por el nombre completo del responsable en `docs/politica-privacidad.md` y en la web.
3. **Data Safety en Play Console.** Copiar las respuestas de `docs/play-store/formularios.md`: ID de publicidad y datos técnicos recogidos por Google AdMob (terceros), sin backend propio ni analítica propia. Enviar el formulario. La URL de política de la ficha debe ser la misma que abre el botón de la app.
4. **Ficha de AdMob.** Confirmar que el dominio del desarrollador es `anibalortizdev.github.io` para que Play/AdMob encuentren el `app-ads.txt`.
5. **Secretos de firma.** Tener disponibles (fuera de git) `RELEASE_STORE_*` o `~/.wikifan-heartopia/upload.properties`, y `DISTRO_STORE_PASSWORD` / `DISTRO_KEY_ALIAS` / `DISTRO_KEY_PASSWORD` si se usa la distro. Hacer dos copias de la keystore de subida.
6. **Versión.** Ya subida a `versionCode` 43 / `appVersion` 1.1.0 en `app/build.gradle.kts`. El nombre del artefacto y el `versionName` se derivan de `appVersion`.
7. **Generar el bundle.** `.\gradlew.bat bundleRelease --console=plain` con los secretos anteriores. El output queda en `app/build/outputs/bundle/release/`.

## Verificación automática disponible

```powershell
.\gradlew.bat verifyReleaseManifest testDebugUnitTest lintDebug assembleDebug --console=plain
```

`verifyReleaseManifest` valida el manifiesto de release generado sin necesitar secretos; la CI (`.github/workflows/build.yml`) ejecuta esas mismas comprobaciones con `permissions: contents: read`, concurrencia y límite de tiempo.
