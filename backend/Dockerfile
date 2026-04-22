# backend/Dockerfile

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Render provides PORT at runtime; this also works locally if PORT is not set
ENV PORT=10000

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]