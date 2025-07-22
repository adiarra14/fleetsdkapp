import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * REAL MAXVISION SDK SERVER
 * Uses the actual Maxvision SDK to parse TY5201-LOCK messages and extract GPS data
 * Implements LockReportService to receive parsed JSON messages from the SDK
 */
@SpringBootApplication
@ComponentScan({"com.maxvision.edge.gateway", "com.maxvision"})
public class RealSdkMaxvisionServer {
    
    // Database configuration
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://balise-postgres:5432/balisedb");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "adminbdb");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "To7Z2UCeWTsriPxbADX8");
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public static void main(String[] args) {
        System.out.println("🚀 STARTING REAL MAXVISION SDK SERVER");
        System.out.println("📡 SDK will listen on port 8910 for TY5201-LOCK messages");
        System.out.println("🗄️ Database: " + DB_URL);
        
        // Start health monitoring
        startHealthMonitoring();
        
        // Start Spring Boot with SDK
        SpringApplication.run(RealSdkMaxvisionServer.class, args);
        
        System.out.println("✅ REAL MAXVISION SDK SERVER STARTED");
    }
    
    /**
     * Custom bean name generator to avoid conflicts with SDK beans
     */
    @Bean
    public static BeanNameGenerator beanNameGenerator() {
        return new BeanNameGenerator() {
            @Override
            public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
                return definition.getBeanClassName();
            }
        };
    }
    
    /**
     * LockReportService implementation - receives parsed messages from SDK
     */
    @Service
    public static class LockReportServiceImpl implements com.maxvision.edge.gateway.lock.service.LockReportService {
        
        @Override
        public void reportLockMsg(String jsonStr) {
            System.out.println("=== SDK PARSED MESSAGE RECEIVED ===");
            System.out.println("JSON: " + jsonStr);
            
            try {
                JsonNode rootNode = objectMapper.readTree(jsonStr);
                String lockCode = rootNode.path("lockCode").asText();
                JsonNode dataNode = rootNode.path("data");
                
                if (lockCode.isEmpty()) {
                    System.err.println("❌ No lockCode in SDK message");
                    return;
                }
                
                // Ensure balise exists before processing
                ensureBaliseExists(lockCode);
                
                // Process different message types
                if (dataNode.has("gps") && dataNode.has("type")) {
                    // GPS Report Message - this is what we want!
                    processGpsReportMessage(lockCode, dataNode);
                } else if (dataNode.has("lockGpsResModel")) {
                    // Login Message with GPS data
                    processLoginMessage(lockCode, dataNode);
                } else if (dataNode.has("networkValue") && dataNode.has("voltage")) {
                    // Keep-Alive Message
                    processKeepAliveMessage(lockCode, dataNode);
                } else if (dataNode.has("universalNfcList")) {
                    // NFC Info Report
                    processNfcInfoMessage(lockCode, dataNode);
                } else {
                    System.out.println("Unknown SDK message type, storing as generic event");
                    storeGenericEvent(lockCode, jsonStr);
                }
                
            } catch (Exception e) {
                System.err.println("Error processing SDK message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Process GPS report messages - MAIN GPS DATA HANDLER
     */
    private static void processGpsReportMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING GPS REPORT MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            String type = dataNode.path("type").asText();
            long gpsTime = dataNode.path("gpsTime").asLong();
            JsonNode gpsNode = dataNode.path("gps");
            double latitude = gpsNode.path("lat").asDouble();
            double longitude = gpsNode.path("lng").asDouble();
            String lockStatus = dataNode.path("lockStatus").asText();
            String voltage = dataNode.path("voltagePercentage").asText();
            
            System.out.println("🛰️ GPS Data: " + latitude + "," + longitude + " | Status: " + lockStatus + " | Battery: " + voltage + "%");
            
            // Update balise status and location
            String updateSql = """
                UPDATE balises SET 
                    last_seen = NOW(), 
                    updated_at = NOW()
                WHERE device_id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, lockCode);
                int updated = stmt.executeUpdate();
                System.out.println("✅ Updated balise: " + lockCode + " (rows: " + updated + ")");
            }
            
            // Store GPS event with REAL coordinates
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, latitude, longitude, battery_level, raw_data)
                VALUES (?, to_timestamp(? / 1000), ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setLong(2, gpsTime);
                stmt.setString(3, "GPS_" + type.toUpperCase());
                stmt.setDouble(4, latitude);  // REAL LATITUDE!
                stmt.setDouble(5, longitude); // REAL LONGITUDE!
                stmt.setInt(6, parseIntSafe(voltage, 0));
                stmt.setString(7, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("🎯 ✅ STORED GPS EVENT: " + lockCode + " at " + latitude + "," + longitude);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing GPS report message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Process login messages with GPS data
     */
    private static void processLoginMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING LOGIN MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            JsonNode lockGpsModel = dataNode.path("lockGpsResModel");
            JsonNode gpsNode = lockGpsModel.path("gps");
            
            if (!gpsNode.isMissingNode()) {
                double latitude = gpsNode.path("lat").asDouble();
                double longitude = gpsNode.path("lng").asDouble();
                long gpsTime = lockGpsModel.path("gpsTime").asLong();
                String lockStatus = lockGpsModel.path("lockStatus").asText();
                String voltage = lockGpsModel.path("voltage").asText();
                
                System.out.println("🔑 Login GPS: " + latitude + "," + longitude + " | Status: " + lockStatus);
                
                // Store login event with GPS
                String eventSql = """
                    INSERT INTO balise_events (device_id, event_time, event_type, latitude, longitude, battery_level, raw_data)
                    VALUES (?, to_timestamp(? / 1000), ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                    stmt.setString(1, lockCode);
                    stmt.setLong(2, gpsTime);
                    stmt.setString(3, "LOGIN");
                    stmt.setDouble(4, latitude);
                    stmt.setDouble(5, longitude);
                    stmt.setInt(6, parseIntSafe(voltage, 0));
                    stmt.setString(7, dataNode.toString());
                    stmt.executeUpdate();
                    System.out.println("✅ Stored login GPS event: " + lockCode);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error processing login message: " + e.getMessage());
        }
    }
    
    /**
     * Process keep-alive messages
     */
    private static void processKeepAliveMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING KEEP-ALIVE MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String networkValue = dataNode.path("networkValue").asText();
            String voltage = dataNode.path("voltage").asText();
            
            System.out.println("💓 Keep-alive: " + lockCode + " | Network: " + networkValue + " | Battery: " + voltage + "%");
            
            // Store keep-alive event
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, battery_level, raw_data)
                VALUES (?, NOW(), ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, "KEEP_ALIVE");
                stmt.setInt(3, parseIntSafe(voltage, 0));
                stmt.setString(4, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("✅ Stored keep-alive event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing keep-alive message: " + e.getMessage());
        }
    }
    
    /**
     * Process NFC info messages
     */
    private static void processNfcInfoMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING NFC INFO MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Store NFC info event
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, raw_data)
                VALUES (?, NOW(), ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, "NFC_INFO");
                stmt.setString(3, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("✅ Stored NFC info event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing NFC info message: " + e.getMessage());
        }
    }
    
    /**
     * Store generic events for unknown message types
     */
    private static void storeGenericEvent(String lockCode, String jsonStr) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, raw_data)
                VALUES (?, NOW(), ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, "GENERIC");
                stmt.setString(3, jsonStr);
                stmt.executeUpdate();
                System.out.println("✅ Stored generic event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing generic event: " + e.getMessage());
        }
    }
    
    /**
     * Ensure balise exists in database (auto-create if needed)
     */
    private static void ensureBaliseExists(String deviceId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if balise exists
            String checkSql = "SELECT COUNT(*) FROM balises WHERE device_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, deviceId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Balise doesn't exist, create it
                        System.out.println("🔧 Auto-creating balise entry: " + deviceId);
                        
                        String insertSql = "INSERT INTO balises (device_id, created_at, updated_at, last_seen) VALUES (?, NOW(), NOW(), NOW())";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, deviceId);
                            insertStmt.executeUpdate();
                            System.out.println("✅ Auto-created balise: " + deviceId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring balise exists: " + e.getMessage());
            // Continue anyway - the foreign key constraint will catch it
        }
    }
    
    /**
     * Safe integer parsing
     */
    private static int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Start health monitoring
     */
    private static void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("💚 REAL SDK SERVER HEALTHY - Database connected");
            } catch (Exception e) {
                System.err.println("❤️ Health check failed: " + e.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS);
    }
}
