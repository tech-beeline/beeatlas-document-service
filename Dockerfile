ARG RUN_IMAGE=eclipse-temurin:17-jdk-jammy
FROM ${RUN_IMAGE} AS build
WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

ARG RUN_IMAGE=eclipse-temurin:17-jdk-jammy
FROM ${RUN_IMAGE} as app

RUN useradd -m -d /app -s /bin/bash -u 1000 appuser && \
    chown -R appuser:appuser /app
WORKDIR /app

COPY --from=build --chown=appuser:appuser /app/target/document-service-*.jar app.jar
USER appuser
EXPOSE 8080 8090 10260
ENTRYPOINT ["java", "-jar", "app.jar"]
