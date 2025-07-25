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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CmaCgmTestClientClean {
    
    // CMA-CGM UAT Configuration
    private static final String AUTH_URL = "https://auth-pre.cma-cgm.com/as/token.oauth2";
    private static final String API_BASE_URL = "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1";
    private static final String CLIENT_ID = "beapp-sinigroup";
    private static final String CLIENT_SECRET = "YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ";
    private static final String SCOPE = "tracking:write:be";
    
    private final HttpClient httpClient;
    
    public CmaCgmTestClientClean() {
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
        
        System.out.println("Auth Response Status: " + response.statusCode());
        System.out.println("Auth Response Body: " + response.body());
        
        if (response.statusCode() == 200) {
            // Extract access_token from JSON response
            String responseBody = response.body();
            String token = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
            System.out.println("SUCCESS: OAuth2 token obtained successfully");
            return token;
        } else {
            throw new RuntimeException("Authentication failed: " + response.statusCode() + " - " + response.body());
        }
    }
    
    /**
     * Step 2: Send GPS coordinates in CMA-CGM format
     */
    public void sendGpsCoordinates(String token) throws IOException, InterruptedException {
        // Use a fixed timestamp in the past (as required by CMA-CGM)
        // Format exactly as shown in CMA-CGM email: "2023-06-08T08:39:00Z"
        String currentTime = "2024-07-25T08:00:00Z";
        
        // GPS data in exact CMA-CGM format with real Mali coordinates
        String gpsData = String.format("[{" +
            "\"equipmentReference\": \"APZU2106333\"," +
            "\"eventCreatedDateTime\": \"%s\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT MALI\"," +
            "\"carrierBookingReference\": \"LHV3076333\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportOrder\": \"TLHV2330333\"," +
            "\"eventLocation\": {" +
                "\"latitude\": 12.6392," +
                "\"longitude\": -8.0029" +
            "}" +
        "}]", currentTime);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/coordinates"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gpsData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GPS Coordinates Response Status: " + response.statusCode());
        System.out.println("GPS Response Headers: " + response.headers().map());
        System.out.println("GPS Response Body: " + response.body());
        
        if (response.statusCode() == 201) {
            System.out.println("SUCCESS: GPS coordinates sent successfully!");
        } else {
            System.out.println("ERROR: Failed to send GPS coordinates");
        }
    }
    
    /**
     * Step 3: Send event data in CMA-CGM format
     */
    public void sendEventData(String token) throws IOException, InterruptedException {
        // Use a fixed timestamp in the past (as required by CMA-CGM)
        // Format exactly as shown in CMA-CGM email: "2023-06-08T08:39:00Z"
        String currentTime = "2024-07-25T08:00:00Z";
        
        // Event data in exact CMA-CGM format with Mali location
        String eventData = String.format("[{" +
            "\"equipmentReference\": \"APZU2106333\"," +
            "\"eventCreatedDateTime\": \"%s\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT MALI\"," +
            "\"eventType\": \"TRANSPORT\"," +
            "\"transportEventTypeCode\": \"ARRI\"," +
            "\"equipmentEventTypeCode\": \"\"," +
            "\"eventClassifierCode\": \"ACT\"," +
            "\"carrierBookingReference\": \"LHV3076333\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportationPhase\": \"IMPORT\"," +
            "\"transportOrder\": \"TLHV2330333\"," +
            "\"eventLocation\": {" +
                "\"facilityTypeCode\": \"CLOC\"," +
                "\"locationCode\": \"BAMAKO_DEPOT_001\"," +
                "\"locationUnLocode\": \"\"," +
                "\"locationName\": \"Depot Bamako Centre\"," +
                "\"latitude\": 12.6392," +
                "\"longitude\": -8.0029," +
                "\"address\": {" +
                    "\"name\": \"Depot Central Bamako\"," +
                    "\"street\": \"Route de Koulikoro\"," +
                    "\"streetNumber\": \"123\"," +
                    "\"floor\": \"\"," +
                    "\"postCode\": \"\"," +
                    "\"city\": \"BAMAKO\"," +
                    "\"stateRegion\": \"Bamako\"," +
                    "\"country\": \"MALI\"" +
                "}" +
            "}" +
        "}]", currentTime);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/events"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(eventData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Event Data Response Status: " + response.statusCode());
        System.out.println("Event Response Headers: " + response.headers().map());
        System.out.println("Event Response Body: " + response.body());
        
        if (response.statusCode() == 201) {
            System.out.println("SUCCESS: Event data sent successfully!");
        } else {
            System.out.println("ERROR: Failed to send event data");
        }
    }
    
    /**
     * Main test method
     */
    public static void main(String[] args) {
        CmaCgmTestClientClean client = new CmaCgmTestClientClean();
        
        try {
            System.out.println("=== CMA-CGM UAT Test for SiniGroup ===");
            System.out.println("Testing connection to CMA-CGM technical teams");
            System.out.println("Date: " + LocalDateTime.now());
            System.out.println();
            
            // Step 1: Authenticate
            System.out.println("Step 1: Getting OAuth2 token...");
            String token = client.getAccessToken();
            System.out.println();
            
            // Step 2: Send GPS coordinates
            System.out.println("Step 2: Sending GPS coordinates...");
            client.sendGpsCoordinates(token);
            System.out.println();
            
            // Step 3: Send event data
            System.out.println("Step 3: Sending event data...");
            client.sendEventData(token);
            System.out.println();
            
            System.out.println("=== CMA-CGM UAT Test Completed ===");
            
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
