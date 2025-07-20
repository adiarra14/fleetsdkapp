/*
 * Fleet Management System with Maxvision SDK Integration
 * 
 * Project: Fleet Management System with Multi-Customer Support
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This code is proprietary until transfer to client.
 */

package com.maxvision.backend.controller;

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
 * REST API Controller for Balise Assignment Management
 * 
 * Supports both web app and mobile app for:
 * - Assigning balises to customers (CMA-CGM, DHL, etc.)
 * - Managing CMA-CGM specific container information
 * - Viewing assignment statistics and status
 * - Unassigning and reassigning balises
 */
@RestController
@RequestMapping("/api/balises")
@CrossOrigin(origins = "*")
public class BaliseAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(BaliseAssignmentController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Get all balises with their assignment status
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBalises() {
        try {
            String sql = """
                SELECT b.id, b.name, b.imei, b.type, b.status, b.last_seen, b.battery_level,
                       bca.customer_id, c.name as customer_name, c.code as customer_code,
                       bca.equipment_reference, bca.sync_enabled
                FROM balises b
                LEFT JOIN balise_customer_assignments bca ON b.id = bca.balise_id AND bca.sync_enabled = true
                LEFT JOIN customers c ON bca.customer_id = c.id
                ORDER BY b.name
                """;
            
            List<Map<String, Object>> balises = jdbcTemplate.queryForList(sql);
            
            // Transform data for frontend
            balises.forEach(balise -> {
                balise.put("customer", balise.get("customer_name"));
                balise.put("assigned", balise.get("customer_id") != null);
            });
            
            return ResponseEntity.ok(balises);
            
        } catch (Exception e) {
            logger.error("Error getting balises: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get balise statistics for dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBaliseStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Total balises
            Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises", Integer.class);
            stats.put("total", total);
            
            // Active balises (last seen within 1 hour)
            Integer active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises WHERE last_seen > NOW() - INTERVAL '1 hour'", 
                Integer.class);
            stats.put("active", active);
            
            // CMA-CGM assigned balises
            Integer cmacgm = jdbcTemplate.queryForObject(
                """SELECT COUNT(*) FROM balise_customer_assignments bca
                   JOIN customers c ON bca.customer_id = c.id
                   WHERE c.code = 'CMACGM' AND bca.sync_enabled = true""", 
                Integer.class);
            stats.put("cmacgm", cmacgm);
            
            // Unassigned balises
            Integer unassigned = jdbcTemplate.queryForObject(
                """SELECT COUNT(*) FROM balises b
                   WHERE NOT EXISTS (
                       SELECT 1 FROM balise_customer_assignments bca 
                       WHERE bca.balise_id = b.id AND bca.sync_enabled = true
                   )""", 
                Integer.class);
            stats.put("unassigned", unassigned);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error getting balise stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign a balise to a customer
     */
    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignBalise(@RequestBody Map<String, Object> request) {
        try {
            Long baliseId = Long.valueOf(request.get("balise_id").toString());
            String customerCode = request.get("customer").toString();
            String notes = (String) request.get("notes");
            
            // Get or create customer
            Long customerId = getOrCreateCustomer(customerCode);
            
            // Check if balise is already assigned
            Integer existingAssignment = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balise_customer_assignments WHERE balise_id = ? AND sync_enabled = true",
                Integer.class, baliseId);
            
            if (existingAssignment > 0) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Balise is already assigned to a customer"));
            }
            
            // Create assignment
            String assignSql = """
                INSERT INTO balise_customer_assignments 
                (balise_id, customer_id, sync_enabled, sync_gps, sync_events, assigned_by, notes)
                VALUES (?, ?, true, true, true, 'system', ?)
                """;
            
            jdbcTemplate.update(assignSql, baliseId, customerId, notes);
            
            // Handle CMA-CGM specific data
            if ("CMACGM".equals(customerCode) && request.containsKey("cmacgm_data")) {
                saveCmaCgmData(baliseId, customerId, (Map<String, Object>) request.get("cmacgm_data"));
            }
            
            logger.info("Balise {} assigned to customer {}", baliseId, customerCode);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Balise assigned successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Error assigning balise: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to assign balise: " + e.getMessage()));
        }
    }

    /**
     * Unassign a balise from its current customer
     */
    @PostMapping("/{baliseId}/unassign")
    public ResponseEntity<Map<String, Object>> unassignBalise(@PathVariable Long baliseId) {
        try {
            // Disable current assignments
            jdbcTemplate.update(
                "UPDATE balise_customer_assignments SET sync_enabled = false WHERE balise_id = ?",
                baliseId);
            
            logger.info("Balise {} unassigned from customer", baliseId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Balise unassigned successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Error unassigning balise: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to unassign balise: " + e.getMessage()));
        }
    }

    /**
     * Get recent activity for dashboard
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        try {
            String sql = """
                SELECT b.name as balise_name, 
                       CASE 
                           WHEN be.event_type IS NOT NULL THEN CONCAT('Event: ', be.event_type)
                           ELSE 'GPS Update'
                       END as activity,
                       COALESCE(be.event_time, b.last_seen) as timestamp
                FROM balises b
                LEFT JOIN balise_events be ON b.id = be.balise_id 
                    AND be.event_time = (
                        SELECT MAX(event_time) FROM balise_events WHERE balise_id = b.id
                    )
                WHERE COALESCE(be.event_time, b.last_seen) > NOW() - INTERVAL '2 hours'
                ORDER BY timestamp DESC
                LIMIT 10
                """;
            
            List<Map<String, Object>> activities = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(activities);
            
        } catch (Exception e) {
            logger.error("Error getting recent activity: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get CMA-CGM specific data for a balise
     */
    @GetMapping("/{baliseId}/cmacgm-data")
    public ResponseEntity<Map<String, Object>> getCmaCgmData(@PathVariable Long baliseId) {
        try {
            String sql = """
                SELECT equipment_reference, carrier_booking_reference, transport_order,
                       mode_of_transport, partner_name, transportation_phase,
                       location_code, location_name, facility_address, facility_city
                FROM cmacgm_balise_data 
                WHERE balise_id = ?
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, baliseId);
            
            if (results.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error getting CMA-CGM data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Failed to get CMA-CGM data: " + e.getMessage()));
        }
    }

    /**
     * Get or create customer by code
     */
    private Long getOrCreateCustomer(String customerCode) {
        try {
            // Try to find existing customer
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(
                "SELECT id FROM customers WHERE code = ?", customerCode);
            
            if (!existing.isEmpty()) {
                return (Long) existing.get(0).get("id");
            }
            
            // Create new customer
            String customerName = getCustomerNameByCode(customerCode);
            jdbcTemplate.update(
                "INSERT INTO customers (name, code, sync_enabled) VALUES (?, ?, true)",
                customerName, customerCode);
            
            // Get the new customer ID
            List<Map<String, Object>> newCustomer = jdbcTemplate.queryForList(
                "SELECT id FROM customers WHERE code = ?", customerCode);
            
            return (Long) newCustomer.get(0).get("id");
            
        } catch (Exception e) {
            logger.error("Error getting/creating customer: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get/create customer", e);
        }
    }

    /**
     * Get customer display name by code
     */
    private String getCustomerNameByCode(String code) {
        switch (code) {
            case "CMACGM": return "CMA-CGM";
            case "DHL": return "DHL";
            case "MAERSK": return "Maersk";
            default: return code;
        }
    }

    /**
     * Save CMA-CGM specific data
     */
    private void saveCmaCgmData(Long baliseId, Long customerId, Map<String, Object> cmacgmData) {
        try {
            // Create table if it doesn't exist
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS cmacgm_balise_data (
                    id SERIAL PRIMARY KEY,
                    balise_id BIGINT NOT NULL,
                    customer_id BIGINT NOT NULL,
                    equipment_reference VARCHAR(255),
                    carrier_booking_reference VARCHAR(255),
                    transport_order VARCHAR(255),
                    mode_of_transport VARCHAR(50),
                    partner_name VARCHAR(255),
                    transportation_phase VARCHAR(50),
                    location_code VARCHAR(255),
                    location_name VARCHAR(255),
                    facility_address VARCHAR(500),
                    facility_city VARCHAR(255),
                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(balise_id, customer_id)
                )
                """);
            
            // Insert CMA-CGM data
            String sql = """
                INSERT INTO cmacgm_balise_data 
                (balise_id, customer_id, equipment_reference, carrier_booking_reference,
                 transport_order, mode_of_transport, partner_name, transportation_phase,
                 location_code, location_name, facility_address, facility_city)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (balise_id, customer_id) 
                DO UPDATE SET
                    equipment_reference = EXCLUDED.equipment_reference,
                    carrier_booking_reference = EXCLUDED.carrier_booking_reference,
                    transport_order = EXCLUDED.transport_order,
                    mode_of_transport = EXCLUDED.mode_of_transport,
                    partner_name = EXCLUDED.partner_name,
                    transportation_phase = EXCLUDED.transportation_phase,
                    location_code = EXCLUDED.location_code,
                    location_name = EXCLUDED.location_name,
                    facility_address = EXCLUDED.facility_address,
                    facility_city = EXCLUDED.facility_city
                """;
            
            jdbcTemplate.update(sql,
                baliseId, customerId,
                cmacgmData.get("equipmentReference"),
                cmacgmData.get("carrierBookingReference"),
                cmacgmData.get("transportOrder"),
                cmacgmData.get("modeOfTransport"),
                cmacgmData.get("partnerName"),
                cmacgmData.get("transportationPhase"),
                cmacgmData.get("locationCode"),
                cmacgmData.get("locationName"),
                cmacgmData.get("facilityAddress"),
                cmacgmData.get("facilityCity")
            );
            
            logger.info("CMA-CGM data saved for balise {}", baliseId);
            
        } catch (Exception e) {
            logger.error("Error saving CMA-CGM data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save CMA-CGM data", e);
        }
    }
}
