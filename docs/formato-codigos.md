# Formato `codes/codigos.json`

Lista de códigos canjeables. Campos opcionales: no inventes claves en la app si no vienen en el archivo.

## Esquema por entrada

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `code` | string | sí | Texto del código tal como se escribe en el juego. |
| `recompensa` | string | no | Descripción breve del premio (solo si viene en JSON). |
| `fechaInicio` | string | no | Fecha ISO `YYYY-MM-DD` (o prefijo de fecha/hora). El código no se muestra como vigente antes de este día. |
| `fechaVencimiento` | string | no | Fecha ISO; después de este día pasa a la sección **Vencidos**. |
| `fuente` | string | no | Atribución o enlace textual (solo si viene). |

## Ejemplo

```json
[
  {
    "code": "CORAZON2026",
    "recompensa": "Semillas y monedas",
    "fechaInicio": "2026-11-01",
    "fechaVencimiento": "2026-12-01",
    "fuente": "Anunciado en redes oficiales"
  }
]
```

## Persistencia en el teléfono

- `seenCodes` (perfil): códigos que el usuario ya abrió en la pantalla Códigos.
- `redeemedCodeIds` (snapshot): códigos marcados como **Ya canjeado** por el usuario (no implica validación con el servidor).