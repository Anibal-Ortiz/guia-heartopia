# Publicar la política y los términos v1.1.8

Los textos finales están en este repositorio y corresponden a la versión 1.1.8 de la app:

- `docs/politica-privacidad.html` → `index.html`
- `docs/terminos-uso.html` → `terminos.html`

- Sitio: https://anibalortizdev.github.io/guia-heartopia-contenido/
- Repo: https://github.com/anibalortizdev/guia-heartopia-contenido (rama `main`, archivos en la raíz)
- El APK no necesita cambios: los botones «Política de privacidad» y «Términos y condiciones» abren esas URL.

## Cambios de esta versión

1. Política y términos alineados con la app v1.1.8 (23/11/2026).
2. Política: se añade la comprobación de actualizaciones del contenido y se aclara que con un acceso activo no se solicitan anuncios.
3. Términos: se añaden las secciones «Códigos de acceso» y «Actualizaciones», y se aclara que el respaldo depende del usuario.
4. Ninguno de los dos textos incluye referencias a adquisiciones, importes ni servicios de pago; solo describen el canje de un código de acceso y su verificación.

## Publicación

Copiar los dos archivos a la raíz del repo de contenido, hacer commit y push a `main`. No modificar `content/`, el manifiesto ni `contentVersion`.

```bash
cp docs/politica-privacidad.html /ruta/al/repo/guia-heartopia-contenido/index.html
cp docs/terminos-uso.html /ruta/al/repo/guia-heartopia-contenido/terminos.html
git -C /ruta/al/repo/guia-heartopia-contenido add index.html terminos.html
git -C /ruta/al/repo/guia-heartopia-contenido commit -m "Actualizar política y términos a v1.1.8"
git -C /ruta/al/repo/guia-heartopia-contenido push origin main
```

Después del push, verificar con un parámetro `?cb=` único que la web muestre la v1.1.8 en ambos textos. No guardes tokens dentro de este repositorio.
