# Contrato HTTP

Todas las respuestas son JSON con `Cache-Control: no-store` en el proxy. Base: `/api`.

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/health` | 200: estado y modo (`hosted` o `java`) |
| GET | `/bookings?date=AAAA-MM-DD` | 200: agenda de ambas salas, incluye canceladas |
| POST | `/bookings` | 201: reserva creada; 400: validación; 409: conflicto |
| POST | `/bookings/{uuid}/cancel` | 200: cancelada, también si ya lo estaba; 404: no existe |

Cuerpo de creación:

```json
{"room":"norte","date":"2026-09-10","start":600,"duration":60,"title":"Planeación del equipo","name":"Equipo creativo"}
```

Sustituye la fecha del ejemplo por una fecha futura. `start` representa minutos desde medianoche (600 = 10:00). `room`: norte o sur. `duration`: 30, 60 o 90. El motivo admite 3–80 caracteres y el nombre 2–60 después de quitar espacios exteriores. La respuesta agrega `id` y `status: CONFIRMED`.

Error: `{"message":"Ese horario acaba de ocuparse. Elige otro."}`. Ante un 409, vuelve a consultar la agenda. Los clientes no deben reintentar automáticamente una creación después de un fallo de red: no hay clave de idempotencia para crear; consulta primero la agenda. La cancelación sí es idempotente.

Para llamar al proxy desde un cliente HTTP en escrituras se requiere el encabezado `Origin` igual al origen del frontend. Java directo está pensado para la red interna de Docker.
