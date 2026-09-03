# Arquitectura

## Flujo

Navegador → `/api/bookings` (Worker). En Sites, la ruta consulta D1; con `TURNO_API_BASE`, delega por HTTP a Spring Boot → PostgreSQL. El navegador siempre usa rutas del mismo origen. El proxy permite únicamente las rutas del contrato y restringe escrituras al origen esperado. Los valores no se interpolan en SQL; todas las consultas están parametrizadas.

## Prevención de conflictos

`bookings` conserva motivo, nombre, horario y estado. `slots` contiene una fila por cada bloque ocupado. La combinación `(room, date, start)` es única. Una reserva de 90 minutos ocupa tres filas.

Crear una reserva inserta el registro y sus bloques en una transacción: `@Transactional` en Java y `DB.batch` atómico en D1. Dos solicitudes concurrentes pueden ver disponibilidad, pero únicamente una consigue la restricción única. La otra obtiene 409; sus inserciones parciales se revierten. No se depende de una comprobación previa en memoria.

Cancelar cambia el estado y elimina los bloques en una transacción. Java bloquea la reserva con `SELECT ... FOR UPDATE` para serializar cancelaciones. Repetir la cancelación devuelve éxito. La agenda conserva el historial cancelado.

## Decisiones

- JDBC hace explícito el SQL y el límite transaccional; no se introduce ORM donde no aporta valor.
- Los intervalos son semiabiertos: 10:00–11:00 y 11:00–11:30 son compatibles.
- La agenda usa minutos desde medianoche; no admite intervalos que crucen el cierre.
- Java usa `America/Mexico_City`; la demo usa UTC−06:00 para esta ubicación sin cambio estacional. Revisar esta equivalencia si cambian las reglas horarias o se añaden sedes.
- Los entornos implementan el mismo contrato y comparten una prueba HTTP reproducible; no comparten código de reglas ni datos. Los cambios de negocio requieren mantener ambas implementaciones.
- La agenda recupera como máximo 500 registros por fecha. Para un servicio de uso prolongado añadir paginación del historial y política de retención.

## Alcance de seguridad

Demo compartida sin autenticación de aplicación. El acceso alojado depende de los permisos de Sites y se publica inicialmente privado. No hay pagos, notificaciones ni datos personales requeridos. El entorno Docker no publica PostgreSQL ni Java al host y expone solo el frontend en loopback. El control de origen del proxy reduce escrituras desde otros sitios, pero no reemplaza autenticación.
