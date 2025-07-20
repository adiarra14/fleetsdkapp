/**
 * Fleet Monitor - CMA-CGM Integration Module
 * API Response Model
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

import java.time.Instant;

public class ApiResponse {
    
    private final boolean success;
    private final int statusCode;
    private final String responseBody;
    private final String errorCode;
    private final String errorMessage;
    private final String errorDetails;
    private final Instant timestamp;
    
    private ApiResponse(boolean success, int statusCode, String responseBody, 
                       String errorCode, String errorMessage, String errorDetails) {
        this.success = success;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
        this.timestamp = Instant.now();
    }
    
    /**
     * Create a successful API response
     */
    public static ApiResponse success(int statusCode, String responseBody) {
        return new ApiResponse(true, statusCode, responseBody, null, null, null);
    }
    
    /**
     * Create an error API response
     */
    public static ApiResponse error(String errorCode, String errorMessage, String errorDetails) {
        return new ApiResponse(false, 0, null, errorCode, errorMessage, errorDetails);
    }
    
    /**
     * Create an error API response with status code
     */
    public static ApiResponse error(int statusCode, String errorCode, String errorMessage, String errorDetails) {
        return new ApiResponse(false, statusCode, null, errorCode, errorMessage, errorDetails);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "ApiResponse{" +
                    "success=true" +
                    ", statusCode=" + statusCode +
                    ", timestamp=" + timestamp +
                    '}';
        } else {
            return "ApiResponse{" +
                    "success=false" +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
