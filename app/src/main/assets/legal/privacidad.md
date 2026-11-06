Política de privacidad — Guía Heartopia
Versión 0.1.0

Este texto describe lo que hace ESTA versión de la aplicación. Antes de publicarla en Google Play hay que completar el nombre de quien responde, un correo de contacto y el dominio del sitio. Esos datos todavía no existen: no los inventes ni los des por publicados.

Qué es
Guía Heartopia es una guía independiente para anotar tu propia partida y preparar referencias de pixel art. No está afiliada a los creadores de Heartopia ni a sus editores.

Qué se guarda en el dispositivo
El nombre que escribes, el país y la zona horaria que eliges, la rutina, las fichas, los coleccionables, los hobbies y la última cuadrícula de pixel art se guardan solo en el teléfono, con DataStore y archivos de la app. La app no tiene cuenta, no tiene servidor propio y no envía esa información a un servicio de Guía Heartopia.

Qué no pedimos
No pedimos ubicación, contactos, fotos de forma permanente, micrófono ni notificaciones. La imagen para pixel art se abre con el selector del sistema y se procesa en el dispositivo. Exportar un PNG usa el almacenamiento que ofrece el sistema (MediaStore en Android 10 o superior) sin un permiso amplio de archivos.

Anuncios, identificador de publicidad y consentimiento
Esta versión puede mostrar un banner de Google AdMob.

- La compilación de depuración usa únicamente los ID oficiales de prueba de Google. No son anuncios de un editor real.
- La compilación de publicación solo solicita anuncios si quien publica configura un App ID y un bloque fuera del repositorio. Si no están, no se inicializa el SDK y no se pide publicidad.
- Antes de MobileAds.initialize, la app consulta el User Messaging Platform (UMP) de Google. No marca el consentimiento como otorgado. Si rechazas el consentimiento y UMP indica que no se pueden pedir anuncios, el banner no se muestra.
- Si la consulta de consentimiento falla, y los anuncios están configurados, la solicitud se hace como no personalizada (señal npa).
- El SDK de anuncios declara el permiso AD_ID (identificador de publicidad). Cuando un anuncio se solicita, Google puede tratar datos del dispositivo, incluido ese identificador, según su propia política. Guía Heartopia no añade otro SDK de analítica ni de atribución.
- Para usuarios del EEE y otros territorios regulados, el mensaje de consentimiento tiene que estar creado y publicado en la consola de AdMob (Privacidad y mensajes). Sin ese mensaje publicado, el formulario de producción no puede mostrarse. Esta app no sustituye ese paso.

Encargado y contacto
Responsable: pendiente de definir antes de publicar.
Correo: pendiente de definir antes de publicar.
Dominio: pendiente de definir antes de publicar.

Cambios
Si esta política cambia, la versión de la app y este texto se actualizarán juntos.
