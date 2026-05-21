FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/network-mapper-backend-1.0-SNAPSHOT.jar app.jar
RUN addgroup -S runnergroup && adduser -S runneruser -G runnergroup
USER runneruser
ENTRYPOINT ["java", "-jar", "app.jar"]