# Respuestas sugeridas para Play Console

App: WikiFan para Heartopia (`com.guiaheartopia.app`).  
Política de privacidad: https://anibalortizdev.github.io/guia-heartopia-contenido/

## Declaración de anuncios

¿Tu app contiene anuncios? **Sí.**

Hay banner de AdMob y videos recompensados opcionales. El banner va etiquetado como «Publicidad» y no se mezcla con los botones de la guía.

## ID de publicidad

¿Tu app usa un ID de publicidad? **Sí.**

- Permiso declarado: `com.google.android.gms.permission.AD_ID`.
- Lo usa el SDK de Google AdMob para pedir y medir anuncios, después del formulario UMP.
- No hay otro SDK de analítica ni de atribución.
- El usuario puede limitar o borrar el identificador de publicidad en los ajustes de Android, y cambiar el consentimiento de anuncios en Ajustes de la app cuando Google muestra esa opción.

No marques la app como dirigida a niños: con anuncios e identificador de publicidad, el programa Familias no corresponde (ver público objetivo).

## Público objetivo

- Edad: **13 años o más**.
- ¿Está diseñada para atraer a niños? **No.**

Por qué: es una guía de consulta (listas, horarios, clima, aficiones y anotaciones). El tono es de cuaderno de naturalista, sin personajes infantiles, sin minijuegos y sin premios dirigidos a menores. Incluye anuncios, entre ellos video recompensado, y trata el identificador de publicidad. Esa combinación no debe entrar al programa «App para familias» ni declararse como contenido para niños: Play exige en ese programa anuncios certificados y prohíbe varios formatos que esta app usa. Declararla 13+ y «no diseñada para atraer a niños» deja los anuncios en el flujo normal de AdMob, con UMP, fuera de Familias.

No selecciones grupos de edad por debajo de 13 años.

## Clasificación de contenido (IARC)

Categoría de la ficha: herramientas, o libros y referencias, si Play lo ofrece. No es un juego.

Respuestas orientativas del cuestionario:

| Tema | Respuesta |
| --- | --- |
| Violencia | No |
| Miedo, horror | No |
| Sexualidad | No |
| Lenguaje soez | No |
| Drogas, alcohol, tabaco | No |
| Apuestas, loot boxes | No |
| Contenido generado por el usuario visible para otros | No. Las notas se quedan en el teléfono. No hay chat, perfil público ni servidor de la guía. |
| Usuarios pueden interactuar entre sí | No |
| Comparte la ubicación del usuario con otros | No |
| Compras digitales | No. No hay Play Billing. |
| Anuncios | Sí, de Google |
| Restricción de edad en la propia app | No hay muro de edad. El público declarado es 13+. |

Resultado esperado: PEGI 3 / Everyone o similar por el contenido de la guía, **sin** marcarla como app infantil en el público objetivo. La clasificación IARC y el público 13+ son preguntas distintas: la guía no tiene contenido violento, y aun así no está hecha para atraer a niños por los anuncios.

## Seguridad de los datos

¿La app recoge o comparte datos de usuario? **Sí**, por el SDK de anuncios de Google. Los apuntes de la guía no salen del teléfono.

### Datos que no salen del teléfono (no los declares como recogidos por el desarrollador)

- Nombre escrito en el perfil.
- País, zona horaria, recordatorios.
- Fichas, colección, aficiones y notas.

No hay cuenta. No se pueden usar para seguimiento entre apps. Se borran al desinstalar o al borrar el almacenamiento de la app.

### Datos que declara el SDK de AdMob (terceros: Google)

Marca que **los recogen terceros**, no que esta app tenga un backend propio. No se venden.

| Tipo en el formulario | ¿Se recoge? | ¿Se comparte? | Finalidad | Obligatorio |
| --- | --- | --- | --- | --- |
| ID de publicidad (identificadores del dispositivo) | Sí | Sí, con Google | Publicidad o marketing | Sí, para mostrar anuncios |
| ID del dispositivo u otros identificadores técnicos que envíe el SDK | Sí | Sí, con Google | Publicidad, análisis de anuncios, prevención de fraude del SDK | Sí |
| Interacciones con el anuncio (actividad en la app) | Sí | Sí, con Google | Publicidad y análisis del anuncio | Sí |
| Ubicación aproximada (la que Google infiere de la IP) | Sí | Sí, con Google | Publicidad | Sí, la envía el SDK al pedir el anuncio |
| Información del dispositivo (modelo, idioma, versión) | Sí | Sí, con Google | Publicidad y diagnóstico del SDK | Sí |

No declares: ubicación precisa, contactos, fotos, micrófono, archivos, salud, mensajes, historial web propio, ni datos financieros.

### Otras casillas

- ¿Se venden los datos? **No.**
- ¿El tráfico va cifrado? **Sí** (HTTPS hacia Google y hacia el paquete público de contenido).
- ¿Los usuarios pueden pedir que se borren? **Sí.** Los datos locales se borran al desinstalar. Los datos de anuncios los gestiona Google (https://policies.google.com/privacy); la app no guarda una copia en un servidor propio.
- ¿Hay cuenta? **No.** No hace falta un inicio de sesión para borrar datos.
- Consentimiento en el EEE: formulario UMP antes de pedir anuncios. Si no se puede pedir publicidad, el banner no aparece.

## Play App Signing

Al crear la app, activa **Play App Signing** y deja que Google genere y guarde la clave de firma de la app. El archivo `.aab` se sube firmado solo con la clave de subida. Detalle en `firma.md`.
