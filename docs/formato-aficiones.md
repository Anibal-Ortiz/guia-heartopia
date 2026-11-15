# Formato de Huerto y Cocina

La app no trae cultivos, flores ni recetas. Esas pantallas leen el paquete remoto cuando exista, con el mismo manifiesto, `contentVersion` y SHA-256 que el resto de los archivos. Mientras el archivo no esté publicado, la pantalla dice «Próximamente».

Rutas previstas:

- `content/huerto/siembras.json`
- `content/huerto/flores.json`
- `content/cocina/platos.json`
- `content/cocina/recetas.json`

Cada archivo puede ser una lista o un objeto `{ "items": [ ... ] }`. No se inventan campos que falten: si no vienen, no se muestran.

```json
[
  {
    "id": "opcional",
    "nombre": "Nombre en español",
    "imagen": "huerto/images/ejemplo.webp",
    "ingredientes": ["opcional"],
    "tiempoCrecimiento": "opcional",
    "temporada": "opcional",
    "precioVenta": "opcional"
  }
]
```

`nombre` es el único campo que hace visible la ficha. `imagen`, `ingredientes`, `tiempoCrecimiento`, `temporada` y `precioVenta` son opcionales. Una afición nueva se suma con otra tarjeta en la pantalla Aficiones; no hace falta cambiar la barra de abajo.
