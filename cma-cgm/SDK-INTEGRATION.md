# SDK to CMA-CGM Integration Guide

*Fleet Management System with Maxvision SDK Integration*  
*Project: Fleet Management System with CMA-CGM Integration*  
*Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)*  
*Project Chief: Antoine Diarra*  
*Copyright (c) 2025 Ynnov*

## 🔄 **How the Integration Works**

This document explains exactly how your existing SDK balise data flows automatically to CMA-CGM's Eagle Platform.

### 📊 **Data Flow Overview**

```
Balise Devices → TCP Server (SDK) → PostgreSQL Database → CMA-CGM Sync Service → CMA-CGM API
```

### 🗃️ **Database Tables Used**

#### 1. **balises** table
- Stores balise device information (IMEI, name, container_id)
- Links balises to containers for equipment tracking

#### 2. **balise_events** table  
- Stores all GPS coordinates and events from balises
- Contains location data (PostGIS POINT format)
- Event types: ARRIVAL, DEPARTURE, LOADING, UNLOADING

#### 3. **containers** table
- Container information linked to balises
- Used as equipment reference for CMA-CGM

#### 4. **cma_cgm_sync_log** table (auto-created)
- Tracks which events have been sent to CMA-CGM
- Prevents duplicate data transmission

## 🚀 **Automatic Synchronization**

### ⏰ **Scheduled Sync (Every 5 Minutes)**

The `BaliseDataSyncService` automatically:

1. **Queries new GPS data** from `balise_events` table
2. **Queries transport events** (ARRIVAL, DEPARTURE, etc.)
3. **Transforms data** to CMA-CGM JSON format
4. **Sends to CMA-CGM API** (coordinates and events)
5. **Marks as synced** to avoid duplicates

### 📍 **GPS Coordinates Sync**

**Database Query:**
```sql
SELECT be.id, be.balise_id, be.event_time, be.location,
       b.imei, b.name as balise_name, b.container_id,
       c.name as container_name
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
LEFT JOIN containers c ON b.container_id = c.id
WHERE be.location IS NOT NULL 
  AND be.event_time > NOW() - INTERVAL '1 hour'
  AND NOT EXISTS (SELECT 1 FROM cma_cgm_sync_log WHERE event_id = be.id AND sync_type = 'GPS')
```

**CMA-CGM JSON Format:**
```json
[{
  "equipmentReference": "CONTAINER_NAME_OR_IMEI",
  "eventCreatedDateTime": "2024-07-20T17:00:00Z",
  "originatorName": "SINIGROUP",
  "partnerName": "SINI TRANSPORT",
  "carrierBookingReference": "CONTAINER_NAME",
  "modeOfTransport": "TRUCK",
  "transportOrder": "TO_CONTAINER_NAME",
  "eventLocation": {
    "latitude": -12.6392,
    "longitude": -8.0029
  }
}]
```

### 🚛 **Transport Events Sync**

**Database Query:**
```sql
SELECT be.id, be.balise_id, be.event_type, be.event_time, be.location,
       b.imei, b.name as balise_name, b.container_id,
       c.name as container_name
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
LEFT JOIN containers c ON b.container_id = c.id
WHERE be.event_type IN ('ARRIVAL', 'DEPARTURE', 'LOADING', 'UNLOADING')
  AND NOT EXISTS (SELECT 1 FROM cma_cgm_sync_log WHERE event_id = be.id AND sync_type = 'EVENT')
```

**Event Type Mapping:**
- `ARRIVAL` → `ARRI` (CMA-CGM code)
- `DEPARTURE` → `DEPA` (CMA-CGM code)
- `LOADING` → `LOAD` (CMA-CGM code)
- `UNLOADING` → `DISC` (CMA-CGM code)

**CMA-CGM JSON Format:**
```json
[{
  "equipmentReference": "CONTAINER_NAME_OR_IMEI",
  "eventCreatedDateTime": "2024-07-20T17:00:00Z",
  "originatorName": "SINIGROUP",
  "partnerName": "SINI TRANSPORT",
  "eventType": "TRANSPORT",
  "transportEventTypeCode": "ARRI",
  "equipmentEventTypeCode": "",
  "eventClassifierCode": "ACT",
  "carrierBookingReference": "CONTAINER_NAME",
  "modeOfTransport": "TRUCK",
  "transportationPhase": "IMPORT",
  "transportOrder": "TO_CONTAINER_NAME",
  "eventLocation": {
    "facilityTypeCode": "CLOC",
    "locationCode": "BAMAKO_DEPOT_01",
    "locationUnLocode": "",
    "locationName": "Bamako Central Depot",
    "latitude": -12.6392,
    "longitude": -8.0029,
    "address": {
      "name": "Bamako Central Depot",
      "street": "Avenue de la Nation",
      "streetNumber": "123",
      "floor": "",
      "postCode": "",
      "city": "BAMAKO",
      "stateRegion": "Bamako District",
      "country": "MALI"
    }
  }
}]
```

## 🎛️ **Manual Control via API**

### 📡 **REST API Endpoints**

#### Trigger Manual Sync
```http
POST /api/cmacgm/sync
```
**Response:**
```json
{
  "status": "success",
  "message": "Synchronization triggered successfully",
  "timestamp": 1642694400000
}
```

#### Get Sync Statistics
```http
GET /api/cmacgm/sync/stats
```
**Response:**
```json
{
  "totalEventsLast24h": 150,
  "syncedGpsLast24h": 120,
  "syncedEventsLast24h": 30,
  "activeBalises": 5,
  "timestamp": 1642694400000
}
```

#### View Recent Data
```http
GET /api/cmacgm/data/recent
```
**Response:**
```json
{
  "gpsData": [
    {
      "id": 123,
      "event_time": "2024-07-20T17:00:00Z",
      "location": "POINT(-8.0029 -12.6392)",
      "imei": "123456789012345",
      "balise_name": "Balise_001",
      "container_name": "CONTAINER_001",
      "synced": true
    }
  ],
  "eventData": [
    {
      "id": 124,
      "event_type": "ARRIVAL",
      "event_time": "2024-07-20T17:05:00Z",
      "location": "POINT(-8.0029 -12.6392)",
      "imei": "123456789012345",
      "balise_name": "Balise_001",
      "container_name": "CONTAINER_001",
      "synced": true
    }
  ]
}
```

#### Check Integration Health
```http
GET /api/cmacgm/health
```
**Response:**
```json
{
  "database": "healthy",
  "baliseCount": 10,
  "recentEvents": 25,
  "status": "healthy",
  "timestamp": 1642694400000
}
```

## 🔧 **Configuration**

### Environment Variables
```properties
# CMA-CGM API Configuration
CMACGM_AUTH_URL=https://auth-pre.cma-cgm.com/as/token.oauth2
CMACGM_API_BASE_URL=https://apis-uat.cma-cgm.net/technical/generic/eagle/v1
CMACGM_CLIENT_ID=beapp-sinigroup
CMACGM_CLIENT_SECRET=YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ
CMACGM_SCOPE=tracking:write:be

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/balisedb
SPRING_DATASOURCE_USERNAME=adminbdb
SPRING_DATASOURCE_PASSWORD=your_password
```

### Sync Configuration
- **Sync Interval**: Every 5 minutes (300,000 ms)
- **Data Window**: Last 1 hour of events
- **Batch Size**: Maximum 50 records per sync
- **Location**: Bamako, Mali coordinates and addresses

## 🚦 **Monitoring and Troubleshooting**

### ✅ **Success Indicators**
- HTTP 201 responses from CMA-CGM API
- Increasing sync counts in statistics
- Recent events marked as `synced: true`

### ❌ **Error Handling**
- Failed API calls are logged with full error details
- Database connectivity issues are caught and logged
- Individual record failures don't stop batch processing

### 📊 **Monitoring Dashboard**
Access the web UI at `http://localhost:6061` to view:
- Real-time balise status
- Recent GPS coordinates
- Transport events
- CMA-CGM sync statistics

## 🎯 **Production Deployment**

### 1. **Database Setup**
Ensure your PostgreSQL database has:
- PostGIS extension enabled
- All required tables (balises, balise_events, containers)
- Proper indexes on event_time and location columns

### 2. **Service Configuration**
- Set production CMA-CGM credentials
- Configure sync interval as needed
- Set up proper logging and monitoring

### 3. **Testing**
1. Add test balise data to database
2. Trigger manual sync via API
3. Verify data appears in CMA-CGM system
4. Monitor automatic sync every 5 minutes

## 📋 **Example Data Flow**

### Step 1: Balise sends GPS data
```
Balise IMEI 123456789012345 → TCP Server → Database
```

### Step 2: Database stores event
```sql
INSERT INTO balise_events (balise_id, event_type, event_time, location, ...)
VALUES (1, 'GPS_UPDATE', NOW(), ST_Point(-8.0029, -12.6392), ...)
```

### Step 3: Sync service processes data
```
BaliseDataSyncService (every 5 min) → Query new events → Transform to CMA-CGM format
```

### Step 4: Send to CMA-CGM
```
HTTP POST to CMA-CGM API → Receive HTTP 201 → Mark as synced
```

### Step 5: Avoid duplicates
```sql
INSERT INTO cma_cgm_sync_log (event_id, sync_type, sync_time)
VALUES (123, 'GPS', NOW())
```

## 🎉 **Ready for Production!**

Your SDK integration with CMA-CGM is now **production-ready** and will automatically:

✅ **Sync GPS coordinates** from all active balises  
✅ **Send transport events** (arrivals, departures)  
✅ **Use correct Bamako, Mali** location data  
✅ **Prevent duplicate** data transmission  
✅ **Provide monitoring** via REST API  
✅ **Handle errors** gracefully with logging  

The integration runs automatically every 5 minutes and can be monitored/controlled via the web dashboard and REST API endpoints.
