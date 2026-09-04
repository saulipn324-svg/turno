# Turno · Sistema de reservas

Segundo proyecto del portafolio de **Saul Ramos Sanchez**. Una aplicación para reservar salas en bloques de 30 minutos sin permitir solapamientos.

## Funcionalidad

- Dos salas independientes, fechas dentro de 90 días y jornadas de 09:00 a 18:00 en Ciudad de México.
- Reservas de 30, 60 o 90 minutos; consulta de agenda y cancelación idempotente.
- Validación en cliente y servidor, errores 400/404/409, transacciones y restricción única por sala/fecha/bloque.
- Interfaz responsive con React, TypeScript, Tailwind y componentes accesibles.

## Dos entornos del mismo producto

La demo alojada usa un Worker y D1 (SQLite) con almacenamiento real. La versión local usa la misma interfaz, una API Java 21 / Spring Boot 3.5, JDBC, Flyway y PostgreSQL 17. No se afirma que Java se ejecute dentro de Sites. Cada entorno conserva su propia base de datos; no hay sincronización entre ellos.

Es una agenda compartida de demostración: no tiene cuentas ni roles. Cualquier participante con acceso al sitio puede ver o cancelar reservas. Utiliza datos ficticios. El despliegue local se limita a 127.0.0.1; añadir autenticación y autorización es necesario antes de abrir la API a una organización.

## Inicio con Docker

Requisitos: Docker Desktop abierto y Node 24 para el script de comprobación.

Desde la carpeta `turno`, ejecutar `powershell -ExecutionPolicy Bypass -File .\scripts\Iniciar-Turno.ps1`. La excepción de ejecución aplica únicamente a ese proceso. El script genera `.env` si no existe, inicia los contenedores y comprueba las reservas. Abre http://localhost:3004. No afecta al puerto 3002 de Issueflow.

Inicio manual: copia `.env.example` a `.env`, sustituye la contraseña y ejecuta `docker compose up --build -d --wait`. Detener con `docker compose stop`; iniciar de nuevo con `docker compose up -d --wait`. Los datos permanecen en el volumen `turno_postgres-data`.

## Desarrollo

Backend: JDK 21, Maven 3.9; `cd backend` y `mvn spring-boot:run`. Usa H2 en archivo por defecto. `mvn test` ejecuta siete pruebas de integración; `mvn -Ppostgres verify` añade las mismas siete contra PostgreSQL real mediante Testcontainers (requiere Docker).

Frontend: `cd frontend`, `npm ci`, `npm run db:migrate`, `npm run dev -- --port 3003`. Usa D1 local. Para conectarlo a Java, crea `.dev.vars` con `TURNO_API_BASE=http://127.0.0.1:8080` y reinicia el servidor. `npm run typecheck`, `npm test` y `npm run build` verifican la entrega.

## Documentación

- [Arquitectura y decisiones](docs/arquitectura.md)
- [Contrato HTTP](docs/api.md)
- [Manual de uso](docs/manual.md)
- [Pruebas y limitaciones](docs/pruebas.md)
- [Guion para presentar el proyecto](docs/presentacion.md)

Las migraciones versionadas son la única fuente del esquema: Flyway para Java, Drizzle para D1. Los secretos y datos locales se excluyen del repositorio.

## Organización del código

Consulta [la arquitectura y sus decisiones](docs/ARQUITECTURA.md).
