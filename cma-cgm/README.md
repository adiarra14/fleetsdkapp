# CMA-CGM Integration - DK Balise Platform

<!--
/**
 * Fleet Monitor - CMA-CGM Integration Module
 * DK Balise Platform Data Integration
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
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

## 🚢 Overview

This module provides integration with CMA-CGM's Eagle Platform API for DK balise customers. It enables our Fleet Monitor system to push tracking data (coordinates and events) to CMA-CGM's UAT environment using OAuth2 authentication.

## 🔐 Authentication

### OAuth2 Client Credentials Flow

**Authentication Endpoint**: `https://auth-pre.cma-cgm.com/as/token.oauth2`

**Credentials**:
- **Client ID**: `beapp-sinigroup`
- **Client Secret**: `YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ`
- **Grant Type**: `client_credentials`
- **Scope**: `tracking:write:be`

**Token Expiration**: 5 minutes

## 🌐 API Endpoints

### Base URL
`https://apis-uat.cma-cgm.net/technical/generic/eagle/v1`

### Available Endpoints

#### 1. Coordinates Endpoint
- **URL**: `/coordinates`
- **Method**: `POST`
- **Purpose**: Push container tracking coordinates
- **Content-Type**: `application/json`
- **Authorization**: `Bearer <JWT_TOKEN>`

#### 2. Events Endpoint
- **URL**: `/events`
- **Method**: `POST`
- **Purpose**: Push container events
- **Content-Type**: `application/json`
- **Authorization**: `Bearer <JWT_TOKEN>`

## 📊 Data Models

### Coordinate Object
```json
{
  "equipmentReference": "CGMU5417495",
  "eventCreatedDateTime": "2024-05-22T12:27:16Z",
  "originatorName": "SINIGROUP",
  "partnerName": "SINIGROUP",
  "carrierBookingReference": "LHV2536188",
  "modeOfTransport": "TRUCK",
  "transportOrder": "AAAA112233",
  "eventLocation": {
    "latitude": 0.126487,
    "longitude": 2.315618
  },
  "fuelType": "FUEL",
  "truckMileage": 80000,
  "truckMileageUnit": "KM",
  "reeferTemperature": 2,
  "reeferTemperatureUnit": "CEL"
}
```

### Event Object
```json
{
  "equipmentReference": "CGMU5417495",
  "eventCreatedDateTime": "2024-05-22T12:27:16Z",
  "originatorName": "SINIGROUP",
  "partnerName": "SINIGROUP",
  "eventType": "EQUIPMENT",
  "eventClassifierCode": "ACT",
  "equipmentEventTypeCode": "LOAD",
  "eventLocation": {
    "facilityTypeCode": "DEPO",
    "locationCode": "FRFOSD22",
    "locationName": "CARTAGENA CONTECAR",
    "latitude": 50.51281,
    "longitude": 0.256815,
    "address": {
      "name": "PORT OF BRISBANE",
      "street": "BINGERA DRIVE",
      "streetNumber": "14",
      "postCode": "4178",
      "city": "BRISBANE",
      "country": "FRANCE"
    }
  }
}
```

## 🔧 Integration Components

### 1. Authentication Service
- OAuth2 token management
- Automatic token refresh
- Secure credential storage

### 2. API Client
- HTTP client for CMA-CGM endpoints
- Request/response handling
- Error management and retry logic

### 3. Data Transformation
- Convert balise data to CMA-CGM format
- Coordinate mapping
- Event type mapping

### 4. Configuration
- Environment-specific settings
- API endpoint configuration
- Authentication parameters

## 📁 File Structure

```
cma-cgm/
├── README.md                                    # This documentation
├── technical.generic.eagle.ingestion.v1.srv.yaml  # CMA-CGM API Swagger specification
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── maxvision/
│   │   │           └── cmacgm/
│   │   │               ├── auth/
│   │   │               │   ├── OAuth2Service.java
│   │   │               │   └── TokenManager.java
│   │   │               ├── client/
│   │   │               │   ├── CmaCgmApiClient.java
│   │   │               │   └── ApiResponse.java
│   │   │               ├── model/
│   │   │               │   ├── Coordinate.java
│   │   │               │   ├── Event.java
│   │   │               │   ├── EventLocation.java
│   │   │               │   └── Location.java
│   │   │               ├── service/
│   │   │               │   ├── CmaCgmIntegrationService.java
│   │   │               │   └── DataTransformationService.java
│   │   │               └── config/
│   │   │                   └── CmaCgmConfig.java
│   │   └── resources/
│   │       ├── application-cmacgm.yml
│   │       └── logback-cmacgm.xml
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
└── docs/
    ├── api-integration.md
    ├── authentication.md
    └── troubleshooting.md
```

## 🚀 Quick Start

### 1. Authentication Test
```bash
curl -X POST https://auth-pre.cma-cgm.com/as/token.oauth2 \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=beapp-sinigroup&client_secret=YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ&grant_type=client_credentials&scope=tracking:write:be"
```

### 2. Send Coordinates
```bash
curl -X POST https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/coordinates \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '[{"equipmentReference":"CGMU5417495","eventCreatedDateTime":"2024-05-22T12:27:16Z","originatorName":"SINIGROUP","eventLocation":{"latitude":0.126487,"longitude":2.315618}}]'
```

### 3. Send Events
```bash
curl -X POST https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/events \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '[{"equipmentReference":"CGMU5417495","eventCreatedDateTime":"2024-05-22T12:27:16Z","originatorName":"SINIGROUP","eventType":"EQUIPMENT","eventClassifierCode":"ACT","equipmentEventTypeCode":"LOAD","eventLocation":{"facilityTypeCode":"DEPO","locationCode":"FRFOSD22","latitude":50.51281,"longitude":0.256815}}]'
```

## 🔍 Environment Configuration

### UAT Environment
- **Auth URL**: `https://auth-pre.cma-cgm.com/as/token.oauth2`
- **API Base URL**: `https://apis-uat.cma-cgm.net/technical/generic/eagle/v1`
- **Client ID**: `beapp-sinigroup`
- **Scope**: `tracking:write:be`

### Production Environment
- **Auth URL**: `https://auth.cma-cgm.com/as/token.oauth2` (TBD)
- **API Base URL**: `https://apis.cma-cgm.net/technical/generic/eagle/v1` (TBD)
- **Client ID**: `beapp-sinigroup` (TBD)
- **Scope**: `tracking:write:be` (TBD)

## 📋 Integration Checklist

- [ ] Set up OAuth2 authentication service
- [ ] Implement API client with retry logic
- [ ] Create data transformation services
- [ ] Add configuration management
- [ ] Implement error handling and logging
- [ ] Create unit and integration tests
- [ ] Set up monitoring and alerting
- [ ] Document API usage and troubleshooting
- [ ] Validate with CMA-CGM UAT environment
- [ ] Prepare for production deployment

## 🔒 Security Considerations

1. **Credential Management**: Store client secret securely using environment variables or secret management systems
2. **Token Security**: Implement secure token storage and automatic refresh
3. **Data Privacy**: Ensure compliance with data protection regulations
4. **Audit Logging**: Log all API calls for audit and troubleshooting
5. **Rate Limiting**: Implement proper rate limiting to avoid API throttling

## 📞 Support

For technical support and integration questions:
- **Project Chief**: Antoine Diarra
- **Client**: SiniTechnologie / Sinigroupe
- **CMA-CGM Contact**: (To be provided)

---

This integration module enables seamless data flow from our Fleet Monitor system to CMA-CGM's Eagle Platform, providing real-time container tracking capabilities for DK balise customers.
