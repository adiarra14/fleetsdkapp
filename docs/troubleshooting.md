# Troubleshooting Guide

## 🚨 Fleet Monitor System Troubleshooting

This guide helps diagnose and resolve common issues with the Fleet Monitor system deployment and operation.

## 🔍 Quick Diagnostics

### System Health Check
```bash
# Check all services status
docker-compose ps

# Test API endpoints
curl http://localhost:6062/health
curl http://localhost:6062/api/status

# Test database connectivity
docker exec balise-postgres pg_isready -U adminbdb

# Check web UI
curl http://localhost:6061
```

### Service Connectivity Matrix
| From | To | Test Command | Expected Result |
|------|----|--------------|-----------------| 
| Web UI | Backend API | `curl http://balise-sdk-service:8080/health` | HTTP 200 |
| Backend | Database | `psql -h balise-postgres -U adminbdb balisedb` | Connection OK |
| TCP Server | Database | `psql -h balise-postgres -U adminbdb balisedb` | Connection OK |
| External | Web UI | `curl http://localhost:6061` | HTML response |

## 🐳 Docker & Container Issues

### Container Won't Start

#### Symptoms
- Service shows as "Exited" status
- Container restarts continuously
- "failed to create task" errors

#### Diagnosis
```bash
# Check container logs
docker-compose logs <service-name>

# Check resource usage
docker stats

# Inspect container configuration
docker inspect <container-name>
```

#### Common Solutions

**1. Port Conflicts**
```bash
# Check if port is in use
netstat -tulpn | grep :6062

# Kill process using port
sudo kill -9 $(lsof -t -i:6062)
```

**2. Memory Issues**
```bash
# Check available memory
free -h

# Increase Docker memory limits
# Edit docker-compose.yml:
deploy:
  resources:
    limits:
      memory: 1G
```

**3. Volume Mount Issues**
```bash
# Check volume permissions
ls -la ./backend-service/lib/

# Fix permissions
chmod -R 755 ./backend-service/lib/
```

### Image Build Failures

#### Maven Build Errors
**Problem**: `mvn clean package` fails in Docker
**Solution**: Use simplified Dockerfile without Maven
```dockerfile
# Use Dockerfile.simple instead of Dockerfile
dockerfile: Dockerfile.simple
```

**Problem**: Dependencies not found
**Solution**: Ensure SDK JAR is present
```bash
ls -la backend-service/lib/maxvision-edge-protocol-gateway-service-sdk.jar
```

#### Docker Cache Issues
**Problem**: Old images being used despite updates
**Solution**: Force rebuild
```bash
# Clear Docker cache
docker system prune -a

# Rebuild without cache
docker-compose build --no-cache

# Update cache bust argument
args:
  - CACHE_BUST=$(date +%s)
```

## 🌐 Network & Connectivity Issues

### API Connection Failures

#### Web UI Shows "Disconnected"
**Symptoms**: Red status indicator, "Failed to connect to backend API"

**Diagnosis**:
```bash
# Test backend API directly
curl http://localhost:6062/health
curl http://localhost:6062/api/status

# Check if backend container is running
docker ps | grep balise-sdk-service

# Check backend logs
docker logs balise-sdk-service
```

**Solutions**:
1. **Backend not running**: Restart backend service
2. **Wrong API URL**: Verify web UI uses `/api` (relative URL)
3. **CORS issues**: Check backend CORS configuration
4. **Port mapping**: Verify `6062:8080` mapping

#### TCP Server Connection Issues
**Symptoms**: Balise devices can't connect to port 6060

**Diagnosis**:
```bash
# Test TCP port
telnet localhost 6060

# Check if port is listening
netstat -tulpn | grep :6060

# Check TCP server logs
docker logs balise-tcp-server
```

**Solutions**:
1. **Port not exposed**: Add port mapping `6060:6060`
2. **Firewall blocking**: Open port 6060
3. **Service not listening**: Check TCP server startup logs

### Database Connection Issues

#### "Database connection failed" Errors
**Symptoms**: Backend/TCP server can't connect to PostgreSQL

**Diagnosis**:
```bash
# Test database connectivity
docker exec balise-postgres pg_isready -U adminbdb

# Check database logs
docker logs balise-postgres

# Test connection from backend container
docker exec balise-sdk-service pg_isready -h balise-postgres -U adminbdb
```

**Solutions**:
1. **Database not ready**: Wait for PostgreSQL startup
2. **Wrong credentials**: Verify environment variables
3. **Network issues**: Check Docker network connectivity
4. **Database not initialized**: Run initialization scripts

## 🗄️ Database Issues

### Tables Not Found
**Symptoms**: "relation does not exist" errors

**Diagnosis**:
```sql
-- Connect to database
docker exec -it balise-postgres psql -U adminbdb balisedb

-- Check if tables exist
\dt

-- Check table structure
\d balises
```

**Solutions**:
1. **Run initialization scripts**:
```bash
docker exec -i balise-postgres psql -U adminbdb balisedb < create-tables-manually.sql
```

2. **Check volume mounts**:
```yaml
volumes:
  - ./database/init:/docker-entrypoint-initdb.d
```

### PostGIS Extension Issues
**Symptoms**: Spatial queries fail, geometry functions not found

**Diagnosis**:
```sql
-- Check PostGIS extension
SELECT postgis_version();

-- List extensions
\dx
```

**Solution**:
```sql
-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
```

## 🔧 Application-Specific Issues

### Spring Boot Startup Failures

#### Bean Creation Errors
**Symptoms**: "Error creating bean" in logs

**Common Causes**:
1. **Missing dependencies**: Check pom.xml
2. **Bean name conflicts**: Use CustomBeanNameGenerator
3. **Configuration errors**: Verify application.yml

**Solutions**:
```java
// Use custom bean name generator
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.maxvision.backend",
    "com.maxvision.backend.controller"
})
public class BackendApplication {
    @Bean
    public BeanNameGenerator beanNameGenerator() {
        return new CustomBeanNameGenerator();
    }
}
```

#### SDK Integration Issues
**Symptoms**: SDK classes not found, protocol errors

**Diagnosis**:
```bash
# Check if SDK JAR is present
docker exec balise-tcp-server ls -la /app/lib/

# Check classpath
docker exec balise-tcp-server java -cp "/app/lib/*" -version
```

**Solutions**:
1. **Add SDK JAR to lib directory**
2. **Update Maven dependencies**
3. **Configure component scanning for SDK packages**

### Web UI Issues

#### Dashboard Shows No Data
**Symptoms**: "No balises found" message, zero statistics

**Diagnosis**:
```bash
# Test API endpoints directly
curl http://localhost:6062/api/balises
curl http://localhost:6062/api/status

# Check database for data
docker exec -it balise-postgres psql -U adminbdb balisedb -c "SELECT COUNT(*) FROM balises;"
```

**Solutions**:
1. **Add test data to database**
2. **Verify API endpoints return data**
3. **Check web UI API URL configuration**

#### JavaScript Errors
**Symptoms**: Console errors, broken functionality

**Diagnosis**: Open browser developer tools, check console

**Common Fixes**:
1. **API URL**: Ensure relative URLs (`/api` not `http://localhost:6062/api`)
2. **CORS**: Verify backend allows cross-origin requests
3. **JSON parsing**: Check API response format

## 🏗️ Portainer-Specific Issues

### Stack Deployment Failures

#### Git Repository Issues
**Symptoms**: "failed to clone repository" errors

**Solutions**:
1. **Check repository URL**: `https://github.com/adiarra14/fleetsdkapp`
2. **Verify branch exists**: `sdk-ok-wait-balisetest`
3. **Check network connectivity**
4. **Use SSH key if private repository**

#### Image Build Failures in Portainer
**Symptoms**: Build fails with Maven or dependency errors

**Solutions**:
1. **Use simplified Dockerfiles**: `Dockerfile.simple`
2. **Pre-build images locally and push to registry**
3. **Use cache-busting arguments**
4. **Check Portainer build logs**

#### Service Not Starting in Portainer
**Symptoms**: Services show as stopped or unhealthy

**Diagnosis**:
1. Check Portainer container logs
2. Review resource limits
3. Verify environment variables
4. Check network configuration

**Solutions**:
1. **Increase resource limits**
2. **Fix environment variable syntax**
3. **Remove complex volume mounts**
4. **Use custom Docker images instead of volume mounts**

### Persistent Caching Issues
**Problem**: Portainer uses old images despite updates

**Solutions**:
1. **Update cache-bust arguments**:
```yaml
args:
  - CACHE_BUST=2025-07-20-14-15-NEW
```

2. **Delete and recreate stack**
3. **Use explicit image tags**
4. **Clear Portainer image cache**

## 📊 Performance Issues

### Slow API Responses
**Symptoms**: Dashboard takes long to load, API timeouts

**Diagnosis**:
```bash
# Check response times
time curl http://localhost:6062/api/balises

# Monitor database queries
docker exec balise-postgres tail -f /var/log/postgresql/postgresql.log
```

**Solutions**:
1. **Add database indexes**:
```sql
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balise_events_time ON balise_events(event_time);
```

2. **Optimize queries**
3. **Increase container resources**
4. **Use connection pooling**

### High Memory Usage
**Symptoms**: Containers consuming excessive memory

**Solutions**:
```yaml
# Set memory limits
deploy:
  resources:
    limits:
      memory: 512M
```

```bash
# Optimize JVM settings
environment:
  - JAVA_OPTS=-Xmx256m -Xms128m
```

## 🔍 Debug Commands Reference

### Container Debugging
```bash
# Enter container shell
docker exec -it balise-sdk-service /bin/sh

# Check running processes
docker exec balise-sdk-service ps aux

# Check file system
docker exec balise-sdk-service ls -la /app/

# Check network connectivity
docker exec balise-sdk-service ping balise-postgres
```

### Database Debugging
```bash
# Connect to database
docker exec -it balise-postgres psql -U adminbdb balisedb

# Check database size
docker exec balise-postgres du -sh /var/lib/postgresql/data

# Monitor database activity
docker exec balise-postgres pg_stat_activity
```

### Network Debugging
```bash
# Check Docker networks
docker network ls
docker network inspect <network-name>

# Test service connectivity
docker exec balise-sdk-service nslookup balise-postgres
docker exec balise-sdk-service telnet balise-postgres 5432
```

---

This troubleshooting guide covers the most common issues encountered with the Fleet Monitor system. For additional support, check the service-specific documentation and logs.
