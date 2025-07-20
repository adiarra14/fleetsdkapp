# Database Documentation

## 🗄️ PostgreSQL Database with PostGIS

The Fleet Monitor system uses PostgreSQL 14 with PostGIS 3.3 extension for spatial data support, providing robust data persistence for balise fleet management.

## 🏗️ Database Architecture

### Technology Stack
- **Database**: PostgreSQL 14
- **Spatial Extension**: PostGIS 3.3
- **Container**: `postgis/postgis:14-3.3`
- **Port**: 5432 (internal), 6063 (external)
- **Storage**: Persistent Docker volume

### Connection Details
```
Host: balise-postgres (Docker internal) / localhost:6063 (external)
Database: balisedb
Username: adminbdb
Password: To7Z2UCeWTsriPxbADX8
```

## 📊 Database Schema

### Schema Overview
```sql
balisedb
├── Extensions
│   └── postgis              -- Spatial data support
├── Tables
│   ├── balises             -- GPS tracking devices
│   ├── balise_events       -- Device events and GPS updates
│   ├── containers          -- Shipping containers
│   └── assets              -- Fleet assets
└── Indexes
    ├── Spatial indexes     -- Geographic queries
    └── Performance indexes -- Query optimization
```

### Table Relationships
```
┌─────────────┐    1:N    ┌─────────────────┐
│   balises   │◄─────────►│ balise_events   │
│             │           │                 │
│ - id (PK)   │           │ - id (PK)       │
│ - name      │           │ - balise_id (FK)│
│ - imei      │           │ - event_type    │
│ - location  │           │ - location      │
└─────────────┘           └─────────────────┘
       │                           │
       │ 1:N                       │
       ▼                           ▼
┌─────────────┐           ┌─────────────────┐
│ containers  │           │     assets      │
│             │           │                 │
│ - id (PK)   │           │ - id (PK)       │
│ - balise_id │           │ - name          │
│ - number    │           │ - type          │
└─────────────┘           └─────────────────┘
```

## 📋 Table Schemas

### 1. Balises Table
Primary table for GPS tracking devices.

```sql
CREATE TABLE balises (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    imei VARCHAR(15) UNIQUE NOT NULL,
    type VARCHAR(50) DEFAULT 'GPS_TRACKER',
    status VARCHAR(20) DEFAULT 'INACTIVE',
    battery_level DECIMAL(5,2) DEFAULT 0.0,
    last_seen TIMESTAMP,
    location GEOMETRY(POINT, 4326),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**
- `id`: Primary key, auto-increment
- `name`: Human-readable device name
- `imei`: Unique device identifier (15 digits)
- `type`: Device type (GPS_TRACKER, LOCK_DEVICE, etc.)
- `status`: Current status (ACTIVE, INACTIVE, MAINTENANCE)
- `battery_level`: Battery percentage (0.0-100.0)
- `last_seen`: Last communication timestamp
- `location`: GPS coordinates (PostGIS geometry)
- `created_at`: Device registration timestamp
- `updated_at`: Last update timestamp

### 2. Balise Events Table
Stores all events and GPS updates from balises.

```sql
CREATE TABLE balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER NOT NULL REFERENCES balises(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location GEOMETRY(POINT, 4326),
    battery_level DECIMAL(5,2),
    speed DECIMAL(8,2),
    heading DECIMAL(5,2),
    message_raw TEXT,
    payload JSONB,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**
- `id`: Primary key, auto-increment
- `balise_id`: Foreign key to balises table
- `event_type`: Type of event (GPS_UPDATE, ALARM, HEARTBEAT, etc.)
- `event_time`: When the event occurred
- `location`: GPS coordinates at event time
- `battery_level`: Battery level at event time
- `speed`: Vehicle speed (km/h)
- `heading`: Direction in degrees (0-360)
- `message_raw`: Raw message from device
- `payload`: JSON data payload
- `processed`: Whether event has been processed
- `created_at`: When event was recorded

### 3. Containers Table
Shipping containers associated with balises.

```sql
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id) ON DELETE SET NULL,
    container_number VARCHAR(20) UNIQUE NOT NULL,
    type VARCHAR(50) DEFAULT 'STANDARD',
    status VARCHAR(20) DEFAULT 'EMPTY',
    location GEOMETRY(POINT, 4326),
    destination VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. Assets Table
General fleet assets and equipment.

```sql
CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    location GEOMETRY(POINT, 4326),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔍 Indexes and Performance

### Primary Indexes
```sql
-- Primary keys (automatic)
CREATE INDEX idx_balises_pkey ON balises(id);
CREATE INDEX idx_balise_events_pkey ON balise_events(id);
CREATE INDEX idx_containers_pkey ON containers(id);
CREATE INDEX idx_assets_pkey ON assets(id);
```

### Foreign Key Indexes
```sql
-- Foreign key relationships
CREATE INDEX idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX idx_containers_balise_id ON containers(balise_id);
```

### Performance Indexes
```sql
-- Query optimization
CREATE INDEX idx_balises_imei ON balises(imei);
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balise_events_event_type ON balise_events(event_type);
CREATE INDEX idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX idx_balise_events_processed ON balise_events(processed);
```

### Spatial Indexes
```sql
-- PostGIS spatial indexes
CREATE INDEX idx_balises_location ON balises USING GIST(location);
CREATE INDEX idx_balise_events_location ON balise_events USING GIST(location);
CREATE INDEX idx_containers_location ON containers USING GIST(location);
CREATE INDEX idx_assets_location ON assets USING GIST(location);
```

## 🔧 Database Configuration

### Docker Configuration
```yaml
balise-postgres:
  image: postgis/postgis:14-3.3
  container_name: balise-postgres
  ports:
    - "6063:5432"
  environment:
    - POSTGRES_DB=balisedb
    - POSTGRES_USER=adminbdb
    - POSTGRES_PASSWORD=To7Z2UCeWTsriPxbADX8
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./database/init:/docker-entrypoint-initdb.d
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U adminbdb -d balisedb"]
    interval: 30s
    timeout: 10s
    retries: 5
    start_period: 30s
```

### Initialization Scripts
Located in `database/init/`:
- `01-schema.sql`: Creates tables and indexes
- `02-verify-tables.sql`: Verification queries

## 📊 Common Queries

### Balise Management
```sql
-- Get all active balises
SELECT id, name, imei, status, battery_level, 
       ST_X(location::geometry) as longitude,
       ST_Y(location::geometry) as latitude
FROM balises 
WHERE status = 'ACTIVE';

-- Get balise with recent events
SELECT b.*, COUNT(e.id) as event_count
FROM balises b
LEFT JOIN balise_events e ON b.id = e.balise_id 
    AND e.event_time > NOW() - INTERVAL '24 hours'
GROUP BY b.id;
```

### Event Tracking
```sql
-- Get recent events for a balise
SELECT event_type, event_time, battery_level, speed,
       ST_X(location::geometry) as longitude,
       ST_Y(location::geometry) as latitude
FROM balise_events 
WHERE balise_id = $1 
ORDER BY event_time DESC 
LIMIT 50;

-- Get events within geographic area
SELECT b.name, e.event_type, e.event_time
FROM balise_events e
JOIN balises b ON e.balise_id = b.id
WHERE ST_DWithin(
    e.location,
    ST_MakePoint($longitude, $latitude)::geography,
    $radius_meters
);
```

### Spatial Queries
```sql
-- Find balises near a location
SELECT name, imei, 
       ST_Distance(location::geography, 
                   ST_MakePoint($lng, $lat)::geography) as distance_meters
FROM balises 
WHERE ST_DWithin(location::geography, 
                  ST_MakePoint($lng, $lat)::geography, 
                  $radius_meters)
ORDER BY distance_meters;
```

## 🔒 Security & Permissions

### User Management
```sql
-- Database user with limited permissions
CREATE USER balise_app WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE balisedb TO balise_app;
GRANT USAGE ON SCHEMA public TO balise_app;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO balise_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO balise_app;
```

### Connection Security
- Password-based authentication
- Connection limited to Docker network
- Environment variable configuration
- No direct external access (except via port mapping)

## 📈 Monitoring & Maintenance

### Health Checks
```sql
-- Database connectivity
SELECT 1;

-- Table existence
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'public';

-- PostGIS extension
SELECT postgis_version();
```

### Maintenance Tasks
```sql
-- Update statistics
ANALYZE;

-- Vacuum tables
VACUUM ANALYZE balises;
VACUUM ANALYZE balise_events;

-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

## 🚨 Backup & Recovery

### Backup Strategy
```bash
# Full database backup
docker exec balise-postgres pg_dump -U adminbdb balisedb > backup.sql

# Schema-only backup
docker exec balise-postgres pg_dump -U adminbdb -s balisedb > schema.sql

# Data-only backup
docker exec balise-postgres pg_dump -U adminbdb -a balisedb > data.sql
```

### Recovery Process
```bash
# Restore from backup
docker exec -i balise-postgres psql -U adminbdb balisedb < backup.sql
```

---

The PostgreSQL database with PostGIS provides a robust, scalable foundation for fleet data management with full spatial capabilities for GPS tracking and geographic queries.
