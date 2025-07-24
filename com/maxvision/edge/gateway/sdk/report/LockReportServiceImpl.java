package com.maxvision.edge.gateway.sdk.report;

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
        System.out.println("=== REAL BALISE DATA RECEIVED ===");
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("JSON Data: " + jsonStr);
        System.out.println("JdbcTemplate available: " + (jdbcTemplate != null));

        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.err.println("ERROR: Empty JSON received");
            return;
        }

        try {
            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            String deviceId = extractDeviceId(jsonNode);
            String messageType = extractMessageType(jsonNode);

            if (jdbcTemplate != null) {
                // Ensure device exists
                ensureBaliseExists(deviceId);
                // Store event
                storeBaliseEvent(deviceId, jsonStr, messageType);
                System.out.println("✅ Data stored successfully for device: " + deviceId);
            } else {
                System.out.println("⚠️ JdbcTemplate not available - data logged only");
            }

        } catch (Exception e) {
            System.err.println("❌ Error processing balise data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractDeviceId(JsonNode jsonNode) {
        if (jsonNode.has("deviceId")) {
            return jsonNode.get("deviceId").asText();
        }
        return "UNKNOWN";
    }

    private String extractMessageType(JsonNode jsonNode) {
        if (jsonNode.has("messageType")) {
            return jsonNode.get("messageType").asText();
        }
        return "STATUS_REPORT";
    }

    private void ensureBaliseExists(String deviceId) {
        try {
            String checkSql = "SELECT COUNT(*) FROM balises WHERE device_id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, deviceId);
            
            if (count == 0) {
                String insertSql = "INSERT INTO balises (device_id, name, status, last_seen) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, deviceId, deviceId + "-LOCK", "ONLINE", new Timestamp(System.currentTimeMillis()));
                System.out.println("✅ Created new balise record for: " + deviceId);
            }
        } catch (Exception e) {
            System.err.println("Error ensuring balise exists: " + e.getMessage());
        }
    }

    private void storeBaliseEvent(String deviceId, String jsonStr, String messageType) {
        try {
            String sql = "INSERT INTO balise_events (balise_id, event_type, event_time, raw_data, parsed_data) " +
                        "VALUES ((SELECT id FROM balises WHERE device_id = ?), ?, ?, ?, ?::jsonb)";
            
            jdbcTemplate.update(sql, 
                deviceId, 
                messageType, 
                new Timestamp(System.currentTimeMillis()),
                "SDK_PROCESSED_DATA",
                jsonStr
            );
            System.out.println("✅ Event stored for device: " + deviceId);
        } catch (Exception e) {
            System.err.println("Error storing balise event: " + e.getMessage());
        }
    }
}
