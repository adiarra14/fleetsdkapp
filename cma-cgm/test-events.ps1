# CMA-CGM Event Data Test Script
Write-Host "CMA-CGM Event Data Test" -ForegroundColor Green

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

# Test Event data - exact format from CMA-CGM email
$eventData = '[{"equipmentReference":"APZU2106333","eventCreatedDateTime":"2024-07-20T17:00:00Z","originatorName":"SINIGROUP","partnerName":"SINI TRANSPORT","eventType":"TRANSPORT","transportEventTypeCode":"ARRI","equipmentEventTypeCode":"","eventClassifierCode":"ACT","carrierBookingReference":"LHV3076333","modeOfTransport":"TRUCK","transportationPhase":"IMPORT","transportOrder":"TLHV2330333","eventLocation":{"facilityTypeCode":"CLOC","locationCode":"CONSIGNEE_LOC_CODE","locationUnLocode":"","locationName":"CONSIGNEE_LOC_NAME","latitude":43.31501044666666,"longitude":6.36580111853943,"address":{"name":"CONSIGNEE_LOC_NAME_ADD","street":"piste de la Serre","streetNumber":"","floor":"","postCode":"83340","city":"LES MAYONS","stateRegion":"","country":"FRANCE"}}}]'

$headers = @{
    "Authorization" = "Bearer $accessToken"
    "Content-Type" = "application/json"
}

Write-Host "Sending Event data..." -ForegroundColor Yellow
Write-Host $eventData -ForegroundColor Gray

try {
    $eventResponse = Invoke-WebRequest -Uri "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1/events" -Method Post -Body $eventData -Headers $headers
    Write-Host "Event Success: $($eventResponse.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Event Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $errorResponse = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($errorResponse)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error details: $errorBody" -ForegroundColor Red
    }
}

Write-Host "Event test completed" -ForegroundColor Green
