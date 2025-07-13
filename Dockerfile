# Use a minimal Java 17 runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar and the SDK jar
COPY target/fleet-gateway-1.0.0-SNAPSHOT.jar app.jar
COPY maxvision-edge-protocol-gateway-service-sdk.jar ./

# Expose the device communication port
EXPOSE 6060

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
