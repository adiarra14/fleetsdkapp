# Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /app

# Copy pom and jar early for dependency resolution
COPY pom.xml ./
COPY lib/maxvision-edge-protocol-gateway-service-sdk.jar /tmp/

# Manually install the local JAR into the Maven repo
RUN mvn install:install-file \
  -Dfile=/tmp/maxvision-edge-protocol-gateway-service-sdk.jar \
  -DgroupId=com.maxvision \
  -DartifactId=maxvision-edge-protocol-lock-sdk \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackaging=jar \
  -DgeneratePom=true

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build
RUN mvn -B clean package -DskipTests

# Production Stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 6060

ENTRYPOINT ["java", "-jar", "app.jar"]
