FROM maven:3.8.4-openjdk-11

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /app/src/

CMD mvn spring-boot:run -Dspring-boot.run.profiles=dockerdev
