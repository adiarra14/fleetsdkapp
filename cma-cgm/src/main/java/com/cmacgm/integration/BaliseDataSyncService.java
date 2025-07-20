/*
 * Fleet Management System with Maxvision SDK Integration
 * 
 * Project: Fleet Management System with CMA-CGM Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This code is proprietary until transfer to client.
 */

package com.cmacgm.integration;

import com.cmacgm.api.CmaCgmApiClient;
import com.cmacgm.model.Coordinate;
import com.cmacgm.model.Event;
import com.cmacgm.model.EventLocation;
import com.cmacgm.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service that synchronizes balise data from SDK database to CMA-CGM API
 * 
 * This service:
 * 1. Reads new balise GPS coordinates and events from PostgreSQL database
 * 2. Transforms the data to CMA-CGM JSON format
 * 3. Sends data to CMA-CGM UAT/Production API
 * 4. Tracks synchronization status to avoid duplicates
 */
@Service
public class BaliseDataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(BaliseDataSyncService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CmaCgmApiClient cmaCgmApiClient;

    // Run every 5 minutes to sync new data
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 ms
    public void syncBaliseDataToCmaCgm() {
        logger.info("Starting balise data synchronization to CMA-CGM...");
        
        try {
            // Sync GPS coordinates
            syncGpsCoordinates();
            
            // Sync transport events
            syncTransportEvents();
            
            logger.info("Balise data synchronization completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during balise data synchronization: {}", e.getMessage(), e);
        }
    }

    /**
     * Sync GPS coordinates from balise_events table to CMA-CGM coordinates API
     */
    private void syncGpsCoordinates() {
        logger.info("Syncing GPS coordinates to CMA-CGM...");

        // Query for new GPS data from balise_events that hasn't been synced yet
        String sql = """
            SELECT be.id, be.balise_id, be.event_time, be.location,
                   b.imei, b.name as balise_name, b.container_id,
                   c.name as container_name
            FROM balise_events be
            JOIN balises b ON be.balise_id = b.id
            LEFT JOIN containers c ON b.container_id = c.id
            WHERE be.location IS NOT NULL 
              AND be.event_time > NOW() - INTERVAL '1 hour'
              AND NOT EXISTS (
                  SELECT 1 FROM cma_cgm_sync_log 
                  WHERE event_id = be.id AND sync_type = 'GPS'
              )
            ORDER BY be.event_time DESC
            LIMIT 50
            """;

        List<Map<String, Object>> gpsData = jdbcTemplate.queryForList(sql);
        
        if (gpsData.isEmpty()) {
            logger.info("No new GPS coordinates to sync");
            return;
        }

        List<Coordinate> coordinates = new ArrayList<>();
        
        for (Map<String, Object> row : gpsData) {
            try {
                Coordinate coord = createCoordinateFromBaliseData(row);
                coordinates.add(coord);
                
                // Mark as synced
                markAsSynced((Long) row.get("id"), "GPS");
                
            } catch (Exception e) {
                logger.error("Error processing GPS data for balise event {}: {}", 
                    row.get("id"), e.getMessage());
            }
        }

        if (!coordinates.isEmpty()) {
            // Send to CMA-CGM API
            cmaCgmApiClient.sendCoordinates(coordinates);
            logger.info("Sent {} GPS coordinates to CMA-CGM", coordinates.size());
        }
    }

    /**
     * Sync transport events from balise_events table to CMA-CGM events API
     */
    private void syncTransportEvents() {
        logger.info("Syncing transport events to CMA-CGM...");

        // Query for transport events (arrivals, departures, etc.)
        String sql = """
            SELECT be.id, be.balise_id, be.event_type, be.event_time, be.location,
                   b.imei, b.name as balise_name, b.container_id,
                   c.name as container_name
            FROM balise_events be
            JOIN balises b ON be.balise_id = b.id
            LEFT JOIN containers c ON b.container_id = c.id
            WHERE be.event_type IN ('ARRIVAL', 'DEPARTURE', 'LOADING', 'UNLOADING')
              AND be.event_time > NOW() - INTERVAL '1 hour'
              AND NOT EXISTS (
                  SELECT 1 FROM cma_cgm_sync_log 
                  WHERE event_id = be.id AND sync_type = 'EVENT'
              )
            ORDER BY be.event_time DESC
            LIMIT 50
            """;

        List<Map<String, Object>> eventData = jdbcTemplate.queryForList(sql);
        
        if (eventData.isEmpty()) {
            logger.info("No new transport events to sync");
            return;
        }

        List<Event> events = new ArrayList<>();
        
        for (Map<String, Object> row : eventData) {
            try {
                Event event = createEventFromBaliseData(row);
                events.add(event);
                
                // Mark as synced
                markAsSynced((Long) row.get("id"), "EVENT");
                
            } catch (Exception e) {
                logger.error("Error processing event data for balise event {}: {}", 
                    row.get("id"), e.getMessage());
            }
        }

        if (!events.isEmpty()) {
            // Send to CMA-CGM API
            cmaCgmApiClient.sendEvents(events);
            logger.info("Sent {} transport events to CMA-CGM", events.size());
        }
    }

    /**
     * Create CMA-CGM Coordinate object from balise database data
     */
    private Coordinate createCoordinateFromBaliseData(Map<String, Object> row) {
        Coordinate coord = new Coordinate();
        
        // Equipment reference = container name or balise IMEI
        String containerName = (String) row.get("container_name");
        String equipmentRef = containerName != null ? containerName : (String) row.get("imei");
        coord.setEquipmentReference(equipmentRef);
        
        // Event time
        Timestamp eventTime = (Timestamp) row.get("event_time");
        coord.setEventCreatedDateTime(eventTime.toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
        
        // Originator and partner
        coord.setOriginatorName("SINIGROUP");
        coord.setPartnerName("SINI TRANSPORT");
        
        // Booking reference (use container name if available)
        coord.setCarrierBookingReference(containerName != null ? containerName : equipmentRef);
        
        // Transport details
        coord.setModeOfTransport("TRUCK");
        coord.setTransportOrder("TO" + equipmentRef);
        
        // Extract coordinates from PostGIS POINT
        String locationStr = (String) row.get("location");
        if (locationStr != null) {
            // Parse PostGIS POINT format: POINT(-8.0029 -12.6392)
            String[] coords = locationStr.replace("POINT(", "").replace(")", "").split(" ");
            if (coords.length == 2) {
                Location location = new Location();
                location.setLongitude(Double.parseDouble(coords[0]));
                location.setLatitude(Double.parseDouble(coords[1]));
                coord.setEventLocation(location);
            }
        }
        
        return coord;
    }

    /**
     * Create CMA-CGM Event object from balise database data
     */
    private Event createEventFromBaliseData(Map<String, Object> row) {
        Event event = new Event();
        
        // Equipment reference
        String containerName = (String) row.get("container_name");
        String equipmentRef = containerName != null ? containerName : (String) row.get("imei");
        event.setEquipmentReference(equipmentRef);
        
        // Event time
        Timestamp eventTime = (Timestamp) row.get("event_time");
        event.setEventCreatedDateTime(eventTime.toLocalDateTime()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
        
        // Originator and partner
        event.setOriginatorName("SINIGROUP");
        event.setPartnerName("SINI TRANSPORT");
        
        // Event type mapping
        String baliseEventType = (String) row.get("event_type");
        event.setEventType("TRANSPORT");
        event.setTransportEventTypeCode(mapEventType(baliseEventType));
        event.setEquipmentEventTypeCode("");
        event.setEventClassifierCode("ACT");
        
        // Booking and transport details
        event.setCarrierBookingReference(containerName != null ? containerName : equipmentRef);
        event.setModeOfTransport("TRUCK");
        event.setTransportationPhase("IMPORT");
        event.setTransportOrder("TO" + equipmentRef);
        
        // Event location
        EventLocation eventLocation = new EventLocation();
        eventLocation.setFacilityTypeCode("CLOC");
        eventLocation.setLocationCode("BAMAKO_DEPOT_01");
        eventLocation.setLocationUnLocode("");
        eventLocation.setLocationName("Bamako Central Depot");
        
        // Extract coordinates from PostGIS POINT
        String locationStr = (String) row.get("location");
        if (locationStr != null) {
            String[] coords = locationStr.replace("POINT(", "").replace(")", "").split(" ");
            if (coords.length == 2) {
                eventLocation.setLongitude(Double.parseDouble(coords[0]));
                eventLocation.setLatitude(Double.parseDouble(coords[1]));
            }
        }
        
        // Default Bamako address
        EventLocation.Address address = new EventLocation.Address();
        address.setName("Bamako Central Depot");
        address.setStreet("Avenue de la Nation");
        address.setStreetNumber("123");
        address.setFloor("");
        address.setPostCode("");
        address.setCity("BAMAKO");
        address.setStateRegion("Bamako District");
        address.setCountry("MALI");
        eventLocation.setAddress(address);
        
        event.setEventLocation(eventLocation);
        
        return event;
    }

    /**
     * Map balise event types to CMA-CGM transport event type codes
     */
    private String mapEventType(String baliseEventType) {
        switch (baliseEventType.toUpperCase()) {
            case "ARRIVAL":
                return "ARRI";
            case "DEPARTURE":
                return "DEPA";
            case "LOADING":
                return "LOAD";
            case "UNLOADING":
                return "DISC";
            default:
                return "ARRI"; // Default to arrival
        }
    }

    /**
     * Mark balise event as synced to avoid duplicates
     */
    private void markAsSynced(Long eventId, String syncType) {
        try {
            // Create sync log table if it doesn't exist
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS cma_cgm_sync_log (
                    id SERIAL PRIMARY KEY,
                    event_id BIGINT NOT NULL,
                    sync_type VARCHAR(10) NOT NULL,
                    sync_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(event_id, sync_type)
                )
                """);
            
            // Insert sync record
            jdbcTemplate.update(
                "INSERT INTO cma_cgm_sync_log (event_id, sync_type) VALUES (?, ?) ON CONFLICT DO NOTHING",
                eventId, syncType
            );
            
        } catch (Exception e) {
            logger.warn("Failed to mark event {} as synced: {}", eventId, e.getMessage());
        }
    }

    /**
     * Manual sync method for testing
     */
    public void manualSync() {
        logger.info("Manual sync triggered");
        syncBaliseDataToCmaCgm();
    }
}
