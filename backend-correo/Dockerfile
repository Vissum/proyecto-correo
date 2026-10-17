# Etapa 1: compilar el WAR con Maven (no hace falta NetBeans)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa 2: Payara 7 con el WAR listo
FROM payara/server-full:7.2026.8-jdk25
USER root
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.8/postgresql-42.7.8.jar /opt/payara/appserver/glassfish/domains/domain1/lib/
COPY --from=build /src/target/backend-correo-1.0-SNAPSHOT.war /opt/payara/wars/
COPY docker/post-boot.asadmin /opt/payara/post-boot.asadmin
ENV POSTBOOT_COMMANDS=/opt/payara/post-boot.asadmin
ENV ADJUNTOS_DIR=/data/adjuntos
EXPOSE 8080