# Simple CMA-CGM Test Script
Write-Host "CMA-CGM UAT Test" -ForegroundColor Green

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

# Test GPS coordinates
$gpsData = '[{"equipmentReference":"APZU2106333","eventCreatedDateTime":"2024-07-20T17:00:00Z","originatorName":"SINIGROUP","partnerName":"SINI TRANSPORT","carrierBookingReference":"LHV3076333","modeOfTransport":"TRUCK","transportOrder":"TLHV2330333","eventLocation":{"latitude":44.56187398172333,"longitude":-0.4188740439713333}}]'

$headers = @{
    "Authorization" = "Bearer $accessToken"
    "Content-Type" = "application/json"
}

Write-Host "Sending GPS data..." -ForegroundColor Yellow
Write-Host $gpsData -ForegroundColor Gray

try {
    $gpsResponse = Invoke-WebRequest -Uri "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/coordinates" -Method Post -Body $gpsData -Headers $headers
    Write-Host "GPS Success: $($gpsResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "GPS Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
    }
}

Write-Host "Test completed" -ForegroundColor Green
