# Build stage: compile the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
COPY maxvision-edge-protocol-gateway-service-sdk.jar ./

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage: use a minimal JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/fleet-gateway-1.0.0-SNAPSHOT.jar app.jar
COPY --from=build /app/maxvision-edge-protocol-gateway-service-sdk.jar ./

# Expose the device communication port
EXPOSE 6060

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
