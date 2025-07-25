package com.maxvision.edge.gateway.sdk.report;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Internal LockReportService implementation that works within SDK architecture
 * No external dependencies - uses direct JDBC connections
 */
public class InternalLockReportService implements LockReportService {
    
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public InternalLockReportService() {
        System.out.println("🔧 INTERNAL LOCK REPORT SERVICE CREATED");
        System.out.println("✅ Using direct JDBC connections (no Spring dependencies)");
        initializeDatabase();
    }
    
    @Override
    public void reportLockMsg(String jsonStr) {
        try {
            System.out.println("📡 INTERNAL SERVICE: Processing balise message");
            System.out.println("📝 JSON: " + jsonStr);
            
            // Parse JSON using internal ObjectMapper
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // Extract key information
            String deviceId = jsonNode.has("deviceId") ? jsonNode.get("deviceId").asText() : "UNKNOWN";
            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "UNKNOWN";
            
            System.out.println("🔍 Device ID: " + deviceId);
            System.out.println("🔍 Message Type: " + messageType);
            
            // Store in database using direct JDBC
            storeBaliseData(deviceId, messageType, jsonStr);
            
            System.out.println("✅ INTERNAL SERVICE: Data successfully stored!");
            
        } catch (Exception e) {
            System.err.println("❌ INTERNAL SERVICE ERROR: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: Log to console for debugging
            logFallback(jsonStr, e);
        }
    }
    
    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("🔗 Testing database connection...");
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Database connection failed: " + e.getMessage());
            System.out.println("💡 Will continue with console logging");
        }
    }
    
    private Connection getConnection() throws Exception {
        // Load PostgreSQL driver
        Class.forName("org.postgresql.Driver");
        
        // Create connection with timeout
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    private void storeBaliseData(String deviceId, String messageType, String jsonData) {
        try (Connection conn = getConnection()) {
            
            // First, ensure device exists
            ensureDeviceExists(conn, deviceId);
            
            // Then store the event
            storeBaliseEvent(conn, deviceId, messageType, jsonData);
            
        } catch (Exception e) {
            System.err.println("❌ Database storage failed: " + e.getMessage());
            throw new RuntimeException("Failed to store balise data", e);
        }
    }
    
    private void ensureDeviceExists(Connection conn, String deviceId) throws Exception {
        String checkSql = "SELECT id FROM balises WHERE device_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setString(1, deviceId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                // Device doesn't exist, create it
                String insertSql = "INSERT INTO balises (device_id, name, status, battery_level, last_seen, created_at, updated_at) " +
                                   "VALUES (?, ?, 'ONLINE', 50, NOW(), NOW(), NOW())";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, deviceId);
                    insertStmt.setString(2, deviceId + "-DEVICE");
                    insertStmt.executeUpdate();
                    System.out.println("✅ Created new device: " + deviceId);
                }
            }
        }
    }
    
    private void storeBaliseEvent(Connection conn, String deviceId, String messageType, String jsonData) throws Exception {
        String sql = "INSERT INTO balise_events (balise_id, event_type, event_data, raw_data, created_at) " +
                     "VALUES ((SELECT id FROM balises WHERE device_id = ?), ?, ?::jsonb, ?, NOW())";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deviceId);
            stmt.setString(2, messageType);
            stmt.setString(3, jsonData);
            stmt.setString(4, jsonData);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Stored event: " + rowsAffected + " row(s) affected");
        }
    }
    
    private void logFallback(String jsonData, Exception error) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.println("=== FALLBACK LOG ===");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("JSON Data: " + jsonData);
        System.out.println("Error: " + error.getMessage());
        System.out.println("==================");
    }
}
