package com.maxvision.tcpserver.service;

import com.maxvision.edge.gateway.service.LockReportService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Implementation of LockReportService to handle incoming messages from balise devices.
 * This service processes JSON reports sent by balises and stores them in the database.
 */
@Service
@Slf4j
public class BaliseReportServiceImpl implements LockReportService {

    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void reportLockMsg(String jsonStr) {
        try {
            log.info("Received balise report message: {}", jsonStr);
            
            // Parse the JSON message
            Map<String, Object> reportData = objectMapper.readValue(jsonStr, Map.class);
            
            // Extract the lockCode (balise ID)
            String baliseId = (String) reportData.get("lockCode");
            if (baliseId == null || baliseId.isEmpty()) {
                log.error("Invalid balise report: missing balise ID/lockCode");
                return;
            }
            
            // Process data based on message type
            if (reportData.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) reportData.get("data");
                
                // Check if this is a GPS report
                if (data.containsKey("type") && "gps".equals(data.get("type"))) {
                    processGpsReport(baliseId, data);
                    return;
                }
                
                // Check if this is a login message (contains lockGpsResModel)
                if (data.containsKey("lockGpsResModel")) {
                    processLoginReport(baliseId, data);
                    return;
                }
                
                // Check if this is a keep-alive message
                if (data.containsKey("networkValue") && data.containsKey("voltage")) {
                    processKeepAliveReport(baliseId, data);
                    return;
                }
                
                // Check if this is an NFC info report
                if (data.containsKey("universalNfcList")) {
                    processNfcReport(baliseId, data);
                    return;
                }
            }
            
            // If we reached here, it's an unknown message type - store as raw data
            storeRawBaliseData(baliseId, jsonStr);
            
        } catch (Exception e) {
            log.error("Error processing balise report: {}", e.getMessage(), e);
        }
    }
    
    private void processGpsReport(String baliseId, Map<String, Object> data) {
        try {
            log.info("Processing GPS report from balise {}", baliseId);
            
            // Extract GPS data
            String gpsTime = String.valueOf(data.getOrDefault("gpsTime", ""));
            String speed = String.valueOf(data.getOrDefault("speed", "0"));
            String direction = String.valueOf(data.getOrDefault("direction", "0"));
            String voltagePercentage = String.valueOf(data.getOrDefault("voltagePercentage", "0"));
            String lockStatus = String.valueOf(data.getOrDefault("lockStatus", "unknown"));
            
            // Extract terminal status flags
            List<String> terminalStatus = (List<String>) data.getOrDefault("terminalStatus", new ArrayList<>());
            List<String> warningMessages = (List<String>) data.getOrDefault("warningMessage", new ArrayList<>());
            
            // Extract GPS coordinates
            Map<String, Object> gps = null;
            double lat = 0.0;
            double lon = 0.0;
            boolean gpsValid = Boolean.valueOf(String.valueOf(data.getOrDefault("gpsValid", "false")));
            
            if (data.containsKey("gps")) {
                gps = (Map<String, Object>) data.get("gps");
                if (gps != null) {
                    lat = Double.parseDouble(String.valueOf(gps.getOrDefault("lat", "0")));
                    lon = Double.parseDouble(String.valueOf(gps.getOrDefault("lng", "0")));
                }
            }
            
            // Store balise data in the database
            updateBaliseRecord(baliseId, lockStatus, Double.parseDouble(voltagePercentage), lat, lon);
            
            // Store GPS event
            double speedValue = Double.parseDouble(speed);
            double headingValue = Double.parseDouble(direction);
            storeBaliseEvent(baliseId, "GPS", lat, lon, speedValue, headingValue, objectMapper.writeValueAsString(data));
            
            // Process any alarms/warnings
            for (String warning : warningMessages) {
                storeBaliseEvent(baliseId, "ALARM:" + warning, lat, lon, speedValue, headingValue, warning);
            }
            
            log.info("Processed GPS report from balise {}: location={},{} status={} battery={}%", 
                    baliseId, lat, lon, lockStatus, voltagePercentage);
            
        } catch (Exception e) {
            log.error("Error processing GPS report: {}", e.getMessage(), e);
        }
    }
    
    private void processLoginReport(String baliseId, Map<String, Object> data) {
        try {
            log.info("Processing login report from balise {}", baliseId);
            
            String gpsInterval = String.valueOf(data.getOrDefault("gpsUploadInterval", ""));
            String version = String.valueOf(data.getOrDefault("version", ""));
            String deviceMode = String.valueOf(data.getOrDefault("deviceMode", ""));
            
            // Handle GPS data within login message
            if (data.containsKey("lockGpsResModel")) {
                Map<String, Object> gpsModel = (Map<String, Object>) data.get("lockGpsResModel");
                
                // Extract GPS data
                double lat = 0.0;
                double lon = 0.0;
                String voltage = String.valueOf(gpsModel.getOrDefault("voltage", "0"));
                String lockStatus = String.valueOf(gpsModel.getOrDefault("lockStatus", "unknown"));
                
                // Extract coordinates if available
                if (gpsModel.containsKey("gps")) {
                    Map<String, Object> gps = (Map<String, Object>) gpsModel.get("gps");
                    lat = Double.parseDouble(String.valueOf(gps.getOrDefault("lat", "0")));
                    lon = Double.parseDouble(String.valueOf(gps.getOrDefault("lng", "0")));
                }
                
                // Update balise record
                updateBaliseRecord(baliseId, lockStatus, Double.parseDouble(voltage), lat, lon);
                
                // Store login event
                storeBaliseEvent(baliseId, "LOGIN", lat, lon, 0, 0, objectMapper.writeValueAsString(data));
                
                // Process any alarms/warnings
                List<String> warnings = (List<String>) gpsModel.getOrDefault("warningMessageList", new ArrayList<>());
                for (String warning : warnings) {
                    storeBaliseEvent(baliseId, "ALARM:" + warning, lat, lon, 0, 0, warning);
                }
                
                log.info("Processed login report from balise {}: version={} mode={} interval={}s", 
                        baliseId, version, deviceMode, gpsInterval);
            }
            
        } catch (Exception e) {
            log.error("Error processing login report: {}", e.getMessage(), e);
        }
    }
    
    private void processKeepAliveReport(String baliseId, Map<String, Object> data) {
        try {
            log.info("Processing keep-alive report from balise {}", baliseId);
            
            String networkValue = String.valueOf(data.getOrDefault("networkValue", ""));
            String voltage = String.valueOf(data.getOrDefault("voltage", ""));
            
            // Update balise record with battery level
            updateBaliseRecord(baliseId, "ONLINE", Double.parseDouble(voltage), 0, 0);
            
            // Store keep-alive event
            storeBaliseEvent(baliseId, "KEEP_ALIVE", 0, 0, 0, 0, objectMapper.writeValueAsString(data));
            
            log.info("Processed keep-alive report from balise {}: network={} battery={}%", 
                    baliseId, networkValue, voltage);
            
        } catch (Exception e) {
            log.error("Error processing keep-alive report: {}", e.getMessage(), e);
        }
    }
    
    private void processNfcReport(String baliseId, Map<String, Object> data) {
        try {
            log.info("Processing NFC report from balise {}", baliseId);
            
            List<Map<String, Object>> nfcCards = (List<Map<String, Object>>) data.get("universalNfcList");
            if (nfcCards != null && !nfcCards.isEmpty()) {
                // Store NFC event
                storeBaliseEvent(baliseId, "NFC_INFO", 0, 0, 0, 0, objectMapper.writeValueAsString(data));
                
                log.info("Processed NFC report from balise {}: {} cards", baliseId, nfcCards.size());
            }
            
        } catch (Exception e) {
            log.error("Error processing NFC report: {}", e.getMessage(), e);
        }
    }
    
    private void updateBaliseRecord(String baliseId, String status, double battery, double lat, double lon) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO balises (imei, name, status, battery_level, last_seen, location) " +
                        "VALUES (?, ?, ?, ?, NOW(), ST_SetSRID(ST_MakePoint(?, ?), 4326)) " +
                        "ON CONFLICT (imei) DO UPDATE SET " +
                        "status = EXCLUDED.status, battery_level = EXCLUDED.battery_level, " +
                        "last_seen = EXCLUDED.last_seen, location = EXCLUDED.location";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, baliseId);
                stmt.setString(2, "Balise-" + baliseId.substring(Math.max(0, baliseId.length() - 4)));
                stmt.setString(3, status);
                stmt.setDouble(4, battery);
                stmt.setDouble(5, lon);
                stmt.setDouble(6, lat);
                
                int updated = stmt.executeUpdate();
                log.debug("Updated balise record for ID {}: {} rows", baliseId, updated);
            }
        } catch (SQLException e) {
            log.error("Error updating balise record: {}", e.getMessage(), e);
        }
    }
    
    private void storeBaliseEvent(String baliseId, String eventType, double lat, double lon, 
                                  double speed, double heading, String rawMessage) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // First, get or create balise ID
            Integer baliseDbId = getBaliseId(conn, baliseId);
            if (baliseDbId == null) {
                log.error("Could not find/create balise for ID: {}", baliseId);
                return;
            }
            
            String sql = "INSERT INTO balise_events (balise_id, event_type, location, speed, heading, message_raw) " +
                        "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, baliseDbId);
                stmt.setString(2, eventType);
                stmt.setDouble(3, lon);
                stmt.setDouble(4, lat);
                stmt.setDouble(5, speed);
                stmt.setDouble(6, heading);
                stmt.setString(7, rawMessage);
                
                int inserted = stmt.executeUpdate();
                log.debug("Stored balise event for {}: {} (event type: {})", baliseId, inserted, eventType);
            }
        } catch (SQLException e) {
            log.error("Error storing balise event: {}", e.getMessage(), e);
        }
    }
    
    private void storeRawBaliseData(String baliseId, String data) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Store as raw event
            Integer baliseDbId = getBaliseId(conn, baliseId);
            if (baliseDbId == null) return;
            
            String sql = "INSERT INTO balise_events (balise_id, event_type, message_raw) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, baliseDbId);
                stmt.setString(2, "RAW");
                stmt.setString(3, data);
                
                stmt.executeUpdate();
                log.info("Stored raw data from balise {}", baliseId);
            }
        } catch (SQLException e) {
            log.error("Error storing raw data: {}", e.getMessage(), e);
        }
    }
    
    private Integer getBaliseId(Connection conn, String baliseId) throws SQLException {
        // Try to find existing balise by ID/IMEI
        String selectSql = "SELECT id FROM balises WHERE imei = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, baliseId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Create new balise if not found
        String insertSql = "INSERT INTO balises (imei, name, status, created_at) VALUES (?, ?, ?, NOW()) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, baliseId);
            stmt.setString(2, "Balise-" + baliseId.substring(Math.max(0, baliseId.length() - 4)));
            stmt.setString(3, "NEW");
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    log.info("Created new balise record with ID: {} for balise {}", newId, baliseId);
                    return newId;
                }
            }
        }
        
        return null;
    }
}
