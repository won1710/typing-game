# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent POM and module POMs for dependency resolution
COPY pom.xml .
COPY portal/pom.xml portal/
COPY slack-scheduler/pom.xml slack-scheduler/
COPY typing-game/pom.xml typing-game/

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -pl typing-game -am -q

# Copy source and build
COPY typing-game/src typing-game/src
RUN mvn package -pl typing-game -am -DskipTests -q

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/typing-game/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
