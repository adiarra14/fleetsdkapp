/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Main Integration Service
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

package com.maxvision.cmacgm.service;

import com.maxvision.cmacgm.client.ApiResponse;
import com.maxvision.cmacgm.client.CmaCgmApiClient;
import com.maxvision.cmacgm.config.CmaCgmConfig;
import com.maxvision.cmacgm.model.Coordinate;
import com.maxvision.cmacgm.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CmaCgmIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CmaCgmIntegrationService.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    @Autowired
    private CmaCgmApiClient apiClient;
    
    @Autowired
    private CmaCgmConfig config;
    
    /**
     * Send balise coordinates to CMA-CGM
     */
    @Async
    public CompletableFuture<ApiResponse> sendBaliseCoordinates(String equipmentReference, 
                                                               double latitude, 
                                                               double longitude,
                                                               Instant timestamp) {
        
        if (!config.getIntegration().isEnabled()) {
            logger.debug("CMA-CGM integration is disabled");
            return CompletableFuture.completedFuture(
                ApiResponse.error("INTEGRATION_DISABLED", "CMA-CGM integration is disabled", "")
            );
        }
        
        logger.info("Sending balise coordinates for equipment: {}", equipmentReference);
        
        try {
            // Create coordinate object
            Coordinate coordinate = createCoordinate(equipmentReference, latitude, longitude, timestamp);
            List<Coordinate> coordinates = List.of(coordinate);
            
            // Send to CMA-CGM API
            return apiClient.sendCoordinates(coordinates)
                    .thenApply(response -> {
                        if (response.isSuccess()) {
                            logger.info("Successfully sent coordinates for equipment: {}", equipmentReference);
                        } else {
                            logger.error("Failed to send coordinates for equipment: {} - {}", 
                                       equipmentReference, response.getErrorMessage());
                        }
                        return response;
                    });
            
        } catch (Exception e) {
            logger.error("Error sending coordinates for equipment: {}", equipmentReference, e);
            return CompletableFuture.completedFuture(
                ApiResponse.error("PROCESSING_ERROR", "Error processing coordinates", e.getMessage())
            );
        }
    }
    
    /**
     * Send multiple balise coordinates in batch
     */
    @Async
    public CompletableFuture<ApiResponse> sendBaliseCoordinatesBatch(List<BaliseCoordinateData> coordinateDataList) {
        
        if (!config.getIntegration().isEnabled()) {
            logger.debug("CMA-CGM integration is disabled");
            return CompletableFuture.completedFuture(
                ApiResponse.error("INTEGRATION_DISABLED", "CMA-CGM integration is disabled", "")
            );
        }
        
        logger.info("Sending batch of {} balise coordinates to CMA-CGM", coordinateDataList.size());
        
        try {
            List<Coordinate> coordinates = new ArrayList<>();
            
            for (BaliseCoordinateData data : coordinateDataList) {
                Coordinate coordinate = createCoordinate(
                    data.getEquipmentReference(),
                    data.getLatitude(),
                    data.getLongitude(),
                    data.getTimestamp()
                );
                coordinates.add(coordinate);
            }
            
            // Send batch to CMA-CGM API
            return apiClient.sendCoordinates(coordinates)
                    .thenApply(response -> {
                        if (response.isSuccess()) {
                            logger.info("Successfully sent batch of {} coordinates", coordinates.size());
                        } else {
                            logger.error("Failed to send batch coordinates - {}", response.getErrorMessage());
                        }
                        return response;
                    });
            
        } catch (Exception e) {
            logger.error("Error sending batch coordinates", e);
            return CompletableFuture.completedFuture(
                ApiResponse.error("BATCH_PROCESSING_ERROR", "Error processing batch coordinates", e.getMessage())
            );
        }
    }
    
    /**
     * Create CMA-CGM coordinate object from balise data
     */
    private Coordinate createCoordinate(String equipmentReference, double latitude, double longitude, Instant timestamp) {
        Coordinate coordinate = new Coordinate();
        
        // Required fields
        coordinate.setEquipmentReference(equipmentReference);
        coordinate.setEventCreatedDateTime(formatTimestamp(timestamp));
        coordinate.setOriginatorName(config.getIntegration().getOriginatorName());
        coordinate.setEventLocation(new Location(latitude, longitude));
        
        // Optional fields with defaults
        coordinate.setPartnerName(config.getIntegration().getPartnerName());
        coordinate.setModeOfTransport(config.getIntegration().getDefaultModeOfTransport());
        coordinate.setFuelType(config.getIntegration().getDefaultFuelType());
        coordinate.setTruckMileageUnit(config.getIntegration().getDefaultMileageUnit());
        coordinate.setReeferTemperatureUnit(config.getIntegration().getDefaultTemperatureUnit());
        
        return coordinate;
    }
    
    /**
     * Format timestamp for CMA-CGM API (ISO 8601 UTC format)
     */
    private String formatTimestamp(Instant timestamp) {
        return timestamp.atOffset(ZoneOffset.UTC).format(ISO_FORMATTER);
    }
    
    /**
     * Test CMA-CGM API connectivity
     */
    public CompletableFuture<Boolean> testConnectivity() {
        logger.info("Testing CMA-CGM API connectivity");
        return apiClient.testConnectivity();
    }
    
    /**
     * Check if CMA-CGM integration is enabled
     */
    public boolean isIntegrationEnabled() {
        return config.getIntegration().isEnabled();
    }
    
    /**
     * Get integration configuration
     */
    public CmaCgmConfig.Integration getIntegrationConfig() {
        return config.getIntegration();
    }
    
    /**
     * Data class for balise coordinate information
     */
    public static class BaliseCoordinateData {
        private String equipmentReference;
        private double latitude;
        private double longitude;
        private Instant timestamp;
        
        public BaliseCoordinateData(String equipmentReference, double latitude, double longitude, Instant timestamp) {
            this.equipmentReference = equipmentReference;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getEquipmentReference() {
            return equipmentReference;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
    }
}
