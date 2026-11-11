# Alineación de 16 KB (v1.0.0, versionCode 33)

Comprobado sobre `Guia-Heartopia-1.0.0-release.apk` y el `.aab` firmado con la clave de subida.

`aapt dump badging`:

- `minSdk` / `sdkVersion`: 26
- `targetSdkVersion`: 36
- `compileSdkVersion`: 37 (AndroidX de este proyecto no compila contra 36; Play valida el target)

`apksigner verify --print-certs`: esquema V2, certificado `CN=WikiFan Heartopia Upload`.

`zipalign -c -P 16 -v 4` en el APK: **Verification successful**.

El mismo `zipalign -c -P 16` sobre el `.aab`: **Verification successful**.

Bibliotecas nativas (LOAD `p_align` ≥ 16384) en las cuatro ABI:

| Biblioteca | arm64-v8a | armeabi-v7a | x86_64 | x86 |
| --- | --- | --- | --- | --- |
| libandroidx.graphics.path.so | 16384 | 16384 | 16384 | 16384 |
| libdatastore_shared_counter.so | 16384 | 16384 | 16384 | 16384 |

No hay `.so` con alineación de 4 KB.
