#
# Fleet Monitor - CMA-CGM UAT Debug Test Script
# Enhanced test script with detailed error reporting
#
# Project: Fleet Management System with Maxvision SDK Integration
# Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
# Project Chief: Antoine Diarra
#
# Copyright (c) 2025 Ynnov
#

Write-Host "🔍 CMA-CGM UAT Debug Test Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Configuration
$AUTH_URL = "https://auth-pre.cma-cgm.com/as/token.oauth2"
$API_BASE_URL = "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1"
$CLIENT_ID = "beapp-sinigroup"
$CLIENT_SECRET = "YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ"
$SCOPE = "tracking:write:be"

Write-Host "1️⃣ Getting OAuth2 token..." -ForegroundColor Yellow

# Step 1: Get OAuth2 token
$authBody = @{
    client_id = $CLIENT_ID
    client_secret = $CLIENT_SECRET
    grant_type = "client_credentials"
    scope = $SCOPE
}

try {
    $tokenResponse = Invoke-RestMethod -Uri $AUTH_URL -Method Post -Body $authBody -ContentType "application/x-www-form-urlencoded"
    $accessToken = $tokenResponse.access_token
    Write-Host "✅ Access token obtained: $($accessToken.Substring(0, 20))..." -ForegroundColor Green
    Write-Host "Token expires in: $($tokenResponse.expires_in) seconds" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get access token: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "2️⃣ Sending GPS coordinates (exact CMA-CGM format)..." -ForegroundColor Yellow

# Step 2: Send GPS coordinates - using exact format from CMA-CGM email
$gpsData = @(
    @{
        equipmentReference = "APZU2106333"
        eventCreatedDateTime = "2024-07-20T17:00:00Z"
        originatorName = "SINIGROUP"
        partnerName = "SINI TRANSPORT"
        carrierBookingReference = "LHV3076333"
        modeOfTransport = "TRUCK"
        transportOrder = "TLHV2330333"
        eventLocation = @{
            latitude = 44.56187398172333
            longitude = -0.4188740439713333
        }
    }
)

$headers = @{
    "Authorization" = "Bearer $accessToken"
    "Content-Type" = "application/json"
}

$gpsJson = $gpsData | ConvertTo-Json -Depth 10 -Compress
Write-Host "GPS JSON Payload:" -ForegroundColor Gray
Write-Host $gpsJson -ForegroundColor Gray

try {
    $gpsResponse = Invoke-RestMethod -Uri "$API_BASE_URL/coordinates" -Method Post -Body $gpsJson -Headers $headers
    Write-Host "✅ GPS coordinates sent successfully!" -ForegroundColor Green
    Write-Host "GPS Response: $($gpsResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to send GPS coordinates" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response Body: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "3️⃣ Sending event data (exact CMA-CGM format)..." -ForegroundColor Yellow

# Step 3: Send event data - using exact format from CMA-CGM email
$eventData = @(
    @{
        equipmentReference = "APZU2106333"
        eventCreatedDateTime = "2024-07-20T17:00:00Z"
        originatorName = "SINIGROUP"
        partnerName = "SINI TRANSPORT"
        eventType = "TRANSPORT"
        transportEventTypeCode = "ARRI"
        equipmentEventTypeCode = ""
        eventClassifierCode = "ACT"
        carrierBookingReference = "LHV3076333"
        modeOfTransport = "TRUCK"
        transportationPhase = "IMPORT"
        transportOrder = "TLHV2330333"
        eventLocation = @{
            facilityTypeCode = "CLOC"
            locationCode = "CONSIGNEE_LOC_CODE"
            locationUnLocode = ""
            locationName = "CONSIGNEE_LOC_NAME"
            latitude = 43.31501044666666
            longitude = 6.36580111853943
            address = @{
                name = "CONSIGNEE_LOC_NAME_ADD"
                street = "piste de la Serre"
                streetNumber = ""
                floor = ""
                postCode = "83340"
                city = "LES MAYONS"
                stateRegion = ""
                country = "FRANCE"
            }
        }
    }
)

$eventJson = $eventData | ConvertTo-Json -Depth 10 -Compress
Write-Host "Event JSON Payload:" -ForegroundColor Gray
Write-Host $eventJson -ForegroundColor Gray

try {
    $eventResponse = Invoke-RestMethod -Uri "$API_BASE_URL/events" -Method Post -Body $eventJson -Headers $headers
    Write-Host "✅ Event data sent successfully!" -ForegroundColor Green
    Write-Host "Event Response: $($eventResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to send event data" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response Body: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "4️⃣ Testing minimal GPS payload..." -ForegroundColor Yellow

# Step 4: Test with minimal required fields only
$minimalGpsData = @(
    @{
        equipmentReference = "APZU2106333"
        eventCreatedDateTime = "2024-07-20T17:00:00Z"
        originatorName = "SINIGROUP"
        eventLocation = @{
            latitude = 44.56187398172333
            longitude = -0.4188740439713333
        }
    }
)

$minimalGpsJson = $minimalGpsData | ConvertTo-Json -Depth 10 -Compress
Write-Host "Minimal GPS JSON Payload:" -ForegroundColor Gray
Write-Host $minimalGpsJson -ForegroundColor Gray

try {
    $minimalGpsResponse = Invoke-RestMethod -Uri "$API_BASE_URL/coordinates" -Method Post -Body $minimalGpsJson -Headers $headers
    Write-Host "✅ Minimal GPS coordinates sent successfully!" -ForegroundColor Green
    Write-Host "Response: $($minimalGpsResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to send minimal GPS coordinates" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response Body: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "✅ CMA-CGM UAT Debug Test completed!" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
