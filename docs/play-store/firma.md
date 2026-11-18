# Firma para Google Play

## Play App Signing

La app se publica con **Play App Signing**:

1. En Play Console, al crear la app, activa Play App Signing.
2. Elige que **Google genere y custodie la clave de firma de la app**. Esa clave no está en este repositorio ni en el ordenador de compilación. Es la que firma lo que Play entrega a los teléfonos.
3. Sube el Android App Bundle firmado con la **clave de subida** (upload key). Play comprueba esa firma y vuelve a firmar el paquete con la clave de la app.

La variante `release` de Gradle usa, en este orden: una keystore indicada en `local.properties` (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`); si no hay, la clave de subida en `~/.wikifan-heartopia/`; si tampoco, la distro en `~/.wikifan-heartopia/release.jks` con sus secretos `DISTRO_*`. **Si no hay ningún secreto de firma, la compilación release falla**: ya no se cae silenciosamente a la keystore de debug. Solo para una prueba local no publicable se puede forzar con `-PallowDebugSignedRelease=true`. El bundle que se entrega para Play debe estar firmado con la clave de subida.

## Por qué hay que respaldarla

Sin la clave de subida no puedes firmar una actualización y Play la rechaza. Pedir un restablecimiento en Play Console existe, pero no es inmediato y puede denegarse. Mientras tanto la app no se puede actualizar. Si se pierde y no hay copia, el riesgo real es quedarte sin poder publicar versiones nuevas de esta ficha.

La clave de firma de la app, si Google la generó, no la tienes tú: no hace falta (ni se debe) exportarla. Lo que hay que guardar es la clave de subida.

## Cómo respaldarla

La keystore y las contraseñas **no van en git** (`.gitignore` ignora `*.jks`). Están solo en:

- `~/.wikifan-heartopia/upload-key.jks`
- `~/.wikifan-heartopia/upload.properties` (alias y contraseñas)

Haz al menos dos copias fuera del portátil y fuera del repositorio:

1. Un gestor de contraseñas, con el archivo `.jks` adjunto y el alias, la contraseña del almacén y la de la clave.
2. Un disco cifrado offline (no un chat ni un issue).

Comprueba la copia abriendo el almacén:

```bash
keytool -list -keystore upload-key.jks
```

Tiene que mostrar el mismo alias y la misma huella del certificado que la primera entrega. Si cambias de ordenador, copia esos dos archivos al mismo directorio del usuario antes de `bundleRelease`, o define `RELEASE_STORE_*` en `local.properties` apuntando a esa keystore.

No subas la keystore a GitHub, a la ficha de Play como captura, ni a un ZIP público del código.
