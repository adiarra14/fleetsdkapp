FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Create lib directory and copy SDK JAR
RUN mkdir -p lib
COPY lib/ lib/

# Verify SDK JAR exists
RUN ls -la lib/ && echo "SDK JAR files:"

# Copy source code
COPY src/ src/

# Build with verbose output
RUN mvn clean package -DskipTests -X

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/lock-sdk-demo-1.0.0-SNAPSHOT.jar app.jar
COPY --from=build /app/lib/ lib/
EXPOSE 8080
EXPOSE 8910
ENTRYPOINT ["java", "-jar", "app.jar"] 