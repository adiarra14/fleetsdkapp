#!/bin/bash
#
# Fleet Monitor - CMA-CGM UAT Test Script
# Quick test script for CMA-CGM API integration
#
# Project: Fleet Management System with Maxvision SDK Integration
# Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
# Project Chief: Antoine Diarra
#
# Copyright (c) 2025 Ynnov
#

echo "🚀 CMA-CGM UAT Test Script"
echo "=========================="

# Configuration
AUTH_URL="https://auth-pre.cma-cgm.com/as/token.oauth2"
API_BASE_URL="https://apis-uat.cma-cgm.net/technical/generic/eagle/v1"
CLIENT_ID="beapp-sinigroup"
CLIENT_SECRET="YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ"
SCOPE="tracking:write:be"

echo "1️⃣ Getting OAuth2 token..."

# Step 1: Get OAuth2 token
TOKEN_RESPONSE=$(curl -s -X POST "$AUTH_URL" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&grant_type=client_credentials&scope=$SCOPE")

echo "Token response: $TOKEN_RESPONSE"

# Extract access token
ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get access token"
    exit 1
fi

echo "✅ Access token obtained: ${ACCESS_TOKEN:0:20}..."

echo ""
echo "2️⃣ Sending GPS coordinates..."

# Step 2: Send GPS coordinates
GPS_DATA='[
    {
        "equipmentReference": "APZU2106333",
        "eventCreatedDateTime": "2024-07-20T17:00:00Z",
        "originatorName": "SINIGROUP",
        "partnerName": "SINI TRANSPORT",
        "carrierBookingReference": "LHV3076333",
        "modeOfTransport": "TRUCK",
        "transportOrder": "TLHV2330333",
        "eventLocation": {
            "latitude": 44.56187398172333,
            "longitude": -0.4188740439713333
        }
    }
]'

GPS_RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$API_BASE_URL/coordinates" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$GPS_DATA")

GPS_HTTP_STATUS=$(echo "$GPS_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
GPS_BODY=$(echo "$GPS_RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

echo "GPS Response Status: $GPS_HTTP_STATUS"
echo "GPS Response Body: $GPS_BODY"

if [ "$GPS_HTTP_STATUS" = "201" ]; then
    echo "✅ GPS coordinates sent successfully!"
else
    echo "❌ Failed to send GPS coordinates"
fi

echo ""
echo "3️⃣ Sending event data..."

# Step 3: Send event data
EVENT_DATA='[
    {
        "equipmentReference": "APZU2106333",
        "eventCreatedDateTime": "2024-07-20T17:00:00Z",
        "originatorName": "SINIGROUP",
        "partnerName": "SINI TRANSPORT",
        "eventType": "TRANSPORT",
        "transportEventTypeCode": "ARRI",
        "equipmentEventTypeCode": "",
        "eventClassifierCode": "ACT",
        "carrierBookingReference": "LHV3076333",
        "modeOfTransport": "TRUCK",
        "transportationPhase": "IMPORT",
        "transportOrder": "TLHV2330333",
        "eventLocation": {
            "facilityTypeCode": "CLOC",
            "locationCode": "CONSIGNEE_LOC_CODE",
            "locationUnLocode": "",
            "locationName": "CONSIGNEE_LOC_NAME",
            "latitude": 43.31501044666666,
            "longitude": 6.36580111853943,
            "address": {
                "name": "CONSIGNEE_LOC_NAME_ADD",
                "street": "piste de la Serre",
                "streetNumber": "",
                "floor": "",
                "postCode": "83340",
                "city": "LES MAYONS",
                "stateRegion": "",
                "country": "FRANCE"
            }
        }
    }
]'

EVENT_RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$API_BASE_URL/events" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$EVENT_DATA")

EVENT_HTTP_STATUS=$(echo "$EVENT_RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
EVENT_BODY=$(echo "$EVENT_RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

echo "Event Response Status: $EVENT_HTTP_STATUS"
echo "Event Response Body: $EVENT_BODY"

if [ "$EVENT_HTTP_STATUS" = "201" ]; then
    echo "✅ Event data sent successfully!"
else
    echo "❌ Failed to send event data"
fi

echo ""
echo "✅ CMA-CGM UAT Test completed!"
echo "=========================="
