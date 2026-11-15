# Eventos remotos

Archivo opcional del paquete de contenido: `content/calendar/eventos.json`.

Si el manifiesto remoto lo incluye, entra con el mismo hash y el mismo reemplazo atómico que el resto. Si no está, o el JSON no se puede leer, la app no programa avisos de eventos y sigue funcionando. Este archivo no va en el contenido que trae el APK.

Las horas son hora del servidor del juego. `zonaFuente` no cambia con el horario de verano (por ejemplo `Etc/GMT+5`, que es UTC−5 fijo). La app las pasa a la zona IANA del usuario con `java.time`, en cada fecha.

```json
{
  "zonaFuente": "Etc/GMT+5",
  "servidor": "America",
  "eventos": [
    {
      "id": "pesca-altamar",
      "tipo": "aficion",
      "categoria": "pesca",
      "temporadaId": "opcional",
      "nombre": "Pesca de altamar",
      "repeticion": "diaria",
      "dias": ["lun"],
      "horas": ["12:00"],
      "inicio": "2026-04-04T00:00",
      "fin": "2026-04-04T12:00",
      "duracionMin": 10,
      "avisoMinAntes": 15
    },
    {
      "id": "halloween-noche-grimkin",
      "tipo": "temporada",
      "categoria": "temporada",
      "nombre": "Noche grimkin",
      "inicio": "2026-10-30T06:00",
      "fin": "2026-11-16T05:59"
    }
  ]
}
```

`categoria` de una afición es una de: `pesca`, `aves`, `insectos`.

`repeticion` es `diaria` o `semanal`. En `semanal`, `dias` usa `lun`, `mar`, `mie`, `jue`, `vie`, `sab`, `dom`, según el día en la zona del servidor.

Opcionales: `temporadaId`, `dias`, `inicio`, `fin`, `duracionMin`, `avisoMinAntes` (si falta, 15). `inicio` y `fin` son `YYYY-MM-DDTHH:mm` en hora de servidor. Si existen, la hora de una afición solo avisa dentro de ese intervalo.

- `tipo` `aficion`: un aviso `avisoMinAntes` minutos antes de cada hora. El texto es «En 15 min: <nombre>» (con los minutos reales). En Ajustes hay un interruptor por categoría.
- `tipo` `temporada`: un aviso al llegar `inicio` y otro «Queda 1 día» 24 h antes de `fin`. El interruptor es «Eventos de temporada». En Calendario, al final, cada evento del archivo muestra su nombre y, si es una temporada en curso, el tiempo que falta hasta `fin`.

Los avisos de las próximas 48 h se programan con WorkManager, sin alarma exacta: al sincronizar, al abrir la app, después de cada aviso y al reiniciar el teléfono.
