# SDK que exige Play

Desde el 31 de agosto de 2026, Google Play rechaza apps nuevas y actualizaciones con `targetSdk` menor que **36** (Android 16). La prórroga terminó el 1 de noviembre de 2026. Esta app declara `targetSdk` **36** y `minSdk` **26** (Android 8). El 26 deja fuera solo equipos muy viejos y alcanza para notificaciones en tiempo de ejecución (Android 13) y el borde a borde.

`compileSdk` queda en **37** porque la AndroidX que ya usa el proyecto (Compose, Navigation y el resto del BOM de 2026) no compila contra la API 36. Compilar contra una API más nueva no cambia el `targetSdk` del manifiesto: Play lee `targetSdkVersion`, no el `compileSdk`. Bajar esas librerías solo para forzar `compileSdk` 36 rompería el borde a borde y no cambia lo que Play comprueba.

En el APK o el AAB tiene que verse:

- `minSdkVersion` 26
- `targetSdkVersion` 36
