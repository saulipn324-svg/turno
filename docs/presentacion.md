# Cómo presentar Turno

“Construí un sistema de reservas para resolver conflictos de disponibilidad. El usuario selecciona una sala, fecha y duración. La API Java valida la solicitud y ocupa bloques de media hora dentro de una transacción. Una restricción única impide que dos peticiones concurrentes reserven el mismo intervalo.”

## Demostración de tres minutos

1. Crea una reserva de 90 minutos y muestra cómo desaparecen los horarios que se superponen.
2. Reserva en otra sala al mismo tiempo: las salas son independientes.
3. Cancela y vuelve a reservar el intervalo para mostrar la liberación.
4. Explica el test concurrente: dos hilos, un éxito, un conflicto y ninguna inserción parcial.

Tecnologías: Java 21, Spring Boot, JDBC, Flyway, PostgreSQL, JUnit, Testcontainers, React, TypeScript, Tailwind, Docker y una demo Worker/D1. Presentar únicamente las verificaciones efectivamente ejecutadas; no afirmar una arquitectura de microservicios ni autenticación que el proyecto no implementa.

La siguiente evolución razonable sería añadir identidad, propiedad de reservas e idempotencia de creación, antes de incorporar notificaciones o más sedes.
