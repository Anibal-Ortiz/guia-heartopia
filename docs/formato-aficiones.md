# Formato de Huerto y Cocina

La app no trae cultivos, flores ni recetas en el APK. Esas pantallas leen el paquete remoto cuando exista, con el mismo manifiesto, `contentVersion` y SHA-256 que el resto de los archivos. Mientras el archivo no esté publicado, la pantalla dice «Próximamente».

Rutas (relativas a la carpeta `content/` del sitio, sin prefijo en el manifiesto):

- `huerto/siembras.json`
- `huerto/flores.json`
- `cocina/recetas.json`

Cada archivo puede ser una lista o un objeto `{ "items": [ ... ] }`. **No se inventan campos que falten**: si no vienen en el JSON, la pantalla no los muestra. Solo nombres en español.

## Compatibilidad con apps antiguas

El `minAppVersionCode` del manifiesto global sigue siendo el de la app que publica el paquete. Los archivos de Huerto y Cocina deben incluir **`minAppVersionCode` por entrada** (38 con la 1.0.5) para que instalaciones 1.0.3 o 1.0.4 sigan descargando catálogo y calendario sin quedarse bloqueadas.

Ejemplo de entrada en `manifest.json`:

```json
{
  "path": "huerto/siembras.json",
  "size": 1234,
  "sha256": "...",
  "minAppVersionCode": 38
}
```

## `huerto/siembras.json`

Campos por ficha:

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `id` | string | sí |
| `nombre` | string | sí |
| `nivelSiembra` | entero 0–13 | sí |
| `precioSemilla` | entero | no |
| `fuenteSemilla` | string | no |
| `tiempoCrecimientoMinutos` | entero | sí (para temporizador) |
| `precioVentaEstrellas` | lista de 5 enteros | no |
| `categoria` | string | no |
| `evento` | string | no |
| `maestriaCosechas` | string | no |
| `imagen` | ruta relativa (arte propio, no del juego) | no |

## `huerto/flores.json`

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `id`, `nombre` | string | sí |
| `nivelSiembra` | entero | no |
| `variantes` | lista | sí si hay datos |
| `variantes[].color` | string | sí |
| `variantes[].origen` | `"tienda"` \| `"cruce"` | sí |
| `variantes[].estrellas` | entero | no |
| `variantes[].precioVenta` | entero | no |
| `imagen` | string | no |

## `cocina/recetas.json`

| Campo | Tipo | Obligatorio |
|-------|------|-------------|
| `id`, `nombre` | string | sí |
| `nivelCocina` | entero 0–13 | sí |
| `energiaEstrellas` | lista de 5 enteros | no |
| `precioVentaEstrellas` | lista de 5 enteros | no |
| `ingredientes` | lista | no |
| Ingrediente fijo | `{ "idSiembra" o "nombre", "cantidad" }` | — |
| Ingrediente «elige N» | `{ "elegir": N, "opciones": [{ "idSiembra" o "nombre" }, ...] }` | — |
| `categoria` | string | no |
| `evento` | string | no |
| `maestria` | string | no |
| `imagen` | string | no |

Las pruebas de lectura usan JSON de ejemplo solo en `app/src/test`, nunca en el APK ni en `assets`.