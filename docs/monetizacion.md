# Monetización (AdMob)

La app llama a AdMob (banner, intersticial y dos videos recompensados) y a UMP. En **release** se usan los IDs reales de `local.properties` (o variables de entorno). En **debug** solo los IDs oficiales de prueba de Google.

## IDs de producción (cuenta pub-5182407366716972)

| Qué es en AdMob | Clave en `local.properties` | ID |
| --- | --- | --- |
| ID de aplicación | `ADMOB_APP_ID` | `ca-app-pub-5182407366716972~9800170561` |
| Banner | `ADMOB_BANNER_AD_UNIT_ID` | `ca-app-pub-5182407366716972/4302312201` |
| Intersticial | `ADMOB_INTERSTITIAL_AD_UNIT_ID` | `ca-app-pub-5182407366716972/1514311421` |
| Video recompensado de 24 h sin anuncios | `ADMOB_REWARDED_ADFREE_AD_UNIT_ID` | `ca-app-pub-5182407366716972/8509150522` |
| Video recompensado del calendario | `ADMOB_REWARDED_CALENDAR_AD_UNIT_ID` | `ca-app-pub-5182407366716972/3256823846` |

En release el `ADMOB_APP_ID` también va al meta-data `com.google.android.gms.ads.APPLICATION_ID` del manifiesto generado. Las demás claves alimentan `BuildConfig`.

Otros builds: copia estas cinco líneas en `local.properties` (no está en git) o exporta las mismas variables de entorno antes de `bundleRelease`.

En AdMob hay que tener publicado el mensaje de privacidad (UMP) en Privacidad y mensajes. La política enlazada: https://anibalortizdev.github.io/guia-heartopia-contenido/

## app-ads.txt

No hace falta crear otro archivo. Ya está publicado en:

https://anibalortizdev.github.io/app-ads.txt

El publisher es **pub-5182407366716972**. En la ficha de AdMob, el dominio del desarrollador tiene que ser el que sirve ese `app-ads.txt` (`anibalortizdev.github.io`).
