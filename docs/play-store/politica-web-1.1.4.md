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

## Estado en este equipo

El repo de contenido se puede clonar, pero el push devuelve `403: Permission to anibalortizdev/guia-heartopia-contenido.git denied`. Las credenciales disponibles pertenecen a `Anibal-Ortiz` y no tienen escritura en esa cuenta. Para publicar hace falta una cuenta con acceso o un token con permiso `repo` sobre ese repo; no guardes tokens dentro de este repositorio.
