# ---------- Build stage ----------
FROM eclipse-temurin:17-jdk-alpine AS builder
RUN apk add --no-cache maven git
WORKDIR /workspace

# copy pom.xml first
COPY pom.xml .

# copy and install SDK JAR to local Maven repo
COPY lib/maxvision-edge-protocol-gateway-service-sdk.jar /tmp/sdk.jar
RUN mvn -q \
    install:install-file \
    -Dfile=/tmp/sdk.jar \
    -DgroupId=com.maxvision \
    -DartifactId=maxvision-edge-protocol-lock-sdk \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=jar \
    -DgeneratePom=true

# prefetch dependencies
RUN mvn -B dependency:go-offline

# copy source code and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /workspace/target/fleet-gateway-1.0.0-SNAPSHOT.jar ./app.jar

# Expose the device communication port
EXPOSE 6060

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
