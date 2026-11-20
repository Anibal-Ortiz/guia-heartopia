# Publicar la política v1.1.3 en el sitio de contenido

El texto local ya está actualizado en `docs/politica-privacidad.md` (v1.1.3 — 20 de noviembre de 2026). La web se publica desde el repo que sirve https://anibalortizdev.github.io/guia-heartopia-contenido/. Archivo a editar: `index.html`. Cuatro cambios:

## 1. Versión

La web tiene ahora:

```html
<p>Versión de la política: <strong>1.1.0</strong> — 18 de noviembre de 2026<br>
```

sustituir por:

```html
<p>Versión de la política: <strong>1.1.3</strong> — 20 de noviembre de 2026<br>
```

## 2. Párrafo «Qué es esta app»

Donde dice `No hay cuenta de usuario ni servidor propio que reciba tu información personal.` sustituir por:

```html
No hay cuenta de usuario ni servidor propio. Solo si tú decides escribir al desarrollador desde el menú de comentarios, tu mensaje se envía a un servicio de formularios que lo reenvía por correo.
```

## 3. Sección nueva (insertar justo antes de `<h2>Permisos</h2>`)

```html
  <h2>Comentarios y preguntas</h2>
  <p>Si usas el menú «Comentarios y preguntas», la app envía por HTTPS el texto que escribas y, solo si lo indicas, el correo que introduzcas para que el desarrollador pueda responderte. El envío va a un formulario de <strong>Google Forms</strong> alojado en la cuenta del desarrollador; la app no abre el correo ni el navegador y no guarda una copia del mensaje en tu dispositivo ni en un servidor propio. Las respuestas quedan en ese formulario, al que solo tiene acceso el desarrollador.</p>
  <p>Estos mensajes no se usan para publicidad, no se comparten con otros usuarios y no sirven para seguimiento entre apps. Puedes pedir la rectificación o el borrado de lo que enviaste escribiendo a ese mismo correo.</p>
```

## 4. Permisos

```html
<li><code>INTERNET</code> y <code>ACCESS_NETWORK_STATE</code>: contenido público y anuncios.</li>
```

por:

```html
<li><code>INTERNET</code> y <code>ACCESS_NETWORK_STATE</code>: contenido público, anuncios y envío de comentarios.</li>
```

Después de subir el `index.html`, verifica que https://anibalortizdev.github.io/guia-heartopia-contenido/ muestre «1.1.3 — 20 de noviembre de 2026» y el apartado «Comentarios y preguntas». La app abre esa misma URL desde «Política de privacidad», así que no hace falta tocar el APK.

## Qué me falta para poder publicarlo yo

1. **La URL real del repo del sitio.** Probé `https://github.com/Anibal-Ortiz/guia-heartopia-contenido.git` y responde *Repository not found*; puede llamarse de otro modo, estar bajo otra cuenta o ser privado. Con la URL correcta puedo clonarlo.
2. **Acceso de escritura en este equipo.** Necesito que estés autenticado con `gh auth login` o un token con permisos sobre ese repo (`repo`). No guardes tokens dentro de este repositorio.
3. **La rama que publica GitHub Pages** (`main`, `gh-pages` o `docs/`) y confirmación de que `index.html` vive en la raíz de esa rama (ahora mismo es así en la web publicada).
4. **Tu autorización para hacer commit y push en ese repo de contenido.** No haría ningún commit en `guia-heartopia-final`.

Con esos cuatro datos: clono el repo, aplico los cuatro cambios, hago commit y push; GitHub Pages publica el texto en unos minutos.
