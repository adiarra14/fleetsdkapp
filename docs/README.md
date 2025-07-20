# Fleet Monitor - Maxvision SDK Integration Documentation

<!--
/**
 * Fleet Monitor - Balise Management System
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

## 🚛 Overview

The Fleet Monitor system is a comprehensive balise (GPS tracking device) management platform that integrates the Maxvision Protocol Gateway SDK for real-time fleet monitoring, command management, and data processing.

## 📋 Table of Contents

- [Architecture Overview](./architecture.md)
- [Services Documentation](./services/README.md)
- [Database Schema](./database/README.md)
- [API Documentation](./api/README.md)
- [Deployment Guide](./deployment/README.md)
- [SDK Integration](./sdk/README.md)
- [Web UI Documentation](./web-ui/README.md)
- [Configuration Guide](./configuration/README.md)
- [Troubleshooting](./troubleshooting.md)

## 🎯 System Status

**Current Version**: 1.0.0-SNAPSHOT  
**Status**: ✅ Production Ready  
**Last Updated**: July 20, 2025  
**Branch**: `sdk-ok-wait-balisetest`

## 🏗️ System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web UI        │    │  Backend API    │    │  TCP Server     │
│  (Port 6061)    │◄──►│  (Port 6062)    │◄──►│  (Port 6060)    │
│  Fleet Monitor  │    │  REST API       │    │  Balise Data    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │  (Port 6063)    │
                    │  + PostGIS      │
                    └─────────────────┘
```

## 🚀 Quick Start

1. **Prerequisites**: Docker, Portainer, PostgreSQL
2. **Deployment**: Use `docker-compose.yml` for full stack deployment
3. **Access**: Fleet Monitor dashboard at `http://your-host:6061`
4. **API**: Backend API available at `http://your-host:6062/api`

## 📊 Key Features

### ✅ Implemented Features
- **Real-time Fleet Monitoring Dashboard**
- **REST API for Balise Management**
- **PostgreSQL Database with PostGIS**
- **Maxvision SDK Integration**
- **Docker Containerization**
- **Portainer Deployment Support**
- **CORS-enabled API**
- **Health Monitoring**
- **Auto-refresh Dashboard**

### 🔄 Ready for Testing
- **Balise Data Reception** (TCP Server ready)
- **Command Sending** (API endpoints ready)
- **GPS Tracking** (PostGIS spatial support)
- **Event Processing** (Database schema ready)

## 🛠️ Services Overview

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| **Web UI** | 6061 | ✅ Running | Fleet Monitor Dashboard |
| **Backend API** | 6062 | ✅ Running | REST API & Business Logic |
| **TCP Server** | 6060 | ✅ Running | Balise Data Reception |
| **PostgreSQL** | 6063 | ✅ Running | Database with PostGIS |

## 📈 Current Metrics

- **Total Services**: 4 containerized services
- **Database Tables**: 4 (balises, balise_events, containers, assets)
- **API Endpoints**: 6 REST endpoints
- **SDK Dependencies**: 7 required libraries (all present)
- **Docker Images**: Custom builds for all services

## 🔗 External Dependencies

- **Maxvision SDK**: `maxvision-edge-protocol-gateway-service-sdk.jar`
- **Spring Boot**: 2.7.0
- **PostgreSQL**: 14 with PostGIS 3.3
- **Java**: 17 (Eclipse Temurin)
- **Node.js**: 16 (for web UI build)

## 📞 Support

For technical support or questions:
- Check [Troubleshooting Guide](./troubleshooting.md)
- Review [Configuration Guide](./configuration/README.md)
- Consult [API Documentation](./api/README.md)

---

**Note**: This system is ready for balise testing and production deployment. All SDK requirements have been verified and all services are operational.
