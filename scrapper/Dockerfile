FROM maven:3.9.12-eclipse-temurin-25 AS builder
WORKDIR /app

COPY . .
RUN mvn -pl scrapper -am package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/scrapper/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
