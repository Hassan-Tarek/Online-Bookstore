# -------------------- BUILD STAGE --------------------
FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline -B

# Copy src and build the app
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# -------------------- RUNTIME STAGE --------------------
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy jar file from builder
COPY --from=builder /app/target/*.jar app.jar

# Default build-time args
ARG SERVER_PORT=8080
ARG SPRING_PROFILES_ACTIVE=dev

# Runtime environment
ENV SERVER_PORT=$SERVER_PORT
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE

# Expose the application port (default to 8080)
EXPOSE ${SERVER_PORT}

# Run the application
ENTRYPOINT [ "java", "-jar", "app.jar" ]
