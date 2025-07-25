# Maxvision Lock SDK Demo Application

This is a Spring Boot application demonstrating the integration with Maxvision Intelligent Lock SDK.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Maxvision Lock SDK JAR file

## Setup

1. Place the Maxvision SDK JAR file in the `lib/` directory:
```bash
mkdir lib
cp /path/to/maxvision-edge-protocol-gateway-service.jar lib/
```

2. Build the application:
```bash
mvn clean package
```

3. Run the application:
```bash
java -jar target/lock-sdk-demo-1.0.0-SNAPSHOT.jar
```

## Configuration

The application uses the following configuration in `application.yml`:

```yaml
mvgateway:
  servers:
    - port: 8910
      encrypt: true
      name: maxvision
```

## API Endpoints

### 1. Authorize Seal Card
```http
POST /api/lock/auth-seal-card?lockCode={lockCode}&cardNo={cardNo}
```

### 2. Set GPS Interval
```http
POST /api/lock/gps-interval?lockCode={lockCode}&intervalSeconds={intervalSeconds}
```

### 3. Configure SMS VIP Settings
```http
POST /api/lock/sms-vip?lockCode={lockCode}
Content-Type: application/json

[
  {
    "index": 1,
    "phone": "1234567890",
    "pushAlarm": true
  }
]
```

### 4. Change Device Mode
```http
POST /api/lock/device-mode?lockCode={lockCode}&mode={0|1}
```

## Error Codes

| Code  | Description |
|-------|-------------|
| 1001  | Device not connected |
| 1002  | commandLogId is invalid |
| 1003  | lockCode format error |
| 1004  | Invalid commandType |
| 2001  | Invalid subCmdType |
| 2002  | Invalid cardType |
| 2003  | Invalid cardNo |
| 3001  | Invalid gpsInterval |
| 4001  | Empty smsVipList |
| 4002  | Invalid slot index |
| 4003  | Invalid VIP phone number |
| 5001-5006 | IP/APN/account/password/domain errors |
| 6001  | cmdType inconsistent |
| 7001  | Invalid deviceMode (should be 0 or 1) |

## Logging

Logs are written to:
- Console
- `logs/lock-sdk.log`
- Daily rolling files: `logs/lock-sdk-{date}.log`

## Support

For any issues or questions, please refer to the Maxvision Intelligent Lock SDK Developer Reference Guide. 