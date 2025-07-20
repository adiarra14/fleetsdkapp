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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for CMA-CGM Integration Management
 * 
 * Provides endpoints to:
 * - Manually trigger data synchronization
 * - Monitor sync status and statistics
 * - View recent balise data
 * - Check integration health
 */
@RestController
@RequestMapping("/api/cmacgm")
@CrossOrigin(origins = "*")
public class CmaCgmIntegrationController {

    private static final Logger logger = LoggerFactory.getLogger(CmaCgmIntegrationController.class);

    @Autowired
    private BaliseDataSyncService syncService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Manually trigger synchronization to CMA-CGM
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerSync() {
        logger.info("Manual sync triggered via API");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            syncService.manualSync();
            response.put("status", "success");
            response.put("message", "Synchronization triggered successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during manual sync: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Synchronization failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get synchronization statistics
     */
    @GetMapping("/sync/stats")
    public ResponseEntity<Map<String, Object>> getSyncStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Count total balise events
            Integer totalEvents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '24 hours'",
                Integer.class
            );
            
            // Count synced events (if sync log table exists)
            Integer syncedGps = 0;
            Integer syncedEvents = 0;
            
            try {
                syncedGps = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM cma_cgm_sync_log WHERE sync_type = 'GPS' AND sync_time > NOW() - INTERVAL '24 hours'",
                    Integer.class
                );
                
                syncedEvents = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM cma_cgm_sync_log WHERE sync_type = 'EVENT' AND sync_time > NOW() - INTERVAL '24 hours'",
                    Integer.class
                );
            } catch (Exception e) {
                // Sync log table might not exist yet
                logger.debug("Sync log table not found: {}", e.getMessage());
            }
            
            // Count active balises
            Integer activeBalises = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises WHERE last_seen > NOW() - INTERVAL '1 hour'",
                Integer.class
            );
            
            stats.put("totalEventsLast24h", totalEvents);
            stats.put("syncedGpsLast24h", syncedGps);
            stats.put("syncedEventsLast24h", syncedEvents);
            stats.put("activeBalises", activeBalises);
            stats.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting sync stats: {}", e.getMessage(), e);
            stats.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(stats);
        }
    }

    /**
     * Get recent balise data that will be/was synced to CMA-CGM
     */
    @GetMapping("/data/recent")
    public ResponseEntity<Map<String, Object>> getRecentData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get recent GPS data
            String gpsQuery = """
                SELECT be.id, be.event_time, be.location,
                       b.imei, b.name as balise_name,
                       c.name as container_name,
                       CASE WHEN csl.event_id IS NOT NULL THEN true ELSE false END as synced
                FROM balise_events be
                JOIN balises b ON be.balise_id = b.id
                LEFT JOIN containers c ON b.container_id = c.id
                LEFT JOIN cma_cgm_sync_log csl ON be.id = csl.event_id AND csl.sync_type = 'GPS'
                WHERE be.location IS NOT NULL 
                  AND be.event_time > NOW() - INTERVAL '2 hours'
                ORDER BY be.event_time DESC
                LIMIT 20
                """;
            
            List<Map<String, Object>> gpsData = jdbcTemplate.queryForList(gpsQuery);
            
            // Get recent event data
            String eventQuery = """
                SELECT be.id, be.event_type, be.event_time, be.location,
                       b.imei, b.name as balise_name,
                       c.name as container_name,
                       CASE WHEN csl.event_id IS NOT NULL THEN true ELSE false END as synced
                FROM balise_events be
                JOIN balises b ON be.balise_id = b.id
                LEFT JOIN containers c ON b.container_id = c.id
                LEFT JOIN cma_cgm_sync_log csl ON be.id = csl.event_id AND csl.sync_type = 'EVENT'
                WHERE be.event_type IN ('ARRIVAL', 'DEPARTURE', 'LOADING', 'UNLOADING')
                  AND be.event_time > NOW() - INTERVAL '2 hours'
                ORDER BY be.event_time DESC
                LIMIT 20
                """;
            
            List<Map<String, Object>> eventData = jdbcTemplate.queryForList(eventQuery);
            
            response.put("gpsData", gpsData);
            response.put("eventData", eventData);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting recent data: {}", e.getMessage(), e);
            response.put("error", "Failed to get recent data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get integration health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Check database connectivity
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("database", "healthy");
            
            // Check if balises table exists and has data
            Integer baliseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises", Integer.class
            );
            health.put("baliseCount", baliseCount);
            
            // Check recent activity
            Integer recentEvents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '1 hour'",
                Integer.class
            );
            health.put("recentEvents", recentEvents);
            
            // Overall health
            String status = (baliseCount > 0) ? "healthy" : "no_data";
            health.put("status", status);
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            health.put("status", "error");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(health);
        }
    }

    /**
     * Get CMA-CGM integration configuration info
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("description", "CMA-CGM Integration for DK Balise Customer");
        config.put("syncInterval", "5 minutes");
        config.put("dataTypes", new String[]{"GPS Coordinates", "Transport Events"});
        config.put("eventTypes", new String[]{"ARRIVAL", "DEPARTURE", "LOADING", "UNLOADING"});
        config.put("location", "Bamako, Mali");
        config.put("apiEndpoints", new String[]{
            "/coordinates - GPS data",
            "/events - Transport events"
        });
        config.put("status", "Production Ready");
        
        return ResponseEntity.ok(config);
    }
}
