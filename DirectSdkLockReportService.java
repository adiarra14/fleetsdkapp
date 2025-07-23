package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Direct LockReportService implementation that bypasses Spring entirely
 * and connects directly to PostgreSQL for maximum reliability.
 */
public class DirectSdkLockReportService implements LockReportService {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balise_management";
    private static final String DB_USER = "balise_user";
    private static final String DB_PASS = "balise_password123";
    
    static {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            System.out.println("=== DIRECT SDK: PostgreSQL driver loaded ===");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: PostgreSQL driver not found: " + e.getMessage());
        }
    }
    
    @Override
    public void reportLockMsg(String jsonStr) {
        System.out.println("=== DIRECT SDK MESSAGE RECEIVED ===");
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("JSON Length: " + (jsonStr != null ? jsonStr.length() : 0));
        System.out.println("JSON: " + jsonStr);
        
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.err.println("ERROR: Empty or null JSON message received");
            return;
        }
        
        Connection conn = null;
        try {
            // Get direct database connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("SUCCESS: Direct database connection established");
            
            // Parse the JSON message from SDK
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // Extract device information
            String deviceId = extractDeviceId(jsonNode);
            String messageType = extractMessageType(jsonNode);
            
            System.out.println("Extracted Device ID: " + deviceId);
            System.out.println("Extracted Message Type: " + messageType);
            
            // Ensure balise exists in database
            ensureBaliseExists(deviceId, conn);
            
            // Store the event
            storeBaliseEvent(deviceId, jsonStr, messageType, conn);
            
            System.out.println("SUCCESS: DIRECT SDK: Successfully stored message for device: " + deviceId);
            
        } catch (Exception e) {
            System.err.println("ERROR in DirectSdkLockReportService: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    System.err.println("Error closing database connection: " + e.getMessage());
                }
            }
        }
    }
    
    private String extractDeviceId(JsonNode jsonNode) {
        // Try to extract device ID from various possible fields
        if (jsonNode.has("deviceId")) {
            return jsonNode.get("deviceId").asText();
        }
        if (jsonNode.has("device_id")) {
            return jsonNode.get("device_id").asText();
        }
        if (jsonNode.has("lockId")) {
            return jsonNode.get("lockId").asText();
        }
        if (jsonNode.has("imei")) {
            return jsonNode.get("imei").asText();
        }
        if (jsonNode.has("id")) {
            return jsonNode.get("id").asText();
        }
        
        // Try to extract from nested objects
        if (jsonNode.has("data")) {
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode.has("deviceId")) {
                return dataNode.get("deviceId").asText();
            }
            if (dataNode.has("imei")) {
                return dataNode.get("imei").asText();
            }
        }
        
        // Fallback: generate ID from message content hash
        String fallbackId = "DIRECT-SDK-" + Math.abs(jsonStr.hashCode());
        System.out.println("WARNING: Using fallback device ID: " + fallbackId);
        return fallbackId;
    }
    
    private String extractMessageType(JsonNode jsonNode) {
        if (jsonNode.has("messageType")) {
            return jsonNode.get("messageType").asText();
        }
        if (jsonNode.has("commandType")) {
            return jsonNode.get("commandType").asText();
        }
        if (jsonNode.has("type")) {
            return jsonNode.get("type").asText();
        }
        return "DIRECT_SDK_PARSED";
    }
    
    private void ensureBaliseExists(String deviceId, Connection conn) throws Exception {
        // Check if balise exists
        String checkSql = "SELECT COUNT(*) FROM balises WHERE device_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, deviceId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            
            if (count == 0) {
                // Create new balise entry
                String insertSql = "INSERT INTO balises (device_id, created_at, updated_at, last_seen) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    insertStmt.setString(1, deviceId);
                    insertStmt.setTimestamp(2, now);
                    insertStmt.setTimestamp(3, now);
                    insertStmt.setTimestamp(4, now);
                    insertStmt.executeUpdate();
                    System.out.println("INFO: DIRECT SDK: Auto-created balise entry: " + deviceId);
                }
            } else {
                // Update last_seen
                String updateSql = "UPDATE balises SET last_seen = ?, updated_at = ? WHERE device_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                    updateStmt.setTimestamp(1, now);
                    updateStmt.setTimestamp(2, now);
                    updateStmt.setString(3, deviceId);
                    updateStmt.executeUpdate();
                    System.out.println("INFO: DIRECT SDK: Updated last_seen for: " + deviceId);
                }
            }
        }
    }
    
    private void storeBaliseEvent(String deviceId, String jsonData, String messageType, Connection conn) throws Exception {
        String insertSql = "INSERT INTO balise_events (device_id, event_type, raw_data, event_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, deviceId);
            stmt.setString(2, messageType);
            stmt.setString(3, jsonData);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            int rows = stmt.executeUpdate();
            System.out.println("INFO: DIRECT SDK: Stored event (" + rows + " rows affected)");
        }
    }
}
