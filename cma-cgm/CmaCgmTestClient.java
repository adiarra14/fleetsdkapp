/**
 * Fleet Monitor - CMA-CGM Integration Test Client
 * Simple test client to send GPS and event data to CMA-CGM UAT
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CmaCgmTestClient {
    
    // CMA-CGM UAT Configuration
    private static final String AUTH_URL = "https://auth-pre.cma-cgm.com/as/token.oauth2";
    private static final String API_BASE_URL = "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1";
    private static final String CLIENT_ID = "beapp-sinigroup";
    private static final String CLIENT_SECRET = "YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ";
    private static final String SCOPE = "tracking:write:be";
    
    private final HttpClient httpClient;
    
    public CmaCgmTestClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * Step 1: Get OAuth2 token
     */
    public String getAccessToken() throws IOException, InterruptedException {
        String requestBody = String.format(
            "client_id=%s&client_secret=%s&grant_type=client_credentials&scope=%s",
            CLIENT_ID, CLIENT_SECRET, SCOPE
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // Extract access_token from JSON response
            String responseBody = response.body();
            String token = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
            System.out.println("✅ OAuth2 token obtained successfully");
            return token;
        } else {
            throw new RuntimeException("Authentication failed: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Step 2: Send GPS coordinates in CMA-CGM format
     */
    public void sendGpsCoordinates(String token) throws IOException, InterruptedException {
        // GPS data in exact CMA-CGM format
        String gpsData = """
        [
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
        ]
        """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/coordinates"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gpsData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("📍 GPS Coordinates Response: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        
        if (response.statusCode() == 201) {
            System.out.println("✅ GPS coordinates sent successfully!");
        } else {
            System.out.println("❌ Failed to send GPS coordinates");
        }
    }
    
    /**
     * Step 3: Send event data in CMA-CGM format
     */
    public void sendEventData(String token) throws IOException, InterruptedException {
        // Event data in exact CMA-CGM format
        String eventData = """
        [
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
        ]
        """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/events"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(eventData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("📅 Event Data Response: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        
        if (response.statusCode() == 201) {
            System.out.println("✅ Event data sent successfully!");
        } else {
            System.out.println("❌ Failed to send event data");
        }
    }
    
    /**
     * Main test method
     */
    public static void main(String[] args) {
        CmaCgmTestClient client = new CmaCgmTestClient();
        
        try {
            System.out.println("🚀 Starting CMA-CGM UAT Test...");
            
            // Step 1: Authenticate
            System.out.println("\n1️⃣ Getting OAuth2 token...");
            String token = client.getAccessToken();
            
            // Step 2: Send GPS coordinates
            System.out.println("\n2️⃣ Sending GPS coordinates...");
            client.sendGpsCoordinates(token);
            
            // Step 3: Send event data
            System.out.println("\n3️⃣ Sending event data...");
            client.sendEventData(token);
            
            System.out.println("\n✅ CMA-CGM UAT Test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
