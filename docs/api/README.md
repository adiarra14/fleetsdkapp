# API Documentation

## 🔌 Fleet Monitor REST API

The Fleet Monitor system provides a comprehensive REST API for managing balises, retrieving data, and monitoring system status.

## 📋 API Overview

### Base URL
```
http://your-host:6062/api
```

### Authentication
Currently, no authentication is required. CORS is enabled for cross-origin requests.

### Content Type
All API endpoints accept and return JSON data unless otherwise specified.

## 🚀 Endpoints Reference

### System Endpoints

#### Health Check
```http
GET /health
```

**Description**: Check if the backend service is running.

**Response**: Plain text
```
Fleet SDK Backend Service is running - 2025-07-20T14:15:30
```

#### Service Information
```http
GET /
```

**Description**: Get basic service information.

**Response**: Plain text
```
Fleet SDK Backend Service - Ready and Operational
```

#### System Status
```http
GET /api/status
```

**Description**: Get comprehensive system status and statistics.

**Response**: JSON
```json
{
  "status": "success",
  "databaseConnected": true,
  "timestamp": "2025-07-20T14:15:30",
  "baliseStatusCounts": {
    "ACTIVE": 5,
    "INACTIVE": 2,
    "MAINTENANCE": 1
  },
  "recentEvents": 23
}
```

### Balise Management Endpoints

#### List All Balises
```http
GET /api/balises
```

**Description**: Retrieve all balises with their current status and statistics.

**Response**: JSON
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
      "lastSeen": "2025-07-20T14:10:00",
      "createdAt": "2025-07-15T09:00:00",
      "longitude": 2.3522,
      "latitude": 48.8566,
      "eventCount": 15
    }
  ],
  "count": 1,
  "timestamp": "2025-07-20T14:15:30"
}
```

#### Get Specific Balise
```http
GET /api/balises/{id}
```

**Parameters**:
- `id` (path): Balise ID (integer)

**Response**: JSON
```json
{
  "id": 1,
  "name": "Fleet-001",
  "imei": "123456789012345",
  "type": "GPS_TRACKER",
  "status": "ACTIVE",
  "batteryLevel": 85.5,
  "lastSeen": "2025-07-20T14:10:00",
  "createdAt": "2025-07-15T09:00:00",
  "longitude": 2.3522,
  "latitude": 48.8566,
  "status": "success"
}
```

**Error Response** (404):
```json
{
  "error": "Balise not found",
  "status": "not_found"
}
```

#### Get Balise Events
```http
GET /api/balises/{id}/events
```

**Parameters**:
- `id` (path): Balise ID (integer)

**Description**: Get the last 50 events for a specific balise.

**Response**: JSON
```json
{
  "events": [
    {
      "id": 101,
      "eventType": "GPS_UPDATE",
      "eventTime": "2025-07-20T14:10:00",
      "batteryLevel": 85.5,
      "speed": 45.2,
      "heading": 180.0,
      "longitude": 2.3522,
      "latitude": 48.8566,
      "messageRaw": "GPS:48.8566,2.3522,45.2,180.0",
      "payload": "{\"type\":\"gps\",\"coordinates\":[2.3522,48.8566]}"
    }
  ],
  "count": 1,
  "baliseId": 1,
  "status": "success"
}
```

#### Send Command to Balise
```http
POST /api/balises/{id}/command
Content-Type: application/json
```

**Parameters**:
- `id` (path): Balise ID (integer)

**Request Body**:
```json
{
  "command": "LOCK",
  "parameters": {
    "timeout": 30,
    "force": false
  }
}
```

**Available Commands**:
- `LOCK`: Lock the device
- `UNLOCK`: Unlock the device
- `LOCATE`: Request GPS position
- `RESET`: Reset device
- `SET_INTERVAL`: Set reporting interval

**Response**: JSON
```json
{
  "baliseId": 1,
  "command": "LOCK",
  "status": "queued",
  "message": "Command queued for processing",
  "timestamp": "2025-07-20T14:15:30"
}
```

## 📊 Data Models

### Balise Object
```typescript
interface Balise {
  id: number;                    // Unique identifier
  name: string;                  // Human-readable name
  imei: string;                  // Device IMEI (15 digits)
  type: string;                  // Device type
  status: string;                // Current status
  batteryLevel: number;          // Battery percentage (0-100)
  lastSeen: string;              // ISO timestamp
  createdAt: string;             // ISO timestamp
  longitude: number;             // GPS longitude
  latitude: number;              // GPS latitude
  eventCount?: number;           // Recent events count (24h)
}
```

### Event Object
```typescript
interface BaliseEvent {
  id: number;                    // Event ID
  eventType: string;             // Event type
  eventTime: string;             // ISO timestamp
  batteryLevel: number;          // Battery at event time
  speed: number;                 // Speed in km/h
  heading: number;               // Direction in degrees
  longitude: number;             // GPS longitude
  latitude: number;              // GPS latitude
  messageRaw: string;            // Raw device message
  payload: string;               // JSON payload
}
```

### Command Request
```typescript
interface CommandRequest {
  command: string;               // Command type
  parameters?: {                 // Optional parameters
    [key: string]: any;
  };
}
```

### API Response
```typescript
interface ApiResponse<T> {
  status: "success" | "error";   // Response status
  data?: T;                      // Response data
  error?: string;                // Error message
  timestamp: string;             // Response timestamp
}
```

## 🔧 Error Handling

### HTTP Status Codes
- `200 OK`: Successful request
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error
- `503 Service Unavailable`: Database unavailable

### Error Response Format
```json
{
  "status": "error",
  "error": "Detailed error message",
  "timestamp": "2025-07-20T14:15:30"
}
```

### Common Error Messages
- `"Database connection failed"`: PostgreSQL unavailable
- `"Balise not found"`: Invalid balise ID
- `"Invalid request format"`: Malformed JSON
- `"Command not supported"`: Unknown command type

## 🌐 CORS Configuration

The API supports cross-origin requests with the following headers:
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, OPTIONS`
- `Access-Control-Allow-Headers: Content-Type, Authorization`

## 📈 Rate Limiting

Currently, no rate limiting is implemented. Consider implementing rate limiting for production use.

## 🔍 API Testing

### Using cURL

#### Get System Status
```bash
curl -X GET http://localhost:6062/api/status
```

#### Get All Balises
```bash
curl -X GET http://localhost:6062/api/balises
```

#### Send Command
```bash
curl -X POST http://localhost:6062/api/balises/1/command \
  -H "Content-Type: application/json" \
  -d '{"command": "LOCATE"}'
```

### Using JavaScript (Fetch API)
```javascript
// Get system status
const response = await fetch('/api/status');
const status = await response.json();

// Send command to balise
const commandResponse = await fetch('/api/balises/1/command', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    command: 'LOCK',
    parameters: { timeout: 30 }
  })
});
```

## 📚 Integration Examples

### Dashboard Integration
The Fleet Monitor web UI uses these endpoints:
- `/api/status` for dashboard statistics
- `/api/balises` for the balise table
- `/api/balises/{id}/events` for event history

### Mobile App Integration
Mobile applications can use the API for:
- Real-time balise monitoring
- Command sending
- Event notifications
- GPS tracking

### Third-party Integration
External systems can integrate using:
- Webhook notifications (future feature)
- Bulk data export (future feature)
- Real-time WebSocket updates (future feature)

---

The REST API provides comprehensive access to all fleet management functionality with a clean, consistent interface suitable for web, mobile, and third-party integrations.
