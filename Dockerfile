FROM node:22-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /workspace/target/opsdesk-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
