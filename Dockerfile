# Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Maven and Git
RUN apk add --no-cache maven git

WORKDIR /app

# Copy the SDK JAR and POM
COPY lib/maxvision-edge-protocol-gateway-service-sdk.jar /app/lib/
COPY pom.xml /app/

# Install the SDK JAR into the local Maven repository
RUN echo "Installing SDK JAR into local Maven repository..." && \
    mvn install:install-file -Dfile=/app/lib/maxvision-edge-protocol-gateway-service-sdk.jar \
    -DgroupId=com.maxvision -DartifactId=maxvision-edge-protocol-lock-sdk \
    -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -X

# Download dependencies
RUN echo "Downloading Maven dependencies..." && \
    mvn -B dependency:go-offline -X

# Copy the rest of the application code
COPY src /app/src/

# Build the application with verbose output
RUN echo "Building application..." && \
    mvn -B clean package -DskipTests -X

# Production Stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 6060

ENTRYPOINT ["java", "-jar", "app.jar"]
