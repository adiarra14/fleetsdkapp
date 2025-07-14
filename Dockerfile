# Single-stage build with Maven and JRE
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Copy SDK JAR first and install it to local Maven repo
COPY maxvision-edge-protocol-gateway-service-sdk.jar ./
RUN mvn install:install-file \
    -Dfile=maxvision-edge-protocol-gateway-service-sdk.jar \
    -DgroupId=com.maxvision \
    -DartifactId=maxvision-edge-protocol-lock-sdk \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=jar

# Copy application files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Expose the device communication port
EXPOSE 6060

# Run the application
ENTRYPOINT ["java", "-jar", "target/fleet-gateway-1.0.0-SNAPSHOT.jar"]
