/**
 * Fleet Monitor - CMA-CGM Multiple Data Test Client
 * Test client to send multiple different GPS and event data to CMA-CGM UAT
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

public class CmaCgmMultipleDataTest {
    
    // CMA-CGM UAT Configuration
    private static final String AUTH_URL = "https://auth-pre.cma-cgm.com/as/token.oauth2";
    private static final String API_BASE_URL = "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1";
    private static final String CLIENT_ID = "beapp-sinigroup";
    private static final String CLIENT_SECRET = "YChnAz1dI2gvr40BMOyQWIeYT83DdtitHYfDmGd04xG5llcugV9NvfeihE72s1cJ";
    private static final String SCOPE = "tracking:write:be";
    
    private final HttpClient httpClient;
    
    public CmaCgmMultipleDataTest() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    /**
     * Get OAuth2 token
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
            String responseBody = response.body();
            String token = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
            System.out.println("SUCCESS: OAuth2 token obtained");
            return token;
        } else {
            throw new RuntimeException("Authentication failed: " + response.statusCode());
        }
    }
    
    /**
     * Send multiple GPS coordinates for different containers and locations
     */
    public void sendMultipleGpsCoordinates(String token) throws IOException, InterruptedException {
        
        // Dataset 1: Container APZU2106333 - Bamako, Mali
        String gpsData1 = "[{" +
            "\"equipmentReference\": \"APZU2106333\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T08:00:00Z\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT MALI\"," +
            "\"carrierBookingReference\": \"LHV3076333\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportOrder\": \"TLHV2330333\"," +
            "\"eventLocation\": {" +
                "\"latitude\": 12.6392," +
                "\"longitude\": -8.0029" +
            "}" +
        "}]";
        
        // Dataset 2: Container CGMU5417495 - Dakar, Sénégal
        String gpsData2 = "[{" +
            "\"equipmentReference\": \"CGMU5417495\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T09:15:00Z\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT SENEGAL\"," +
            "\"carrierBookingReference\": \"DKR2025001\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportOrder\": \"TDKR2330001\"," +
            "\"eventLocation\": {" +
                "\"latitude\": 14.6928," +
                "\"longitude\": -17.4467" +
            "}" +
        "}]";
        
        // Dataset 3: Container MSKU7654321 - Abidjan, Côte d'Ivoire
        String gpsData3 = "[{" +
            "\"equipmentReference\": \"MSKU7654321\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T10:30:00Z\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT COTE IVOIRE\"," +
            "\"carrierBookingReference\": \"ABJ2025002\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportOrder\": \"TABJ2330002\"," +
            "\"eventLocation\": {" +
                "\"latitude\": 5.3364," +
                "\"longitude\": -4.0267" +
            "}" +
        "}]";
        
        // Send each dataset
        sendGpsData(token, gpsData1, "Mali - Bamako");
        Thread.sleep(2000); // Wait 2 seconds between requests
        
        sendGpsData(token, gpsData2, "Sénégal - Dakar");
        Thread.sleep(2000);
        
        sendGpsData(token, gpsData3, "Côte d'Ivoire - Abidjan");
    }
    
    /**
     * Send multiple event data for different scenarios
     */
    public void sendMultipleEventData(String token) throws IOException, InterruptedException {
        
        // Event 1: Arrival at customer location - Mali
        String eventData1 = "[{" +
            "\"equipmentReference\": \"APZU2106333\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T08:30:00Z\"," +
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
                "\"locationCode\": \"BAMAKO_CLIENT_001\"," +
                "\"locationUnLocode\": \"\"," +
                "\"locationName\": \"Client Bamako Centre\"," +
                "\"latitude\": 12.6392," +
                "\"longitude\": -8.0029," +
                "\"address\": {" +
                    "\"name\": \"Entrepôt Central Bamako\"," +
                    "\"street\": \"Route de Koulikoro\"," +
                    "\"streetNumber\": \"123\"," +
                    "\"floor\": \"\"," +
                    "\"postCode\": \"\"," +
                    "\"city\": \"BAMAKO\"," +
                    "\"stateRegion\": \"Bamako\"," +
                    "\"country\": \"MALI\"" +
                "}" +
            "}" +
        "}]";
        
        // Event 2: Departure from depot - Sénégal
        String eventData2 = "[{" +
            "\"equipmentReference\": \"CGMU5417495\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T09:00:00Z\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT SENEGAL\"," +
            "\"eventType\": \"TRANSPORT\"," +
            "\"transportEventTypeCode\": \"DEPA\"," +
            "\"equipmentEventTypeCode\": \"\"," +
            "\"eventClassifierCode\": \"ACT\"," +
            "\"carrierBookingReference\": \"DKR2025001\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportationPhase\": \"EXPORT\"," +
            "\"transportOrder\": \"TDKR2330001\"," +
            "\"eventLocation\": {" +
                "\"facilityTypeCode\": \"DEPO\"," +
                "\"locationCode\": \"DAKAR_DEPOT_001\"," +
                "\"locationUnLocode\": \"SNDKR\"," +
                "\"locationName\": \"Dépôt Port Dakar\"," +
                "\"latitude\": 14.6928," +
                "\"longitude\": -17.4467," +
                "\"address\": {" +
                    "\"name\": \"Dépôt Conteneurs Dakar\"," +
                    "\"street\": \"Avenue du Port\"," +
                    "\"streetNumber\": \"45\"," +
                    "\"floor\": \"\"," +
                    "\"postCode\": \"11000\"," +
                    "\"city\": \"DAKAR\"," +
                    "\"stateRegion\": \"Dakar\"," +
                    "\"country\": \"SENEGAL\"" +
                "}" +
            "}" +
        "}]";
        
        // Event 3: Arrival at terminal - Côte d'Ivoire
        String eventData3 = "[{" +
            "\"equipmentReference\": \"MSKU7654321\"," +
            "\"eventCreatedDateTime\": \"2025-07-25T11:00:00Z\"," +
            "\"originatorName\": \"SINIGROUP\"," +
            "\"partnerName\": \"SINI TRANSPORT COTE IVOIRE\"," +
            "\"eventType\": \"TRANSPORT\"," +
            "\"transportEventTypeCode\": \"ARRI\"," +
            "\"equipmentEventTypeCode\": \"\"," +
            "\"eventClassifierCode\": \"ACT\"," +
            "\"carrierBookingReference\": \"ABJ2025002\"," +
            "\"modeOfTransport\": \"TRUCK\"," +
            "\"transportationPhase\": \"IMPORT\"," +
            "\"transportOrder\": \"TABJ2330002\"," +
            "\"eventLocation\": {" +
                "\"facilityTypeCode\": \"POTE\"," +
                "\"locationCode\": \"ABIDJAN_TERMINAL_001\"," +
                "\"locationUnLocode\": \"CIABJ\"," +
                "\"locationName\": \"Terminal Port Abidjan\"," +
                "\"latitude\": 5.3364," +
                "\"longitude\": -4.0267," +
                "\"address\": {" +
                    "\"name\": \"Terminal Conteneurs Abidjan\"," +
                    "\"street\": \"Boulevard du Port\"," +
                    "\"streetNumber\": \"1\"," +
                    "\"floor\": \"\"," +
                    "\"postCode\": \"01\"," +
                    "\"city\": \"ABIDJAN\"," +
                    "\"stateRegion\": \"Abidjan\"," +
                    "\"country\": \"COTE D'IVOIRE\"" +
                "}" +
            "}" +
        "}]";
        
        // Send each event
        sendEventData(token, eventData1, "Mali - Arrivée client");
        Thread.sleep(2000);
        
        sendEventData(token, eventData2, "Sénégal - Départ dépôt");
        Thread.sleep(2000);
        
        sendEventData(token, eventData3, "Côte d'Ivoire - Arrivée terminal");
    }
    
    private void sendGpsData(String token, String gpsData, String description) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/coordinates"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gpsData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("GPS " + description + " - Status: " + response.statusCode());
        if (response.statusCode() == 201) {
            System.out.println("SUCCESS: GPS data sent for " + description);
        } else {
            System.out.println("FAILED: GPS data for " + description);
        }
    }
    
    private void sendEventData(String token, String eventData, String description) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/events"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(eventData))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Event " + description + " - Status: " + response.statusCode());
        if (response.statusCode() == 201) {
            System.out.println("SUCCESS: Event data sent for " + description);
        } else {
            System.out.println("FAILED: Event data for " + description);
        }
    }
    
    /**
     * Main test method
     */
    public static void main(String[] args) {
        CmaCgmMultipleDataTest client = new CmaCgmMultipleDataTest();
        
        try {
            System.out.println("=== CMA-CGM Multiple Data Test for SiniGroup ===");
            System.out.println("Testing multiple containers across West Africa");
            System.out.println("Date: " + LocalDateTime.now());
            System.out.println();
            
            // Step 1: Authenticate
            System.out.println("Step 1: Getting OAuth2 token...");
            String token = client.getAccessToken();
            System.out.println();
            
            // Step 2: Send multiple GPS coordinates
            System.out.println("Step 2: Sending multiple GPS coordinates...");
            client.sendMultipleGpsCoordinates(token);
            System.out.println();
            
            // Step 3: Send multiple event data
            System.out.println("Step 3: Sending multiple event data...");
            client.sendMultipleEventData(token);
            System.out.println();
            
            System.out.println("=== Multiple Data Test Completed ===");
            System.out.println("Total datasets sent: 6 (3 GPS + 3 Events)");
            System.out.println("Countries covered: Mali, Sénégal, Côte d'Ivoire");
            
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
