# Monetización (AdMob)

La app ya llama a AdMob (banner, intersticial y dos videos recompensados) y a UMP. En release, si los IDs reales no están, Gradle deja los IDs de prueba de Google para poder instalar un build. **Esos IDs de prueba no sirven para la ficha de producción.** Hay que pegar los cinco IDs de la app real antes de subir a Play.

## Dónde pegar los 5 IDs

En `local.properties` (no se sube a git), o en las variables de entorno con el mismo nombre:

| Qué es en AdMob | Clave | Dónde se usa |
| --- | --- | --- |
| ID de aplicación | `ADMOB_APP_ID` | `AndroidManifest` de release (`APPLICATION_ID`) y `BuildConfig.ADMOB_APP_ID` |
| Banner | `ADMOB_BANNER_AD_UNIT_ID` | `BuildConfig.ADMOB_BANNER_UNIT_ID` |
| Intersticial | `ADMOB_INTERSTITIAL_AD_UNIT_ID` | `BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID` |
| Video recompensado del calendario | `ADMOB_REWARDED_CALENDAR_AD_UNIT_ID` | `BuildConfig.ADMOB_REWARDED_CALENDAR_UNIT_ID` |
| Video recompensado de 24 h sin anuncios | `ADMOB_REWARDED_ADFREE_AD_UNIT_ID` | `BuildConfig.ADMOB_REWARDED_ADFREE_UNIT_ID` |

Ejemplo (sustituye por los valores de la consola, sin comillas de más):

```properties
ADMOB_APP_ID=ca-app-pub-5182407366716972~XXXXXXXXXX
ADMOB_BANNER_AD_UNIT_ID=ca-app-pub-5182407366716972/XXXXXXXXXX
ADMOB_INTERSTITIAL_AD_UNIT_ID=ca-app-pub-5182407366716972/XXXXXXXXXX
ADMOB_REWARDED_CALENDAR_AD_UNIT_ID=ca-app-pub-5182407366716972/XXXXXXXXXX
ADMOB_REWARDED_ADFREE_AD_UNIT_ID=ca-app-pub-5182407366716972/XXXXXXXXXX
```

El publisher de esta cuenta es **pub-5182407366716972**. Los bloques `~` y `/` salen al crear la app y las unidades en AdMob. Después de pegarlos, vuelve a generar el `.aab`: un ID vacío o con texto de ejemplo (`xxxx`, `placeholder`, `tu_id`) hace que release siga usando los anuncios de prueba.

En AdMob también hay que publicar el mensaje de privacidad (UMP) en Privacidad y mensajes, para el EEE y el resto de regiones donde Google lo pida. La política que debe enlazar ese mensaje es https://anibalortizdev.github.io/guia-heartopia-contenido/

## app-ads.txt

No hace falta crear otro archivo. Ya está publicado en:

https://anibalortizdev.github.io/app-ads.txt

El publisher que debe figurar ahí, y el de las unidades de esta app, es **pub-5182407366716972**. En la ficha de AdMob, el dominio del desarrollador tiene que ser el que sirve ese `app-ads.txt` (`anibalortizdev.github.io`), no un archivo nuevo dentro de este repositorio.
