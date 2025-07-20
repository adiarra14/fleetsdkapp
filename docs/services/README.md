# Services Documentation

<!--
/**
 * Fleet Monitor - Balise Management System
 * Services Documentation
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This software and associated documentation files are the property
 * of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
 * 
 * Unauthorized copying, modification, distribution, or use of this software
 * is strictly prohibited without prior written consent from Ynnov.
 */
-->

## 🚀 Fleet Monitor Services Overview

The Fleet Monitor system consists of four main containerized services that work together to provide comprehensive balise fleet management capabilities.

## 📋 Service Index

1. [Web UI Service](./web-ui.md) - Frontend Dashboard
2. [Backend API Service](./backend-api.md) - REST API & Business Logic
3. [TCP Server Service](./tcp-server.md) - Balise Protocol Handler
4. [Database Service](./database.md) - PostgreSQL with PostGIS

## 🏗️ Service Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web UI        │    │  Backend API    │    │  TCP Server     │
│  nginx:alpine   │◄──►│ temurin:17-jdk  │◄──►│ temurin:17-jdk  │
│  Port: 6061     │    │  Port: 6062     │    │  Port: 6060     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Database      │
                    │ postgis:14-3.3  │
                    │  Port: 6063     │
                    └─────────────────┘
```

## 📊 Service Status Matrix

| Service | Container Name | Image | Port | Status | Health Check |
|---------|----------------|-------|------|--------|--------------|
| **Web UI** | `balise-web-ui` | `nginx:alpine` | 6061 | ✅ Running | HTTP GET / |
| **Backend API** | `balise-sdk-service` | `eclipse-temurin:17-jdk-alpine` | 6062 | ✅ Running | HTTP GET /health |
| **TCP Server** | `balise-tcp-server` | `eclipse-temurin:17-jdk-alpine` | 6060 | ✅ Running | TCP Socket Check |
| **Database** | `balise-postgres` | `postgis/postgis:14-3.3` | 6063 | ✅ Running | PostgreSQL Ready |

## 🔄 Service Dependencies

### Startup Order
1. **Database Service** (PostgreSQL) - Must be ready first
2. **Backend API Service** - Depends on database
3. **TCP Server Service** - Depends on database
4. **Web UI Service** - Depends on backend API

### Runtime Dependencies
- **Web UI** → **Backend API** (REST API calls)
- **Backend API** → **Database** (Data persistence)
- **TCP Server** → **Database** (Event storage)
- **TCP Server** → **Maxvision SDK** (Protocol handling)

## 🌐 Network Communication

### Internal Docker Network
- All services communicate via Docker internal network
- Service discovery by container names
- No external network dependencies for inter-service communication

### External Access Points
- **Web UI**: `http://host:6061` - Public dashboard access
- **Backend API**: `http://host:6062` - REST API endpoints
- **TCP Server**: `tcp://host:6060` - Balise device connections
- **Database**: `tcp://host:6063` - Database management tools

## 🔧 Configuration Management

### Environment Variables
Each service uses environment variables for configuration:

```yaml
# Backend API Service
SPRING_DATASOURCE_URL: jdbc:postgresql://balise-postgres:5432/balisedb
SPRING_DATASOURCE_USERNAME: adminbdb
SPRING_DATASOURCE_PASSWORD: To7Z2UCeWTsriPxbADX8
SERVER_PORT: 8080

# TCP Server Service
SPRING_PROFILES_ACTIVE: tcp-server
MVGATEWAY_PORT: 6060
MVGATEWAY_ENCRYPTION_ENABLED: true

# Database Service
POSTGRES_DB: balisedb
POSTGRES_USER: adminbdb
POSTGRES_PASSWORD: To7Z2UCeWTsriPxbADX8
```

## 📈 Performance Characteristics

### Resource Requirements
| Service | CPU | Memory | Disk | Network |
|---------|-----|--------|------|---------|
| **Web UI** | Low | 64MB | 100MB | Low |
| **Backend API** | Medium | 512MB | 200MB | Medium |
| **TCP Server** | Medium | 512MB | 200MB | High |
| **Database** | High | 1GB | 5GB+ | Medium |

### Scaling Considerations
- **Web UI**: Stateless, easily scalable
- **Backend API**: Stateless, can be load balanced
- **TCP Server**: Stateful connections, vertical scaling preferred
- **Database**: Single instance with backup/replication options

## 🔍 Monitoring & Health Checks

### Health Check Endpoints
- **Web UI**: `GET /` (200 OK)
- **Backend API**: `GET /health` (JSON response)
- **TCP Server**: Socket connection test
- **Database**: PostgreSQL connection test

### Logging
- All services log to stdout/stderr
- Logs are captured by Docker/Portainer
- Structured logging with timestamps
- Error levels: DEBUG, INFO, WARN, ERROR

## 🚨 Troubleshooting Quick Reference

### Common Issues
1. **Service Won't Start**: Check dependencies and environment variables
2. **Connection Refused**: Verify port mappings and network configuration
3. **Database Connection Failed**: Check PostgreSQL service status
4. **API 404 Errors**: Verify backend service is running and endpoints are registered

### Debug Commands
```bash
# Check service status
docker ps

# View service logs
docker logs <container_name>

# Test connectivity
curl http://localhost:6062/health
telnet localhost 6060
```

## 📚 Detailed Service Documentation

For detailed information about each service, see the individual documentation files:

- [Web UI Service Documentation](./web-ui.md)
- [Backend API Service Documentation](./backend-api.md)
- [TCP Server Service Documentation](./tcp-server.md)
- [Database Service Documentation](./database.md)

---

Each service is designed to be independently deployable and maintainable while working together as a cohesive fleet management system.
