# Single-stage build with Maven and JRE
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Copy application files
COPY pom.xml .
COPY src ./src
COPY maxvision-edge-protocol-gateway-service-sdk.jar ./

# Build the application
RUN mvn clean package -DskipTests

# Expose the device communication port
EXPOSE 6060

# Run the application
ENTRYPOINT ["java", "-jar", "target/fleet-gateway-1.0.0-SNAPSHOT.jar"]
