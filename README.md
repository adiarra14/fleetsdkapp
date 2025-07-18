# Fleet Gateway SDK App

This application integrates the Maxvision Protocol Gateway SDK to collect device data and provide REST APIs for fleet management.

## Requirements
- Java 17
- Maven
- PostgreSQL
- Docker and Docker Compose (for containerized deployment)
- Portainer (for production deployment)
- The SDK JAR: `maxvision-edge-protocol-gateway-service-sdk.jar` (in the backend-service/lib and tcp-server-service/lib directories)

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

## Docker Deployment

### Local Deployment with Docker Compose

1. Ensure Docker and Docker Compose are installed on your system
2. Navigate to the project root directory
3. Run the stack using Docker Compose:

```bash
docker-compose up -d
```

4. Verify all services are running:

```bash
docker-compose ps
```

### Port Mappings
- 7000:8080 - SDK Backend Service (Spring Boot)
- 7001:8910 - SDK Netty Server (Maxvision Protocol)
- 7002:5432 - PostgreSQL with PostGIS
- 7003:6060 - TCP Server for balise data
- 7004:8080 - API Service
- 7005:80 - Web UI

## Portainer Deployment

### Requirements
- Portainer instance with access to deploy stacks
- Git repository with this codebase accessible by Portainer

### Deployment Steps

1. Log in to your Portainer instance
2. Navigate to Stacks > Add stack
3. Choose "Repository" as the build method
4. Enter the Git repository URL: `https://github.com/adiarra14/fleetsdkapp.git`
5. Enter the reference name (branch): `master`
6. Enter the compose file path: `docker-compose.yml`
7. Name your stack (e.g., "fleet-sdk")
8. Click "Deploy the stack"

### Troubleshooting Portainer Deployment

- Ensure all start.sh scripts have executable permissions
- Verify that lib directories contain the required SDK JAR files
- Check Portainer logs for any volume mount or permission issues
- For volume mount issues, consider using Docker images instead of volume mounts

---
