# Checklist antes de publicar

Estado del proyecto: la app compila (`assembleDebug`), pasa los 166 tests unitarios y `lintDebug` sin errores. La variante release **falla a propósito** si no hay secretos de firma (usa `-PallowDebugSignedRelease=true` solo para una prueba local no publicable).

## Ya resuelto en el repositorio / en la web

- Dominio del desarrollador: `anibalortizdev.github.io`.
- Política de privacidad: la web publicada sigue en v1.1.3; la v1.1.4 está lista en `docs/politica-privacidad.html` y pendiente de subir a https://anibalortizdev.github.io/guia-heartopia-contenido/ (ver `docs/play-store/politica-web-1.1.4.md`).
- El botón «Política de privacidad» de la app abre esa URL (con copia local incluida en el APK como respaldo).
- `app-ads.txt`: publicado en https://anibalortizdev.github.io/app-ads.txt con el publisher real. **No** crear un archivo con un ID de relleno.
- Respuestas de Data Safety, IARC, público 13+ y anuncios: `docs/play-store/formularios.md`.
- Textos de la ficha: `docs/play-store/ficha.md`.
- Procedimiento de firma y copias de la clave de subida: `docs/play-store/firma.md`.
- IDs de AdMob de producción: `docs/monetizacion.md` y las claves `ADMOB_*` en `local.properties`/entorno. Nunca versionar esos valores.

## Pendiente de acción humana (fuera del código)

1. **Mensaje de privacidad UMP.** Crearlo y **publicarlo** en AdMob → *Privacidad y mensajes*, enlazando la política de privacidad. Sin mensaje publicado, el formulario no llega a usuarios del EEE/EE. UU. regulado. Verificar con `UMP_DEBUG_GEOGRAPHY` en debug.
2. **Política de privacidad.** La v1.1.4 (23/11/2026) añade el apartado «Códigos de acceso» y la viñeta de datos locales. El texto final está en `docs/politica-privacidad.html`; queda subirlo como `index.html` al repo de contenido. El push desde este equipo devuelve `403` (sin escritura en `anibalortizdev`): hace falta una cuenta con acceso. No hace falta tocar el APK.
3. **Data Safety en Play Console.** Copiar las respuestas de `docs/play-store/formularios.md`: ID de publicidad y datos técnicos recogidos por Google AdMob (terceros), el correo opcional y el mensaje del menú de comentarios a Google Forms (tercero: Google), y el código de acceso, el fabricante/modelo, el identificador de activación y la IP enviados al servicio externo de verificación (tercero). No hay backend propio ni analítica propia. Enviar el formulario. La URL de política de la ficha debe ser la misma que abre el botón de la app y estar actualizada a la versión 1.1.4 (23/11/2026).
4. **Ficha de AdMob.** Confirmar que el dominio del desarrollador es `anibalortizdev.github.io` para que Play/AdMob encuentren el `app-ads.txt`.
5. **Secretos de firma.** Tener disponibles (fuera de git) `RELEASE_STORE_*` o `~/.wikifan-heartopia/upload.properties`, y `DISTRO_STORE_PASSWORD` / `DISTRO_KEY_ALIAS` / `DISTRO_KEY_PASSWORD` si se usa la distro. Hacer dos copias de la keystore de subida.
6. **Versión.** Ya aplicada en `app/build.gradle.kts`: `versionCode` 46 / `appVersion` 1.1.3. El nombre del artefacto y el `versionName` se derivan de `appVersion`.
7. **Generar el bundle.** `.\gradlew.bat bundleRelease --console=plain` con los secretos anteriores. El output queda en `app/build/outputs/bundle/release/`.

## Verificación automática disponible

```powershell
.\gradlew.bat verifyReleaseManifest testDebugUnitTest lintDebug assembleDebug --console=plain
```

`verifyReleaseManifest` valida el manifiesto de release generado sin necesitar secretos; la CI (`.github/workflows/build.yml`) ejecuta esas mismas comprobaciones con `permissions: contents: read`, concurrencia y límite de tiempo.
