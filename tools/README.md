# Publicar el contenido de Guía Heartopia

Esta guía es para subir peces, aves, insectos y clima a internet, sin instalar una app nueva. No hace falta saber programar. Si un paso no sale, para y vuelve a leerlo: no borres nada del teléfono.

El paquete 2 no lleva imágenes de fichas. La app dibuja un ícono por categoría. Solo se sube una imagen si más adelante se genera el paquete con `--con-imagenes` y ese archivo es propio, no arte del juego.

La app busca los archivos en una carpeta llamada `content`. Por eso el paquete trae dos copias iguales:

- `manifest.json` en la raíz, para abrirlo en el navegador y comprobar que el sitio responde.
- la carpeta `content`, con el mismo `manifest.json`, las criaturas, el clima y las imágenes. Esa es la copia que descarga la app.

## Qué es cada archivo

- `manifest.json` es la lista. Dice el número de versión del contenido, la fecha y el nombre de cada archivo con su tamaño y una huella SHA-256.
- `catalog/creatures.json` son los peces, aves, insectos y el resto de criaturas.
- `calendar/clima-2026.json` es el clima de 2026. Un año nuevo sería `calendar/clima-2027.json`.
- `catalog/images` no va en el paquete normal. Si existiera, serían archivos propios, no dibujos del juego.

## 1. Crear el repositorio (solo la primera vez)

1. Entra en GitHub con tu cuenta.
2. Pulsa el botón **+** (arriba a la derecha) y elige **New repository**.
3. En **Repository name** escribe exactamente: `guia-heartopia-contenido`
4. Márcalo como **Public**.
5. Marca **Add a README file** para que el repositorio tenga una rama.
6. Pulsa **Create repository**.

La dirección queda así: https://github.com/anibalortizdev/guia-heartopia-contenido

## 2. Activar GitHub Pages (solo la primera vez)

1. En ese repositorio, abre **Settings**.
2. En el menú de la izquierda, entra en **Pages**.
3. En **Build and deployment**, elige **Deploy from a branch**.
4. En **Branch** elige `main` y la carpeta **/ (root)**.
5. Pulsa **Save**.

GitHub tarda unos minutos la primera vez. La dirección del sitio será:

https://anibalortizdev.github.io/guia-heartopia-contenido/

## 3. Subir el paquete desde el navegador

1. Descarga el ZIP del paquete y descomprímelo. Tiene que verse `manifest.json` suelto, más las carpetas `catalog`, `calendar` y `content`. No tiene que haber una carpeta `catalog/images`.
2. Abre https://github.com/anibalortizdev/guia-heartopia-contenido
3. Pulsa **Add file** y luego **Upload files**.
4. Arrastra `manifest.json` y las carpetas `catalog`, `calendar` y `content`. No arrastres el ZIP.
5. `manifest.json` tiene que quedar en la raíz del repositorio, no dentro de otra carpeta que hayas creado sin querer.
6. Abajo, en **Commit changes**, pulsa **Commit changes**.

Cuando termine, en la página principal del repositorio se ve `manifest.json` al mismo nivel que la carpeta `content`.

## 4. Comprobar que el sitio abre

Espera uno o dos minutos después del commit. Luego abre esta dirección en Chrome:

https://anibalortizdev.github.io/guia-heartopia-contenido/manifest.json

Tiene que verse un texto que empieza así:

```json
{
  "contentVersion": 2,
  "minAppVersionCode": 25,
  "publishedOn": "2026-11-10",
  "files": [
```

Si sale un error 404, espera un poco más y recarga. Si sigue igual, revisa que Pages esté en la rama `main` y la carpeta `/ (root)`, y que `manifest.json` esté en la raíz del repositorio, no dentro de `content` solamente.

La app usa esta otra copia, que debe mostrar el mismo texto:

https://anibalortizdev.github.io/guia-heartopia-contenido/content/manifest.json

## 5. Publicar una actualización

El número `contentVersion` tiene que subir. Si publicas otro paquete con el mismo número, la app no cambia nada.

En el computador donde está el proyecto de la app, abre una terminal en la carpeta del proyecto y ejecuta:

```bash
python3 tools/publicar_contenido.py
```

Ese comando lee los datos del proyecto, suma 1 a la versión y vuelve a escribir la carpeta `build/contenido-publicado`. La primera vez que lo corras después del paquete inicial, la versión queda en 2.

Sube otra vez los archivos que cambiaron, sobre todo `manifest.json` y `content/manifest.json`. En GitHub, **Add file > Upload files** reemplaza un archivo si lo subes de nuevo con el mismo nombre y guardas el commit.

### Un pez nuevo

1. Abre `app/src/main/assets/catalog/heartodex.json`.
2. Copia una ficha de pez completa (desde una `{` hasta la `}` que cierra esa ficha).
3. Cámbiale el `id` (no puede repetirse) y el `name`.
4. Deja `imageUrl` si ya existe, pero no añadas un dibujo del juego. Sin archivo de imagen, la app muestra el ícono de la categoría.
5. Ejecuta `python3 tools/publicar_contenido.py`.
6. Sube al repositorio el `manifest.json` nuevo, `catalog/creatures.json` y las mismas copias dentro de la carpeta `content`.

La app, con wifi, lo baja sola en un plazo de un día. También puedes abrir Ajustes y pulsar **Buscar actualizaciones**. El pez aparece en la lista, en la búsqueda y en **Ahora** si le toca el horario y el clima.

### El clima de 2027

1. Crea `app/src/main/assets/calendar/clima-2027.json`.
2. Usa la misma forma que `clima-2026.json`: una lista `days`, y en cada día `date` más `slots` con `start`, `end`, `kind` y `status`. Las horas son las del reloj local, como las de 2026. Ejemplo de un solo día:

```json
{
  "reviewedOn": "2027-01-01",
  "days": [
    {
      "date": "2027-01-01",
      "slots": [
        { "start": "00:00", "end": "24:00", "kind": "lluvia", "status": "incluido" }
      ]
    }
  ]
}
```

3. Ejecuta `python3 tools/publicar_contenido.py`.
4. Sube el `manifest.json` nuevo y `calendar/clima-2027.json`, en la raíz y también dentro de `content`.

En el calendario de la app, 2027 muestra ese clima. 2026 se queda como estaba.

## Si algo sale mal

No pasa nada en el teléfono: la app sigue con el contenido anterior y no muestra un error. Revisa el `manifest.json` del sitio. `contentVersion` tiene que ser mayor que el de la vez anterior, y `minAppVersionCode` no puede ser mayor que el `versionCode` de la app instalada.
