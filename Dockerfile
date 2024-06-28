FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /app/src/

CMD mvn spring-boot:run -Dspring-boot.run.profiles=dockerdev
