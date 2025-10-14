# -------------------- BUILD STAGE --------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the app
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------- RUNTIME STAGE --------------------
FROM openjdk:21-jdk

WORKDIR /app

# Copy jar file from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port (default to 8080)
ARG SERVER_PORT=8080
ENV SERVER_PORT=${SERVER_PORT}
EXPOSE ${SERVER_PORT}

# Environment Variables
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT [ "java", "-jar", "app.jar" ]
