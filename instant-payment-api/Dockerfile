# --- Stage 1: Build the JAR with Maven ---
FROM maven:3.9.5-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy everything from local dir into the container, except what's ignored by .dockerignore
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# --- Stage 2: Run the app ---
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy only the final JAR from the builder stage
COPY --from=builder /app/target/instant-payment-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
