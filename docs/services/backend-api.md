# Backend API Service Documentation

## 🚀 Overview

The Backend API Service is the core business logic component of the Fleet Monitor system, providing RESTful API endpoints for balise management, data retrieval, and system monitoring.

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 2.7.0
- **Java Version**: 17 (Eclipse Temurin)
- **Database**: PostgreSQL with JDBC
- **Container**: Docker with Alpine Linux
- **Port**: 8080 (internal), 6062 (external)

### Service Structure
```
backend-service/
├── src/main/java/com/maxvision/backend/
│   ├── BackendApplication.java          # Main Spring Boot application
│   └── controller/
│       └── BaliseController.java        # REST API endpoints
├── src/main/resources/
│   └── application.yml                  # Configuration
├── lib/
│   └── maxvision-edge-protocol-gateway-service-sdk.jar
├── pom.xml                             # Maven dependencies
├── Dockerfile                          # Full Spring Boot build
└── Dockerfile.simple                   # Simplified build (current)
```

## 🔌 API Endpoints

### Base URL
```
http://localhost:6062/api
```

### Endpoints Overview

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | `/health` | Service health check | Plain text |
| GET | `/` | Service info | Plain text |
| GET | `/api/status` | System status and statistics | JSON |
| GET | `/api/balises` | List all balises | JSON |
| GET | `/api/balises/{id}` | Get specific balise | JSON |
| GET | `/api/balises/{id}/events` | Get balise events | JSON |
| POST | `/api/balises/{id}/command` | Send command to balise | JSON |

### Detailed Endpoint Documentation

#### 1. Health Check
```http
GET /health
```

**Response:**
```
Fleet SDK Backend Service is running - 2025-07-20T12:30:45
```

#### 2. System Status
```http
GET /api/status
```

**Response:**
```json
{
  "status": "success",
  "databaseConnected": true,
  "timestamp": "2025-07-20T12:30:45",
  "baliseStatusCounts": {
    "ACTIVE": 5,
    "INACTIVE": 2
  },
  "recentEvents": 23
}
```

#### 3. List All Balises
```http
GET /api/balises
```

**Response:**
```json
{
  "status": "success",
  "balises": [
    {
      "id": 1,
      "name": "Fleet-001",
      "imei": "123456789012345",
      "type": "GPS_TRACKER",
      "status": "ACTIVE",
      "batteryLevel": 85.5,
      "lastSeen": "2025-07-20T12:25:30",
      "createdAt": "2025-07-15T09:00:00",
      "longitude": 2.3522,
      "latitude": 48.8566,
      "eventCount": 15
    }
  ],
  "count": 1,
  "timestamp": "2025-07-20T12:30:45"
}
```

#### 4. Get Specific Balise
```http
GET /api/balises/1
```

**Response:**
```json
{
  "id": 1,
  "name": "Fleet-001",
  "imei": "123456789012345",
  "type": "GPS_TRACKER",
  "status": "ACTIVE",
  "batteryLevel": 85.5,
  "lastSeen": "2025-07-20T12:25:30",
  "createdAt": "2025-07-15T09:00:00",
  "longitude": 2.3522,
  "latitude": 48.8566,
  "status": "success"
}
```

#### 5. Get Balise Events
```http
GET /api/balises/1/events
```

**Response:**
```json
{
  "events": [
    {
      "id": 101,
      "eventType": "GPS_UPDATE",
      "eventTime": "2025-07-20T12:25:30",
      "batteryLevel": 85.5,
      "speed": 45.2,
      "heading": 180.0,
      "longitude": 2.3522,
      "latitude": 48.8566,
      "messageRaw": "GPS:48.8566,2.3522,45.2,180.0",
      "payload": "{\"type\":\"gps\",\"data\":\"...\"}"
    }
  ],
  "count": 1,
  "baliseId": 1,
  "status": "success"
}
```

#### 6. Send Command to Balise
```http
POST /api/balises/1/command
Content-Type: application/json

{
  "command": "LOCK",
  "parameters": {
    "timeout": 30
  }
}
```

**Response:**
```json
{
  "baliseId": 1,
  "command": "LOCK",
  "status": "queued",
  "message": "Command queued for processing",
  "timestamp": "2025-07-20T12:30:45"
}
```

## 🔧 Configuration

### Environment Variables
```yaml
# Database Configuration
SPRING_DATASOURCE_URL: jdbc:postgresql://balise-postgres:5432/balisedb
SPRING_DATASOURCE_USERNAME: adminbdb
SPRING_DATASOURCE_PASSWORD: To7Z2UCeWTsriPxbADX8

# Server Configuration
SERVER_PORT: 8080
SPRING_PROFILES_ACTIVE: backend

# JVM Configuration
JAVA_OPTS: -Xmx512m -Xms256m
```

### Application Configuration (application.yml)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://balise-postgres:5432/balisedb
    username: adminbdb
    password: To7Z2UCeWTsriPxbADX8
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

logging:
  level:
    com.maxvision: INFO
    org.springframework: WARN
```

## 🐳 Docker Configuration

### Current Dockerfile (Dockerfile.simple)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy SDK JAR and dependencies
COPY lib/*.jar /app/lib/

# Download PostgreSQL JDBC driver
RUN wget -O /app/lib/postgresql.jar https://jdbc.postgresql.org/download/postgresql-42.7.2.jar

# Create and compile simplified backend server
RUN echo 'Java HTTP Server implementation...' > /app/SpringBootBackendServer.java
RUN javac -cp "/app/lib/*" /app/SpringBootBackendServer.java

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

EXPOSE 8080
CMD ["/app/start.sh"]
```

### Docker Compose Configuration
```yaml
balise-sdk-service:
  build:
    context: ./backend-service
    dockerfile: Dockerfile.simple
    args:
      - CACHE_BUST=2025-07-20-12-40-NO-MAVEN
  container_name: balise-sdk-service
  ports:
    - "6062:8080"
  environment:
    - SPRING_DATASOURCE_URL=jdbc:postgresql://balise-postgres:5432/balisedb
    - SPRING_DATASOURCE_USERNAME=adminbdb
    - SPRING_DATASOURCE_PASSWORD=To7Z2UCeWTsriPxbADX8
    - SPRING_PROFILES_ACTIVE=backend
    - SERVER_PORT=8080
    - JAVA_OPTS=-Xmx512m -Xms256m
  healthcheck:
    test: ["CMD-SHELL", "nc -z localhost 8080 || exit 1"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 20s
  depends_on:
    balise-postgres:
      condition: service_healthy
```

## 📊 Database Integration

### Connection Management
- **Connection Pool**: HikariCP (Spring Boot default)
- **Driver**: PostgreSQL JDBC 42.5.1
- **Connection String**: `jdbc:postgresql://balise-postgres:5432/balisedb`
- **Credentials**: Environment variable based

### Database Operations
- **Read Operations**: SELECT queries for balise data and events
- **Write Operations**: INSERT/UPDATE for balise status and events
- **Spatial Queries**: PostGIS functions for location data
- **Transactions**: Spring-managed transactions

## 🔍 Monitoring & Logging

### Health Checks
- **Endpoint**: `/health`
- **Docker Health Check**: Every 30 seconds
- **Database Connectivity**: Verified on each health check
- **Response Time**: < 1 second typical

### Logging Configuration
```yaml
logging:
  level:
    com.maxvision.backend: INFO
    org.springframework.web: DEBUG
    org.springframework.jdbc: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Metrics
- **Request Count**: HTTP requests per endpoint
- **Response Times**: Average response time per endpoint
- **Database Connections**: Active connection count
- **Error Rates**: HTTP 4xx/5xx response rates

## 🚨 Error Handling

### HTTP Status Codes
- **200 OK**: Successful operations
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server errors
- **503 Service Unavailable**: Database connectivity issues

### Error Response Format
```json
{
  "status": "error",
  "error": "Database connection failed",
  "timestamp": "2025-07-20T12:30:45"
}
```

## 🔧 Development & Deployment

### Local Development
1. Ensure PostgreSQL is running
2. Set environment variables
3. Run `java -jar backend-service.jar`
4. Access API at `http://localhost:8080`

### Production Deployment
1. Build Docker image
2. Deploy via docker-compose or Portainer
3. Configure environment variables
4. Monitor health checks

### Testing
- **Unit Tests**: Service layer testing
- **Integration Tests**: Database integration
- **API Tests**: Endpoint functionality
- **Health Check Tests**: Service availability

---

The Backend API Service provides a robust, scalable foundation for fleet management operations with comprehensive REST API capabilities.
