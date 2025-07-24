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
        System.out.println("JdbcTemplate available: " + (jdbcTemplate != null));

        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.err.println("ERROR: Empty or null JSON message received");
            return;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            String deviceId = extractDeviceId(jsonNode);
            String messageType = extractMessageType(jsonNode);

            if (jdbcTemplate != null) {
                ensureBaliseExists(deviceId);
                storeBaliseEvent(deviceId, jsonStr, messageType);
            } else {
                System.out.println("WARNING: JdbcTemplate not available – logging only");
            }
        } catch (Exception e) {
            System.err.println("Error processing SDK parsed message: " + e.getMessage());
        }
    }

    private String extractDeviceId(JsonNode jsonNode) {
        if (jsonNode.has("deviceId")) return jsonNode.get("deviceId").asText();
        if (jsonNode.has("imei")) return jsonNode.get("imei").asText();
        return "SDK-DEVICE-" + System.currentTimeMillis();
    }

    private String extractMessageType(JsonNode jsonNode) {
        if (jsonNode.has("messageType")) return jsonNode.get("messageType").asText();
        return "SDK_PARSED";
    }

    private void ensureBaliseExists(String deviceId) {
        if (jdbcTemplate == null) return;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM balises WHERE device_id = ?", Integer.class, deviceId);
        if (count == null || count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO balises (name, device_id, status, last_seen, creation_date) VALUES (?, ?, 'ACTIVE', now(), now())",
                    "AUTO-" + deviceId, deviceId);
        }
    }

    private void storeBaliseEvent(String deviceId, String jsonData, String messageType) {
        if (jdbcTemplate == null) return;
        Integer baliseId = jdbcTemplate.queryForObject(
                "SELECT id FROM balises WHERE device_id = ?", Integer.class, deviceId);
        jdbcTemplate.update(
                "INSERT INTO balise_events (balise_id, event_type, event_time, raw_data) VALUES (?, ?, now(), ?)",
                baliseId, messageType, jsonData);
    }
}
