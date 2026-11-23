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
| Compras digitales | No se realizan dentro de la app. Los códigos de acceso se entregan fuera de ella. |
| Anuncios | Sí, de Google |
| Restricción de edad en la propia app | No hay muro de edad. El público declarado es 13+. |

Resultado esperado: PEGI 3 / Everyone o similar por el contenido de la guía, **sin** marcarla como app infantil en el público objetivo. La clasificación IARC y el público 13+ son preguntas distintas: la guía no tiene contenido violento, y aun así no está hecha para atraer a niños por los anuncios.

## Seguridad de los datos

¿La app recoge o comparte datos de usuario? **Sí**: por el SDK de anuncios de Google, de forma voluntaria cuando el usuario escribe un comentario o pregunta, y cuando el usuario introduce un código de acceso, que se envía a un servicio externo de verificación. Los apuntes de la guía no salen del teléfono.

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

### Datos que envía el usuario al escribir al desarrollador (tercero: Google Forms)

El menú «Comentarios y preguntas» envía por HTTPS lo que el usuario escribe a un formulario de Google Forms alojado en la cuenta del desarrollador. No hay cuenta ni servidor propio; el correo de respuesta es opcional.

| Tipo en el formulario | ¿Se recoge? | ¿Se comparte? | Finalidad | Obligatorio |
| --- | --- | --- | --- | --- |
| Dirección de correo electrónico (solo si el usuario la escribe) | Sí | Sí, con Google Forms (Google) para poder responderle | Comunicación / función de la app | No, opcional |
| Contenido del mensaje (comentario, pregunta o aclaración que escribe el usuario) | Sí | Sí, con Google Forms (Google) | Atención al usuario / función de la app | No, el usuario decide escribir |

### Datos que envía la app al verificar códigos de acceso (tercero: servicio de verificación)

Cuando el usuario introduce un código, la app envía por HTTPS el código, el fabricante y modelo del dispositivo, un identificador de la activación y el identificador de la organización a un servicio externo de verificación de códigos. Ese servicio ve también la dirección IP de la conexión. La comprobación periódica (cada 72 horas) y la desvinculación usan el código y el identificador de activación. No hay cuenta de usuario y el desarrollador no recibe estos datos en un servidor propio.

| Tipo en el formulario | ¿Se recoge? | ¿Se comparte? | Finalidad | Obligatorio |
| --- | --- | --- | --- | --- |
| Identificadores / dato introducido por el usuario (código de acceso) | Sí | Sí, con el servicio de verificación | Función de la app: verificar el código | Sí, para activar el acceso |
| Información del dispositivo (fabricante y modelo) | Sí | Sí, con el servicio de verificación | Función de la app: asociar el código al dispositivo | Sí, al introducir el código |
| Identificadores (identificador de activación) | Sí | Sí, con el servicio de verificación | Función de la app: comprobación periódica y desvinculación | Sí, mientras el acceso esté activo |
| Dirección IP (visible para el proveedor por HTTPS) | Sí | Sí, con el servicio de verificación | Función de la app: establecer la conexión | Sí, al conectar |

No declares: ubicación precisa, contactos, fotos, micrófono, archivos, salud, mensajería entre usuarios, historial web propio, ni datos financieros. Los apuntes y anotaciones de la guía nunca salen del dispositivo.

### Otras casillas

- ¿Se venden los datos? **No.**
- ¿El tráfico va cifrado? **Sí** (HTTPS hacia AdMob, Google Forms, el paquete público de contenido y el servicio de verificación de códigos).
- ¿Los usuarios pueden pedir que se borren? **Sí.** Los datos locales se borran al desinstalar. Los datos de anuncios los gestiona Google (https://policies.google.com/privacy); la app no guarda una copia en un servidor propio. El registro asociado al código se elimina al desvincular el dispositivo; la copia local se borra al desvincular o al borrar los datos de la app. Los mensajes enviados se pueden borrar pidéndolo por correo a anibalortiz.dev@gmail.com.
- ¿Hay cuenta? **No.** No hace falta un inicio de sesión para borrar datos.
- Consentimiento en el EEE: formulario UMP antes de pedir anuncios. Si no se puede pedir publicidad, el banner no aparece.

## Play App Signing

Al crear la app, activa **Play App Signing** y deja que Google genere y guarde la clave de firma de la app. El archivo `.aab` se sube firmado solo con la clave de subida. Detalle en `firma.md`.
