# -------------------- BUILD STAGE --------------------
FROM maven:4.0.0-rc-5-eclipse-temurin-25-noble AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the app
COPY src ./src
RUN mvn clean package -B -DskipTests

# -------------------- RUNTIME STAGE --------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy jar file from builder
COPY --from=builder /app/target/*.jar app.jar

ENV SERVER_PORT=8080

# Expose the application port (default to 8080)
EXPOSE ${SERVER_PORT}

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 --start-period=40s \
    CMD "wget --no-verbose -q --spider http://localhost:${SERVER_PORT}/actuator/health"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
