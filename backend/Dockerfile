FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B --no-transfer-progress verify
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN groupadd --system turno && useradd --system --gid turno turno
WORKDIR /app
COPY --from=build --chown=turno:turno /build/target/turno-1.0.0.jar app.jar
USER turno
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
