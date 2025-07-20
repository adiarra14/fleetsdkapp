/**
 * Fleet Monitor - Balise Management System
 * Backend API Controller
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This software and associated documentation files are the property
 * of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
 * 
 * Unauthorized copying, modification, distribution, or use of this software
 * is strictly prohibited without prior written consent from Ynnov.
 */

package com.maxvision.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * REST API Controller for Balise Management
 * Provides endpoints for the Fleet Monitor UI
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to connect
public class BaliseController {

    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";

    /**
     * Get all balises with their current status
     */
    @GetMapping("/balises")
    public ResponseEntity<Map<String, Object>> getAllBalises() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> balises = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT b.id, b.name, b.imei, b.type, b.status, b.battery_level, " +
                         "b.last_seen, b.created_at, " +
                         "ST_X(b.location::geometry) as longitude, " +
                         "ST_Y(b.location::geometry) as latitude, " +
                         "COUNT(e.id) as event_count " +
                         "FROM balises b " +
                         "LEFT JOIN balise_events e ON b.id = e.balise_id " +
                         "AND e.event_time > NOW() - INTERVAL '24 hours' " +
                         "GROUP BY b.id, b.name, b.imei, b.type, b.status, b.battery_level, " +
                         "b.last_seen, b.created_at, b.location " +
                         "ORDER BY b.last_seen DESC NULLS LAST";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    Map<String, Object> balise = new HashMap<>();
                    balise.put("id", rs.getInt("id"));
                    balise.put("name", rs.getString("name"));
                    balise.put("imei", rs.getString("imei"));
                    balise.put("type", rs.getString("type"));
                    balise.put("status", rs.getString("status"));
                    balise.put("batteryLevel", rs.getDouble("battery_level"));
                    balise.put("lastSeen", rs.getTimestamp("last_seen"));
                    balise.put("createdAt", rs.getTimestamp("created_at"));
                    balise.put("longitude", rs.getDouble("longitude"));
                    balise.put("latitude", rs.getDouble("latitude"));
                    balise.put("eventCount", rs.getInt("event_count"));
                    
                    balises.add(balise);
                }
            }
            
            response.put("balises", balises);
            response.put("count", balises.size());
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("balises", balises);
            response.put("count", 0);
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get specific balise details
     */
    @GetMapping("/balises/{id}")
    public ResponseEntity<Map<String, Object>> getBalise(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT b.id, b.name, b.imei, b.type, b.status, b.battery_level, " +
                         "b.last_seen, b.created_at, " +
                         "ST_X(b.location::geometry) as longitude, " +
                         "ST_Y(b.location::geometry) as latitude " +
                         "FROM balises b WHERE b.id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        response.put("id", rs.getInt("id"));
                        response.put("name", rs.getString("name"));
                        response.put("imei", rs.getString("imei"));
                        response.put("type", rs.getString("type"));
                        response.put("status", rs.getString("status"));
                        response.put("batteryLevel", rs.getDouble("battery_level"));
                        response.put("lastSeen", rs.getTimestamp("last_seen"));
                        response.put("createdAt", rs.getTimestamp("created_at"));
                        response.put("longitude", rs.getDouble("longitude"));
                        response.put("latitude", rs.getDouble("latitude"));
                        response.put("status", "success");
                    } else {
                        response.put("error", "Balise not found");
                        response.put("status", "not_found");
                        return ResponseEntity.notFound().build();
                    }
                }
            }
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recent events for a balise
     */
    @GetMapping("/balises/{id}/events")
    public ResponseEntity<Map<String, Object>> getBaliseEvents(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> events = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT e.id, e.event_type, e.event_time, e.battery_level, e.speed, " +
                         "e.heading, e.message_raw, e.payload, " +
                         "ST_X(e.location::geometry) as longitude, " +
                         "ST_Y(e.location::geometry) as latitude " +
                         "FROM balise_events e " +
                         "WHERE e.balise_id = ? " +
                         "ORDER BY e.event_time DESC " +
                         "LIMIT 50";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", rs.getInt("id"));
                        event.put("eventType", rs.getString("event_type"));
                        event.put("eventTime", rs.getTimestamp("event_time"));
                        event.put("batteryLevel", rs.getDouble("battery_level"));
                        event.put("speed", rs.getDouble("speed"));
                        event.put("heading", rs.getDouble("heading"));
                        event.put("messageRaw", rs.getString("message_raw"));
                        event.put("payload", rs.getString("payload"));
                        event.put("longitude", rs.getDouble("longitude"));
                        event.put("latitude", rs.getDouble("latitude"));
                        
                        events.add(event);
                    }
                }
            }
            
            response.put("events", events);
            response.put("count", events.size());
            response.put("baliseId", id);
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("events", events);
            response.put("count", 0);
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Send command to balise
     */
    @PostMapping("/balises/{id}/command")
    public ResponseEntity<Map<String, Object>> sendCommand(
            @PathVariable int id, 
            @RequestBody Map<String, Object> commandData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: Integrate with LockSettingService to send actual commands
            String commandType = (String) commandData.get("command");
            
            response.put("baliseId", id);
            response.put("command", commandType);
            response.put("status", "queued");
            response.put("message", "Command queued for processing");
            response.put("timestamp", LocalDateTime.now());
            
            // Log the command for now
            System.out.println("Command received for balise " + id + ": " + commandType);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get system status and statistics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get balise counts by status
            String sql = "SELECT status, COUNT(*) as count FROM balises GROUP BY status";
            Map<String, Integer> statusCounts = new HashMap<>();
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statusCounts.put(rs.getString("status"), rs.getInt("count"));
                }
            }
            
            // Get total event count for last 24 hours
            sql = "SELECT COUNT(*) as count FROM balise_events WHERE event_time > NOW() - INTERVAL '24 hours'";
            int recentEvents = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    recentEvents = rs.getInt("count");
                }
            }
            
            response.put("baliseStatusCounts", statusCounts);
            response.put("recentEvents", recentEvents);
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "success");
            response.put("databaseConnected", true);
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
            response.put("databaseConnected", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
