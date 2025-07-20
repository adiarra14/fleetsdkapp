/**
 * Fleet Monitor - CMA-CGM Integration Module
 * CMA-CGM API Client
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

package com.maxvision.cmacgm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxvision.cmacgm.auth.OAuth2Service;
import com.maxvision.cmacgm.model.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CmaCgmApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CmaCgmApiClient.class);
    
    @Value("${cmacgm.api.base-url:https://apis-uat.cma-cgm.net/technical/generic/eagle/v1}")
    private String baseUrl;
    
    @Value("${cmacgm.api.timeout:30}")
    private int timeoutSeconds;
    
    @Value("${cmacgm.api.retry-attempts:3}")
    private int retryAttempts;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OAuth2Service oAuth2Service;
    
    public CmaCgmApiClient(OAuth2Service oAuth2Service) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.oAuth2Service = oAuth2Service;
    }
    
    /**
     * Send coordinates to CMA-CGM API
     */
    public CompletableFuture<ApiResponse> sendCoordinates(List<Coordinate> coordinates) {
        logger.info("Sending {} coordinates to CMA-CGM API", coordinates.size());
        
        return oAuth2Service.getAccessToken()
                .thenCompose(token -> sendCoordinatesWithToken(coordinates, token))
                .exceptionally(this::handleApiError);
    }
    
    /**
     * Send coordinates with authentication token
     */
    private CompletableFuture<ApiResponse> sendCoordinatesWithToken(List<Coordinate> coordinates, String token) {
        try {
            String jsonBody = objectMapper.writeValueAsString(coordinates);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/coordinates"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> processApiResponse(response, "coordinates"));
            
        } catch (IOException e) {
            logger.error("Failed to serialize coordinates to JSON", e);
            return CompletableFuture.completedFuture(
                ApiResponse.error("JSON_SERIALIZATION_ERROR", "Failed to serialize coordinates", e.getMessage())
            );
        }
    }
    
    /**
     * Send events to CMA-CGM API (placeholder for future implementation)
     */
    public CompletableFuture<ApiResponse> sendEvents(List<Object> events) {
        logger.info("Sending {} events to CMA-CGM API", events.size());
        
        return oAuth2Service.getAccessToken()
                .thenCompose(token -> sendEventsWithToken(events, token))
                .exceptionally(this::handleApiError);
    }
    
    /**
     * Send events with authentication token
     */
    private CompletableFuture<ApiResponse> sendEventsWithToken(List<Object> events, String token) {
        try {
            String jsonBody = objectMapper.writeValueAsString(events);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/events"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> processApiResponse(response, "events"));
            
        } catch (IOException e) {
            logger.error("Failed to serialize events to JSON", e);
            return CompletableFuture.completedFuture(
                ApiResponse.error("JSON_SERIALIZATION_ERROR", "Failed to serialize events", e.getMessage())
            );
        }
    }
    
    /**
     * Process API response and create ApiResponse object
     */
    private ApiResponse processApiResponse(HttpResponse<String> response, String endpoint) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        logger.debug("CMA-CGM API response for {}: {} - {}", endpoint, statusCode, responseBody);
        
        if (statusCode == 201) {
            logger.info("Successfully sent data to CMA-CGM {} endpoint", endpoint);
            return ApiResponse.success(statusCode, responseBody);
        } else if (statusCode >= 400 && statusCode < 500) {
            logger.warn("Client error sending data to CMA-CGM {} endpoint: {} - {}", endpoint, statusCode, responseBody);
            return ApiResponse.error("CLIENT_ERROR", "Client error: " + statusCode, responseBody);
        } else if (statusCode >= 500) {
            logger.error("Server error sending data to CMA-CGM {} endpoint: {} - {}", endpoint, statusCode, responseBody);
            return ApiResponse.error("SERVER_ERROR", "Server error: " + statusCode, responseBody);
        } else {
            logger.warn("Unexpected response from CMA-CGM {} endpoint: {} - {}", endpoint, statusCode, responseBody);
            return ApiResponse.error("UNEXPECTED_RESPONSE", "Unexpected response: " + statusCode, responseBody);
        }
    }
    
    /**
     * Handle API errors
     */
    private ApiResponse handleApiError(Throwable throwable) {
        logger.error("API call failed", throwable);
        return ApiResponse.error("API_CALL_FAILED", "API call failed", throwable.getMessage());
    }
    
    /**
     * Test API connectivity
     */
    public CompletableFuture<Boolean> testConnectivity() {
        logger.info("Testing CMA-CGM API connectivity");
        
        return oAuth2Service.getAccessToken()
                .thenApply(token -> {
                    logger.info("Successfully obtained OAuth2 token for connectivity test");
                    return true;
                })
                .exceptionally(throwable -> {
                    logger.error("Connectivity test failed", throwable);
                    return false;
                });
    }
}
