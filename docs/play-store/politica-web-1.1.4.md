# Publicar la política v1.1.4 en el sitio de contenido

El texto final está listo en `docs/politica-privacidad.html` (v1.1.4 — 23 de noviembre de 2026). Es una copia exacta del `index.html` que debe publicarse.

- Sitio: https://anibalortizdev.github.io/guia-heartopia-contenido/
- Repo: https://github.com/anibalortizdev/guia-heartopia-contenido (rama `main`, `index.html` en la raíz)
- El APK no necesita cambios: el botón «Política de privacidad» abre esa URL.

## Cambios de esta versión

1. Versión 1.1.3 → 1.1.4, fecha 23/11/2026.
2. Nueva viñeta «Código de acceso» en «Datos que se quedan en el dispositivo».
3. Nueva sección «Códigos de acceso» antes de «Permisos»: declara el envío del código, el fabricante y modelo del dispositivo, el identificador de activación y la dirección IP a un servicio externo de verificación, la consulta cada 72 horas y la desvinculación.
4. Permiso `INTERNET`: añade «verificación de códigos de acceso».

## Publicación

Copiar `docs/politica-privacidad.html` a `index.html` en la raíz del repo de contenido, hacer commit y push a `main`. No modificar `content/`, el manifiesto ni `contentVersion`.

```bash
cp docs/politica-privacidad.html /ruta/al/repo/guia-heartopia-contenido/index.html
git -C /ruta/al/repo/guia-heartopia-contenido add index.html
git -C /ruta/al/repo/guia-heartopia-contenido commit -m "Actualizar política de privacidad a v1.1.4"
git -C /ruta/al/repo/guia-heartopia-contenido push origin main
```

Después del push, verificar que la web muestre «1.1.4 — 23 de noviembre de 2026» y el apartado «Códigos de acceso».

## Estado

Publicada en el repo de contenido con el commit `2bd377a` (`7f6df4e..2bd377a  HEAD -> main`). GitHub Pages puede tardar unos minutos en reflejar el cambio; verifica con un parámetro `?cb=` único sobre la URL pública. No guardes tokens dentro de este repositorio.
