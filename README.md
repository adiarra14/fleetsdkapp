# Fleet Gateway SDK App

This application integrates the Maxvision Protocol Gateway SDK to collect device data and provide REST APIs for fleet management.

## Requirements
- Java 17
- Maven
- PostgreSQL
- The SDK JAR: `maxvision-edge-protocol-gateway-service-sdk.jar` (place in the project root)

## Setup
1. Install PostgreSQL and create a database `fleetdb` and user `fleetuser` with password `fleetpass`.
2. Place the SDK JAR in the project root.
3. Run `mvn clean install` to build the project.
4. Start the app with `mvn spring-boot:run`.

## Configuration
- Edit `src/main/resources/application.yml` for database and gateway settings.
- Logging is configured in `src/main/resources/logback-config.xml`.

## Features
- Device connection and data collection via the Maxvision SDK
- Data persistence to PostgreSQL
- REST API for querying device and fleet data

## Extending
- Implement business logic in the service layer.
- Add new REST endpoints as needed.

---
