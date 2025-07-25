FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY lib/ lib/
COPY src/ src/
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/lock-sdk-demo-1.0.0-SNAPSHOT.jar app.jar
COPY --from=build /app/lib/ lib/
EXPOSE 8080
EXPOSE 8910
ENTRYPOINT ["java", "-jar", "app.jar"] 