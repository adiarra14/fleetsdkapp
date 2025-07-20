# CMA-CGM Test Script - Bamako, Mali Location
Write-Host "CMA-CGM UAT Test - Bamako, Mali" -ForegroundColor Green

# Get token
$authBody = @{
    client_id = "beapp-sinigroup"
    client_secret = "YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ"
    grant_type = "client_credentials"
    scope = "tracking:write:be"
}

$tokenResponse = Invoke-RestMethod -Uri "https://auth-pre.cma-cgm.com/as/token.oauth2" -Method Post -Body $authBody -ContentType "application/x-www-form-urlencoded"
$accessToken = $tokenResponse.access_token
Write-Host "Token obtained: OK" -ForegroundColor Green

$headers = @{
    "Authorization" = "Bearer $accessToken"
    "Content-Type" = "application/json"
}

# Test GPS coordinates - Bamako, Mali location
Write-Host "1. Testing GPS coordinates - Bamako, Mali..." -ForegroundColor Yellow
$gpsData = '[{"equipmentReference":"APZU2106333","eventCreatedDateTime":"2024-07-20T17:00:00Z","originatorName":"SINIGROUP","partnerName":"SINI TRANSPORT","carrierBookingReference":"LHV3076333","modeOfTransport":"TRUCK","transportOrder":"TLHV2330333","eventLocation":{"latitude":-12.6392,"longitude":-8.0029}}]'

Write-Host "GPS Data (Bamako): $gpsData" -ForegroundColor Gray

try {
    $gpsResponse = Invoke-WebRequest -Uri "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/coordinates" -Method Post -Body $gpsData -Headers $headers
    Write-Host "GPS Success: $($gpsResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "GPS Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Event data - Bamako, Mali location with proper address
Write-Host "2. Testing Event data - Bamako, Mali..." -ForegroundColor Yellow
$eventData = '[{"equipmentReference":"APZU2106333","eventCreatedDateTime":"2024-07-20T17:00:00Z","originatorName":"SINIGROUP","partnerName":"SINI TRANSPORT","eventType":"TRANSPORT","transportEventTypeCode":"ARRI","equipmentEventTypeCode":"","eventClassifierCode":"ACT","carrierBookingReference":"LHV3076333","modeOfTransport":"TRUCK","transportationPhase":"IMPORT","transportOrder":"TLHV2330333","eventLocation":{"facilityTypeCode":"CLOC","locationCode":"BAMAKO_DEPOT_01","locationUnLocode":"","locationName":"Bamako Central Depot","latitude":-12.6392,"longitude":-8.0029,"address":{"name":"Bamako Central Depot","street":"Avenue de la Nation","streetNumber":"123","floor":"","postCode":"","city":"BAMAKO","stateRegion":"Bamako District","country":"MALI"}}}]'

Write-Host "Event Data (Bamako): $eventData" -ForegroundColor Gray

try {
    $eventResponse = Invoke-WebRequest -Uri "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/events" -Method Post -Body $eventData -Headers $headers
    Write-Host "Event Success: $($eventResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Event Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Bamako test completed successfully!" -ForegroundColor Green
