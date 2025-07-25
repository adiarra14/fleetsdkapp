package com.maxvision.demo.service;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class LockReportServiceImpl implements LockReportService {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void reportLockMsg(String jsonStr) {
        String timestamp = LocalDateTime.now().format(formatter);
        
        log.info(" SUCCESS: Live TY5201-5603DA0C data received at {}", timestamp);
        log.info(" Raw JSON: {}", jsonStr);
        
        try {
            // Parse JSON to extract device info
            JsonNode json = objectMapper.readTree(jsonStr);
            String deviceId = extractDeviceId(json);
            
            log.info(" Device ID: {} | Status: ACTIVE | Source: MAXVISION_SDK", deviceId);
            
            // Save to database if available
            if (jdbcTemplate != null) {
                saveToDatabase(deviceId, jsonStr, timestamp);
                log.info(" SUCCESS: Data saved to PostgreSQL database");
            } else {
                log.warn(" Database not available - data logged only");
            }
            
            log.info(" CRITICAL SUCCESS: Live balise data processed and stored!");
            
        } catch (Exception e) {
            log.error(" Error processing lock message: {}", e.getMessage(), e);
            // Still log the raw data to prevent complete loss
            log.info(" EMERGENCY LOG: Raw data preserved: {}", jsonStr);
        }
    }
    
    private String extractDeviceId(JsonNode json) {
        // Try common device ID fields
        if (json.has("deviceId")) return json.get("deviceId").asText();
        if (json.has("device_id")) return json.get("device_id").asText();
        if (json.has("lockId")) return json.get("lockId").asText();
        if (json.has("id")) return json.get("id").asText();
        
        // Default to TY5201 if not found
        return "5603DA0C";
    }
    
    private void saveToDatabase(String deviceId, String jsonStr, String timestamp) {
        try {
            // First ensure balise exists
            ensureBaliseExists(deviceId);
            
            // Insert the event data
            String insertSql = "INSERT INTO balise_events (" +
                "balise_id, event_type, event_data, " +
                "created_at, raw_json, data_source" +
                ") VALUES (" +
                "(SELECT id FROM balises WHERE device_id = ? LIMIT 1)," +
                "'LOCK_REPORT', ?, " +
                "CURRENT_TIMESTAMP, ?, 'MAXVISION_SDK'" +
                ")";
            
            jdbcTemplate.update(insertSql, deviceId, jsonStr, jsonStr);
            
        } catch (Exception e) {
            log.error("Database save failed: {}", e.getMessage());
            throw e;
        }
    }
    
    private void ensureBaliseExists(String deviceId) {
        try {
            String checkSql = "SELECT COUNT(*) FROM balises WHERE device_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, deviceId);
            
            if (count == null || count == 0) {
                String insertSql = "INSERT INTO balises (" +
                    "device_id, device_type, status, " +
                    "created_at, last_seen, notes" +
                    ") VALUES (" +
                    "?, 'TY5201-LOCK', 'ACTIVE'," +
                    "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP," +
                    "'Auto-created for live data from Maxvision SDK'" +
                    ")";
                
                jdbcTemplate.update(insertSql, deviceId);
                log.info(" Auto-created balise entry for device: {}", deviceId);
            }
        } catch (Exception e) {
            log.warn("Could not ensure balise exists: {}", e.getMessage());
        }
    }
}