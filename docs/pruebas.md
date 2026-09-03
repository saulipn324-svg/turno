# Pruebas y evidencia

Validación realizada el 2 de septiembre de 2026:

- Java: 7 pruebas JUnit con H2 aprobadas, incluyendo carrera entre dos hilos, rollback de inserción parcial, cancelación repetida, adyacencia y validación.
- Prueba HTTP `node scripts/check-api.mjs URL`: aprobada contra D1 local y contra la API Java ejecutada con H2 en archivo. Dos creaciones simultáneas producen exactamente 201 y 409.
- Frontend: cuatro pruebas de reglas y comprobación TypeScript incluidas en los comandos npm.
- PostgreSQL: suite `PostgresIT` preparada para ejecutarse con Docker mediante `mvn -Ppostgres verify`; pendiente de ejecución en el equipo del usuario.

El entorno restringido del asistente produce un error de acceso al cerrar archivos JAR en Maven. Las clases generadas se ejecutaron mediante JUnit Console, con siete éxitos; esto no se presenta como una ejecución Maven completa. La compilación y prueba convencional se incluye en Docker y CI.

Docker Compose está preparado, pero no se ha ejecutado desde el asistente porque Docker Desktop no es accesible a este proceso. El script de inicio comprueba el flujo HTTP y persistencia tras reinicio en el equipo del usuario.

La integración WebMCP prepara el formulario sin confirmar reservas. Requiere `document.modelContext`; no se dispone de un contexto de validación compatible, por lo que no se afirma haberla verificado. No se realizó prueba visual automatizada de navegador.

El script HTTP usa una fecha a 30 días, se detiene si ya contiene reservas activas y cancela únicamente los registros que crea. Conserva el historial cancelado.
