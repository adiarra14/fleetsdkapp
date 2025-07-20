# Deployment Guide

## 🚀 Fleet Monitor Deployment Documentation

This guide covers deployment options for the Fleet Monitor system, including Docker Compose, Portainer, and production considerations.

## 📋 Deployment Options

### 1. Local Docker Compose
Best for development and testing.

### 2. Portainer Stack Deployment
Recommended for production environments.

### 3. Kubernetes (Future)
For large-scale deployments.

## 🐳 Docker Compose Deployment

### Prerequisites
- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 10GB disk space

### Quick Start
```bash
# Clone the repository
git clone https://github.com/adiarra14/fleetsdkapp.git
cd fleetsdkapp

# Deploy the stack
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

### Services Overview
```yaml
services:
  balise-sdk-service:     # Backend API (Port 6062)
  balise-tcp-server:      # TCP Server (Port 6060)
  frontend:               # Web UI (Port 6061)
  balise-postgres:        # Database (Port 6063)
```

### Environment Configuration
Create `.env` file for customization:
```env
# Database Configuration
POSTGRES_DB=balisedb
POSTGRES_USER=adminbdb
POSTGRES_PASSWORD=To7Z2UCeWTsriPxbADX8

# External Port Mappings
WEB_UI_PORT=6061
BACKEND_API_PORT=6062
DATABASE_PORT=6063
TCP_SERVER_PORT=6060

# JVM Configuration
JAVA_OPTS=-Xmx512m -Xms256m
```

## 🏗️ Portainer Stack Deployment

### Prerequisites
- Portainer CE/EE 2.0+
- Docker Swarm or Standalone mode
- Git repository access
- Network connectivity for image pulls

### Deployment Steps

#### 1. Access Portainer
Navigate to your Portainer instance:
```
https://your-portainer-host:9443
```

#### 2. Create New Stack
1. Go to **Stacks** → **Add stack**
2. Choose **Repository** deployment method
3. Configure repository settings:
   - **Repository URL**: `https://github.com/adiarra14/fleetsdkapp`
   - **Repository reference**: `sdk-ok-wait-balisetest`
   - **Compose path**: `docker-compose.yml`

#### 3. Environment Variables
Set the following environment variables in Portainer:
```
POSTGRES_PASSWORD=To7Z2UCeWTsriPxbADX8
JAVA_OPTS=-Xmx512m -Xms256m
```

#### 4. Deploy Stack
1. Click **Deploy the stack**
2. Monitor deployment progress
3. Verify all services are running

### Portainer-Specific Considerations

#### Image Caching
Portainer may cache Docker images. To force rebuild:
1. Update `CACHE_BUST` argument in docker-compose.yml
2. Redeploy the stack
3. Or delete and recreate the stack

#### Volume Mounts
Avoid complex volume mounts in Portainer:
- Use custom Docker images instead
- Embed files in container builds
- Use init containers for setup

#### Network Configuration
Portainer creates isolated networks:
- Services communicate via container names
- External access via port mappings
- No additional network configuration needed

## 🔧 Production Deployment

### Hardware Requirements

#### Minimum Requirements
- **CPU**: 2 cores
- **RAM**: 4GB
- **Disk**: 20GB SSD
- **Network**: 100Mbps

#### Recommended Requirements
- **CPU**: 4 cores
- **RAM**: 8GB
- **Disk**: 50GB SSD
- **Network**: 1Gbps

### Security Considerations

#### Network Security
```yaml
# Restrict external access
ports:
  - "127.0.0.1:6062:8080"  # Backend API (localhost only)
  - "6061:80"              # Web UI (public)
  - "6060:6060"            # TCP Server (balise devices)
```

#### Database Security
```yaml
environment:
  - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}  # Use secrets
  - POSTGRES_HOST_AUTH_METHOD=md5           # Require password
```

#### SSL/TLS Configuration
```nginx
# Nginx SSL configuration for Web UI
server {
    listen 443 ssl;
    ssl_certificate /etc/ssl/certs/fleet-monitor.crt;
    ssl_certificate_key /etc/ssl/private/fleet-monitor.key;
    
    location / {
        proxy_pass http://frontend:80;
    }
    
    location /api/ {
        proxy_pass http://balise-sdk-service:8080/api/;
    }
}
```

### Backup Strategy

#### Database Backup
```bash
# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec balise-postgres pg_dump -U adminbdb balisedb > backup_${DATE}.sql
```

#### Configuration Backup
```bash
# Backup docker-compose and configs
tar -czf config_backup_${DATE}.tar.gz \
    docker-compose.yml \
    .env \
    database/init/ \
    docs/
```

### Monitoring Setup

#### Health Checks
All services include health checks:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 20s
```

#### Log Management
```yaml
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

#### Metrics Collection
Consider integrating:
- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **AlertManager**: Alerting

## 🔄 Update and Maintenance

### Rolling Updates
```bash
# Update specific service
docker-compose up -d --no-deps balise-sdk-service

# Update all services
docker-compose pull
docker-compose up -d
```

### Database Migrations
```bash
# Run migration scripts
docker exec -i balise-postgres psql -U adminbdb balisedb < migration.sql
```

### Service Scaling
```yaml
# Scale backend service
docker-compose up -d --scale balise-sdk-service=3
```

## 🚨 Troubleshooting Deployment

### Common Issues

#### Service Won't Start
```bash
# Check service logs
docker-compose logs balise-sdk-service

# Check resource usage
docker stats

# Verify port availability
netstat -tulpn | grep :6062
```

#### Database Connection Failed
```bash
# Test database connectivity
docker exec balise-postgres pg_isready -U adminbdb

# Check database logs
docker-compose logs balise-postgres

# Verify credentials
docker exec -it balise-postgres psql -U adminbdb balisedb
```

#### Image Build Failures
```bash
# Clean Docker cache
docker system prune -a

# Rebuild specific service
docker-compose build --no-cache balise-sdk-service

# Check Dockerfile syntax
docker build -t test ./backend-service/
```

### Portainer-Specific Issues

#### Stack Deployment Failed
1. Check Git repository access
2. Verify branch exists: `sdk-ok-wait-balisetest`
3. Ensure docker-compose.yml is valid
4. Check Portainer logs

#### Services Not Starting
1. Review container logs in Portainer UI
2. Check resource limits
3. Verify environment variables
4. Ensure network connectivity

#### Image Caching Problems
1. Update `CACHE_BUST` arguments
2. Delete and recreate stack
3. Clear Portainer image cache
4. Use explicit image tags

## 📊 Performance Optimization

### Database Optimization
```sql
-- Create indexes for better performance
CREATE INDEX CONCURRENTLY idx_balise_events_time ON balise_events(event_time);
CREATE INDEX CONCURRENTLY idx_balises_status ON balises(status);

-- Analyze tables
ANALYZE balises;
ANALYZE balise_events;
```

### Container Optimization
```yaml
# Resource limits
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 512M
    reservations:
      cpus: '0.5'
      memory: 256M
```

### Network Optimization
```yaml
# Use custom network
networks:
  fleet-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

---

This deployment guide provides comprehensive instructions for deploying the Fleet Monitor system in various environments, from development to production.
