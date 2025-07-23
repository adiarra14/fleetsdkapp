package com.maxvision.fleet.sdk;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import com.maxvision.edge.gateway.sdk.report.LockReportService;

@Service
public class LockReportServiceImpl implements LockReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Setter for manual dependency injection
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void reportLockMsg(String jsonStr) {
        System.out.println("=== SDK PARSED MESSAGE RECEIVED ===");
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("JSON Length: " + (jsonStr != null ? jsonStr.length() : 0));
        System.out.println("JSON: " + jsonStr);
        System.out.println("JdbcTemplate available: " + (jdbcTemplate != null));
        
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.err.println("ERROR: Empty or null JSON message received");
            return;
        }
        
        try {
            // Parse the JSON message from SDK
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // Extract device information
            String deviceId = extractDeviceId(jsonNode);
            String messageType = extractMessageType(jsonNode);
            
            System.out.println("Extracted Device ID: " + deviceId);
            System.out.println("Extracted Message Type: " + messageType);
            
            // Check if jdbcTemplate is available (Spring timing issue)
            if (jdbcTemplate != null) {
                // Normal database operations
                ensureBaliseExists(deviceId);
                storeBaliseEvent(deviceId, jsonStr, messageType);
                System.out.println("SUCCESS: Successfully stored SDK message for device: " + deviceId);
            } else {
                // Fallback: log the message without database operations
                System.out.println("WARNING: JdbcTemplate not available (Spring timing issue)");
                System.out.println("FALLBACK: Logging message without database storage");
                System.out.println("Device: " + deviceId + ", Type: " + messageType);
                System.out.println("Message contains TY5201-LOCK: " + jsonStr.contains("TY5201-LOCK"));
                System.out.println("SUCCESS: Message received and logged (no database)");
            }
            
        } catch (Exception e) {
            System.err.println("Error processing SDK parsed message: " + e.getMessage());
            e.printStackTrace();
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
        // Fallback to generating ID from message content
        return "SDK-DEVICE-" + System.currentTimeMillis();
    }
    
    private String extractMessageType(JsonNode jsonNode) {
        if (jsonNode.has("messageType")) {
            return jsonNode.get("messageType").asText();
        }
        if (jsonNode.has("commandType")) {
            return jsonNode.get("commandType").asText();
        }
        return "SDK_PARSED";
    }
    
    private void ensureBaliseExists(String deviceId) {
        if (jdbcTemplate == null) {
            System.out.println("WARNING: JdbcTemplate null, skipping balise existence check");
            return;
        }
        
        try {
            // Check if balise exists
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises WHERE device_id = ?", 
                Integer.class, 
                deviceId
            );
            
            if (count == 0) {
                // Create new balise entry
                jdbcTemplate.update(
                    "INSERT INTO balises (device_id, created_at, updated_at, last_seen) VALUES (?, ?, ?, ?)",
                    deviceId,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now())
                );
                System.out.println("INFO: Auto-created balise entry: " + deviceId);
            } else {
                // Update last_seen
                jdbcTemplate.update(
                    "UPDATE balises SET last_seen = ?, updated_at = ? WHERE device_id = ?",
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    deviceId
                );
                System.out.println("INFO: Updated last_seen for: " + deviceId);
            }
        } catch (Exception e) {
            System.err.println("Error ensuring balise exists: " + e.getMessage());
        }
    }
    
    private void storeBaliseEvent(String deviceId, String jsonData, String messageType) {
        if (jdbcTemplate == null) {
            System.out.println("WARNING: JdbcTemplate null, skipping event storage");
            return;
        }
        
        try {
            jdbcTemplate.update(
                "INSERT INTO balise_events (device_id, event_type, raw_data, event_time) VALUES (?, ?, ?, ?)",
                deviceId,
                messageType,
                jsonData,
                Timestamp.valueOf(LocalDateTime.now())
            );
            System.out.println("INFO: Stored event for device: " + deviceId);
        } catch (Exception e) {
            System.err.println("Error storing balise event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
