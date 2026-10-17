# Etapa 1: compilar el WAR con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: Payara 7 con el WAR del frontend
FROM payara/server-full:7.2026.8-jdk25
COPY --from=build /src/target/frontend-correo-1.0-SNAPSHOT.war /opt/payara/deployments/
EXPOSE 8080