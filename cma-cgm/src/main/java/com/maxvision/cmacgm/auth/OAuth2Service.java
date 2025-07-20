/**
 * Fleet Monitor - CMA-CGM Integration Module
 * OAuth2 Authentication Service
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

package com.maxvision.cmacgm.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class OAuth2Service {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);
    
    @Value("${cmacgm.auth.url:https://auth-pre.cma-cgm.com/as/token.oauth2}")
    private String authUrl;
    
    @Value("${cmacgm.auth.client-id:beapp-sinigroup}")
    private String clientId;
    
    @Value("${cmacgm.auth.client-secret}")
    private String clientSecret;
    
    @Value("${cmacgm.auth.scope:tracking:write:be}")
    private String scope;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TokenManager tokenManager;
    
    public OAuth2Service(TokenManager tokenManager) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.tokenManager = tokenManager;
    }
    
    /**
     * Get a valid access token, refreshing if necessary
     */
    public CompletableFuture<String> getAccessToken() {
        // Check if current token is still valid
        if (tokenManager.isTokenValid()) {
            return CompletableFuture.completedFuture(tokenManager.getCurrentToken());
        }
        
        // Request new token
        return requestNewToken();
    }
    
    /**
     * Request a new access token from CMA-CGM OAuth2 endpoint
     */
    private CompletableFuture<String> requestNewToken() {
        logger.info("Requesting new OAuth2 token from CMA-CGM");
        
        String requestBody = String.format(
            "client_id=%s&client_secret=%s&grant_type=client_credentials&scope=%s",
            clientId, clientSecret, scope
        );
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseTokenResponse)
                .exceptionally(this::handleAuthError);
    }
    
    /**
     * Parse the OAuth2 token response
     */
    private String parseTokenResponse(HttpResponse<String> response) {
        try {
            if (response.statusCode() == 200) {
                TokenResponse tokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);
                
                // Store token with expiration time
                Instant expiresAt = Instant.now().plusSeconds(tokenResponse.getExpiresIn() - 30); // 30s buffer
                tokenManager.storeToken(tokenResponse.getAccessToken(), expiresAt);
                
                logger.info("Successfully obtained OAuth2 token, expires in {} seconds", tokenResponse.getExpiresIn());
                return tokenResponse.getAccessToken();
            } else {
                logger.error("OAuth2 authentication failed: {} - {}", response.statusCode(), response.body());
                throw new RuntimeException("OAuth2 authentication failed: " + response.statusCode());
            }
        } catch (IOException e) {
            logger.error("Failed to parse OAuth2 token response", e);
            throw new RuntimeException("Failed to parse OAuth2 token response", e);
        }
    }
    
    /**
     * Handle authentication errors
     */
    private String handleAuthError(Throwable throwable) {
        logger.error("OAuth2 authentication error", throwable);
        throw new RuntimeException("OAuth2 authentication failed", throwable);
    }
    
    /**
     * OAuth2 Token Response model
     */
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("expires_in")
        private int expiresIn;
        
        @JsonProperty("scope")
        private String scope;
        
        // Getters and setters
        public String getAccessToken() {
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getTokenType() {
            return tokenType;
        }
        
        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
        
        public int getExpiresIn() {
            return expiresIn;
        }
        
        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }
        
        public String getScope() {
            return scope;
        }
        
        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}
