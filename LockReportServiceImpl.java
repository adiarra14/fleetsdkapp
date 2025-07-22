package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class LockReportServiceImpl implements LockReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void reportLockMsg(String jsonStr) {
        System.out.println("=== SDK PARSED MESSAGE RECEIVED ===");
        System.out.println("JSON: " + jsonStr);
        
        try {
            // Parse the JSON message from SDK
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // Extract device information
            String deviceId = extractDeviceId(jsonNode);
            String messageType = extractMessageType(jsonNode);
            
            // Ensure balise exists in database
            ensureBaliseExists(deviceId);
            
            // Store the event
            storeBaliseEvent(deviceId, jsonStr, messageType);
            
            System.out.println("✅ Successfully stored SDK parsed message for device: " + deviceId);
            
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
                System.out.println("🔧 Auto-created balise entry: " + deviceId);
            } else {
                // Update last_seen
                jdbcTemplate.update(
                    "UPDATE balises SET last_seen = ?, updated_at = ? WHERE device_id = ?",
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    deviceId
                );
            }
        } catch (Exception e) {
            System.err.println("Error ensuring balise exists: " + e.getMessage());
        }
    }
    
    private void storeBaliseEvent(String deviceId, String jsonData, String messageType) {
        try {
            jdbcTemplate.update(
                "INSERT INTO balise_events (device_id, event_type, event_data, received_at) VALUES (?, ?, ?::jsonb, ?)",
                deviceId,
                messageType,
                jsonData,
                Timestamp.valueOf(LocalDateTime.now())
            );
        } catch (Exception e) {
            System.err.println("Error storing balise event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
