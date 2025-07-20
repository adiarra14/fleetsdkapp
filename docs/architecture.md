# System Architecture Documentation

<!--
/**
 * Fleet Monitor - Balise Management System
 * Architecture Documentation
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

## 🏗️ Fleet Monitor Architecture Overview

The Fleet Monitor system follows a microservices architecture pattern with containerized services for scalability, maintainability, and deployment flexibility.

## 📐 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FLEET MONITOR SYSTEM                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Web UI    │    │ Backend API │    │ TCP Server  │         │
│  │             │    │             │    │             │         │
│  │ - Dashboard │◄──►│ - REST API  │◄──►│ - SDK Integ │         │
│  │ - Monitoring│    │ - Business  │    │ - Protocol  │         │
│  │ - Commands  │    │   Logic     │    │   Handler   │         │
│  │             │    │ - CORS      │    │ - Data Proc │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│         │                   │                   │               │
│         │                   │                   │               │
│         └───────────────────┼───────────────────┘               │
│                             │                                   │
│                    ┌─────────────┐                              │
│                    │ PostgreSQL  │                              │
│                    │             │                              │
│                    │ - PostGIS   │                              │
│                    │ - Spatial   │                              │
│                    │ - Events    │                              │
│                    │ - Balises   │                              │
│                    └─────────────┘                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 Component Architecture

### 1. **Web UI Layer** (Frontend)
```
┌─────────────────────────────────────────┐
│              WEB UI (Nginx)             │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │   HTML5     │  │   JavaScript    │   │
│  │             │  │                 │   │
│  │ - Dashboard │  │ - API Client    │   │
│  │ - Tables    │  │ - Auto-refresh  │   │
│  │ - Charts    │  │ - AJAX Calls    │   │
│  │ - Forms     │  │ - Event Handlers│   │
│  └─────────────┘  └─────────────────┘   │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │           CSS3 Styling              │ │
│  │                                     │ │
│  │ - Responsive Design                 │ │
│  │ - Modern UI Components              │ │
│  │ - Status Indicators                 │ │
│  │ - Interactive Elements              │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 2. **Backend API Layer** (Business Logic)
```
┌─────────────────────────────────────────┐
│           BACKEND API SERVICE           │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │         Spring Boot                 │ │
│  │                                     │ │
│  │ ┌─────────────┐ ┌─────────────────┐ │ │
│  │ │Controllers  │ │   Services      │ │ │
│  │ │             │ │                 │ │ │
│  │ │- BaliseCtrl │ │- Business Logic │ │ │
│  │ │- HealthCtrl │ │- Data Validation│ │ │
│  │ │- StatusCtrl │ │- Command Proc   │ │ │
│  │ └─────────────┘ └─────────────────┘ │ │
│  │                                     │ │
│  │ ┌─────────────────────────────────┐ │ │
│  │ │        Data Access Layer        │ │ │
│  │ │                                 │ │ │
│  │ │ - JDBC Templates                │ │ │
│  │ │ - Connection Pooling            │ │ │
│  │ │ - Transaction Management        │ │ │
│  │ └─────────────────────────────────┘ │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 3. **TCP Server Layer** (Protocol Handler)
```
┌─────────────────────────────────────────┐
│            TCP SERVER SERVICE           │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │         Maxvision SDK               │ │
│  │                                     │ │
│  │ ┌─────────────┐ ┌─────────────────┐ │ │
│  │ │Protocol     │ │  SDK Services   │ │ │
│  │ │Gateway      │ │                 │ │ │
│  │ │             │ │- LockReportSvc  │ │ │
│  │ │- Port 6060  │ │- LockSettingSvc │ │ │
│  │ │- Encryption │ │- Command Handler│ │ │
│  │ │- Auth       │ │- Event Processor│ │ │
│  │ └─────────────┘ └─────────────────┘ │ │
│  │                                     │ │
│  │ ┌─────────────────────────────────┐ │ │
│  │ │        Spring Boot              │ │ │
│  │ │                                 │ │ │
│  │ │ - Component Scanning            │ │ │
│  │ │ - Bean Management               │ │ │
│  │ │ - Configuration                 │ │ │
│  │ └─────────────────────────────────┘ │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

### 4. **Database Layer** (Data Persistence)
```
┌─────────────────────────────────────────┐
│           POSTGRESQL DATABASE           │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │            PostGIS                  │ │
│  │                                     │ │
│  │ ┌─────────────┐ ┌─────────────────┐ │ │
│  │ │Spatial Data │ │   Core Tables   │ │ │
│  │ │             │ │                 │ │ │
│  │ │- GPS Coords │ │- balises        │ │ │
│  │ │- Geometry   │ │- balise_events  │ │ │
│  │ │- Indexes    │ │- containers     │ │ │
│  │ │- Functions  │ │- assets         │ │ │
│  │ └─────────────┘ └─────────────────┘ │ │
│  │                                     │ │
│  │ ┌─────────────────────────────────┐ │ │
│  │ │        Performance              │ │ │
│  │ │                                 │ │ │
│  │ │ - Indexes on key columns        │ │ │
│  │ │ - Spatial indexes               │ │ │
│  │ │ - Connection pooling            │ │ │
│  │ └─────────────────────────────────┘ │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## 🔄 Data Flow Architecture

### 1. **Balise Data Reception Flow**
```
Balise Device → TCP Server (6060) → SDK Processing → Database → Backend API → Web UI
```

### 2. **Command Sending Flow**
```
Web UI → Backend API → TCP Server → SDK → Balise Device
```

### 3. **Dashboard Update Flow**
```
Database → Backend API (REST) → Web UI (AJAX) → User Interface
```

## 🐳 Container Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        DOCKER ENVIRONMENT                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   nginx     │  │eclipse-tem  │  │eclipse-tem  │             │
│  │   :alpine   │  │urin:17-jdk  │  │urin:17-jdk  │             │
│  │             │  │   -alpine   │  │   -alpine   │             │
│  │ Web UI      │  │             │  │             │             │
│  │ Container   │  │ Backend API │  │ TCP Server  │             │
│  │             │  │ Container   │  │ Container   │             │
│  │ Port: 6061  │  │ Port: 6062  │  │ Port: 6060  │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
│         │                 │                 │                  │
│         │                 │                 │                  │
│         └─────────────────┼─────────────────┘                  │
│                           │                                    │
│                  ┌─────────────┐                               │
│                  │ postgis     │                               │
│                  │ :14-3.3     │                               │
│                  │             │                               │
│                  │ PostgreSQL  │                               │
│                  │ Container   │                               │
│                  │             │                               │
│                  │ Port: 6063  │                               │
│                  └─────────────┘                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 🌐 Network Architecture

### Port Mapping
- **6061**: Web UI (HTTP) - Public access to dashboard
- **6062**: Backend API (HTTP) - REST API endpoints
- **6060**: TCP Server (TCP) - Balise device connections
- **6063**: PostgreSQL (TCP) - Database connections

### Internal Communication
- All services communicate via Docker internal network
- Service discovery by container names
- No external dependencies for inter-service communication

### Security Considerations
- CORS enabled for frontend-backend communication
- Database credentials managed via environment variables
- Internal network isolation
- Health checks for all services

## 📊 Scalability Architecture

### Horizontal Scaling Points
1. **Backend API**: Can be load-balanced with multiple instances
2. **TCP Server**: Can handle multiple balise connections
3. **Database**: PostgreSQL supports read replicas
4. **Web UI**: Static files can be CDN-distributed

### Performance Optimizations
- **Database Indexes**: On frequently queried columns
- **Connection Pooling**: For database connections
- **Caching**: Ready for Redis integration
- **Async Processing**: Event-driven architecture ready

## 🔧 Configuration Architecture

### Environment-Based Configuration
- **Development**: Local Docker Compose
- **Production**: Portainer with environment variables
- **Testing**: Isolated test containers

### Configuration Sources
1. **Environment Variables**: Runtime configuration
2. **application.yml**: Service-specific settings
3. **docker-compose.yml**: Container orchestration
4. **Dockerfile**: Build-time configuration

---

This architecture provides a solid foundation for fleet management with room for growth and scalability as your balise fleet expands.
