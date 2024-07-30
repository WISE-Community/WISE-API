# Generate wise-api-server Docker image

# Stage 1: Build application
FROM maven:3.9.6-eclipse-temurin-17 as build
WORKDIR /app
COPY . .
RUN mvn package

# Stage 2: Copy war
FROM eclipse-temurin:17
COPY --from=build /app/target/wise.war wise.war
ENTRYPOINT ["java","-jar","/wise.war", "--spring.config.location=/application.properties"]
