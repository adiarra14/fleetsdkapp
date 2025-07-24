# -------- build stage --------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy the full Maven project
COPY . /build

# Build only the backend-service module (contains SdkNettyApplication)
RUN mvn -q -pl backend-service -am package -DskipTests

# -------- runtime stage --------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=builder /build/backend-service/target/*.jar app.jar

EXPOSE 6060

ENTRYPOINT ["java","-jar","/app/app.jar"]
