// Real Maxvision SDK TCP Server Implementation
// Version 4.0 (2025-07-22) - Full SDK integration with LockReportService
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Service;
import com.maxvision.edge.gateway.lock.service.LockReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@SpringBootApplication
@ComponentScan({"com.maxvision.edge.gateway", "com.maxvision.sdk"})
public class RealMaxvisionSdkServer {
    
    // Custom Bean Name Generator to avoid conflicts
    public static class CustomBeanNameGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            return definition.getBeanClassName();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== REAL MAXVISION SDK TCP SERVER: Starting at " + LocalDateTime.now() + " ===");
        System.out.println("=== NO MOCK - FULL SDK INTEGRATION ===");
        
        // Start Spring Boot application with SDK integration
        new SpringApplicationBuilder(RealMaxvisionSdkServer.class)
            .beanNameGenerator(new CustomBeanNameGenerator())
            .run(args);
            
        System.out.println("=== REAL MAXVISION SDK SERVER STARTED ===");
    }
}

// Real LockReportService implementation - NO MOCK
@Service
public class LockReportServiceImpl implements LockReportService {
    
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void reportLockMsg(String jsonStr) {
        System.out.println("=== REAL SDK MESSAGE RECEIVED ===");
        System.out.println("Raw JSON: " + jsonStr);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            String deviceId = rootNode.path("lockCode").asText();
            JsonNode dataNode = rootNode.path("data");

            double latitude = dataNode.path("latitude").asDouble();
            double longitude = dataNode.path("longitude").asDouble();
            String eventType = dataNode.path("type").asText();
            String battery = dataNode.path("voltage").asText();

            // Insert event into balise_events
            String insertSql = "INSERT INTO balise_events (device_id, latitude, longitude, event_type, battery_level, event_time, raw_data) VALUES (?, ?, ?, ?, ?, NOW(), ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, deviceId);
                stmt.setDouble(2, latitude);
                stmt.setDouble(3, longitude);
                stmt.setString(4, eventType);
                stmt.setInt(5, Integer.parseInt(battery));
                stmt.setString(6, jsonStr);
                stmt.executeUpdate();
            }

            // Optionally: update balises table last_seen
            String updateSql = "UPDATE balises SET last_seen = NOW(), updated_at = NOW() WHERE device_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, deviceId);
                stmt.executeUpdate();
            }

            System.out.println("✅ Event stored for device " + deviceId + " at " + latitude + "," + longitude);

        } catch (Exception e) {
            System.err.println("Error storing SDK event: " + e.getMessage());
            e.printStackTrace();
        }
    }            JsonNode rootNode = objectMapper.readTree(jsonStr);
            
            // Extract lockCode (the real device ID)
            String lockCode = rootNode.path("lockCode").asText();
            JsonNode dataNode = rootNode.path("data");
            
            System.out.println("Lock Code: " + lockCode);
            System.out.println("Data: " + dataNode.toString());
            
            // Determine message type and process accordingly
            if (dataNode.has("gpsUploadInterval")) {
                // Login Message
                processLoginMessage(lockCode, dataNode);
            } else if (dataNode.has("networkValue") && dataNode.has("voltage")) {
                // Keep-Alive Message
                processKeepAliveMessage(lockCode, dataNode);
            } else if (dataNode.has("type")) {
                // GPS Report Message
                processGpsReportMessage(lockCode, dataNode);
            } else if (dataNode.has("universalNfcList")) {
                // NFC Info Report
                processNfcInfoMessage(lockCode, dataNode);
            } else {
                System.out.println("Unknown message type, storing as generic event");
                storeGenericEvent(lockCode, jsonStr);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing SDK message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processLoginMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING LOGIN MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            // Extract device information
            String version = dataNode.path("version").asText();
            String deviceMode = dataNode.path("deviceMode").asText();
            String gpsInterval = dataNode.path("gpsUploadInterval").asText();
            
            JsonNode gpsModel = dataNode.path("lockGpsResModel");
            String lockStatus = gpsModel.path("lockStatus").asText();
            String voltage = gpsModel.path("voltage").asText();
            
            // Store/update balise information
            String upsertSql = """
                INSERT INTO balises (device_id, model, status, battery_level, created_at, updated_at, last_seen)
                VALUES (?, ?, ?, ?, NOW(), NOW(), NOW())
                ON CONFLICT (device_id) DO UPDATE SET
                    status = EXCLUDED.status,
                    battery_level = EXCLUDED.battery_level,
                    updated_at = NOW(),
                    last_seen = NOW()
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, "TY5201-LOCK");
                stmt.setString(3, lockStatus.equals("seal") ? "SEALED" : "UNSEALED");
                stmt.setInt(4, Integer.parseInt(voltage));
                stmt.executeUpdate();
                System.out.println("Stored balise login: " + lockCode);
            }
            
            // Store login event
            storeEvent(conn, lockCode, "LOGIN", gpsModel, 
                "Login - Version: " + version + ", Mode: " + deviceMode + ", GPS Interval: " + gpsInterval);
                
        } catch (Exception e) {
            System.err.println("Error processing login message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processKeepAliveMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING KEEP-ALIVE MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            String networkValue = dataNode.path("networkValue").asText();
            String voltage = dataNode.path("voltage").asText();
            
            // Update balise last_seen and battery
            String updateSql = """
                UPDATE balises SET 
                    battery_level = ?, 
                    last_seen = NOW(), 
                    updated_at = NOW()
                WHERE device_id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, Integer.parseInt(voltage));
                stmt.setString(2, lockCode);
                stmt.executeUpdate();
                System.out.println("Updated balise keep-alive: " + lockCode);
            }
            
            // Store keep-alive event
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, battery_level, raw_data)
                VALUES (?, NOW(), ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, "KEEP_ALIVE");
                stmt.setInt(3, Integer.parseInt(voltage));
                stmt.setString(4, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("Stored keep-alive event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing keep-alive message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processGpsReportMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING GPS REPORT MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            String type = dataNode.path("type").asText();
            long gpsTime = dataNode.path("gpsTime").asLong();
            JsonNode gpsNode = dataNode.path("gps");
            double latitude = gpsNode.path("lat").asDouble();
            double longitude = gpsNode.path("lng").asDouble();
            String lockStatus = dataNode.path("lockStatus").asText();
            String voltage = dataNode.path("voltagePercentage").asText();
            
            // Update balise status and location
            String updateSql = """
                UPDATE balises SET 
                    status = ?, 
                    battery_level = ?, 
                    last_seen = NOW(), 
                    updated_at = NOW()
                WHERE device_id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, lockStatus.equals("seal") ? "SEALED" : "UNSEALED");
                stmt.setInt(2, Integer.parseInt(voltage));
                stmt.setString(3, lockCode);
                stmt.executeUpdate();
                System.out.println("Updated balise GPS: " + lockCode);
            }
            
            // Store GPS event
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, latitude, longitude, battery_level, raw_data)
                VALUES (?, to_timestamp(? / 1000), ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setLong(2, gpsTime);
                stmt.setString(3, "GPS_" + type.toUpperCase());
                stmt.setDouble(4, latitude);
                stmt.setDouble(5, longitude);
                stmt.setInt(6, Integer.parseInt(voltage));
                stmt.setString(7, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("Stored GPS event: " + lockCode + " at " + latitude + "," + longitude);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing GPS report message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processNfcInfoMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING NFC INFO MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            // Store NFC info event
            storeEvent(conn, lockCode, "NFC_INFO", dataNode, "NFC card information updated");
            
        } catch (Exception e) {
            System.err.println("Error processing NFC info message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void storeGenericEvent(String lockCode, String jsonStr) {
        System.out.println("=== STORING GENERIC EVENT ===");
        
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
                System.out.println("Stored generic event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing generic event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void storeEvent(Connection conn, String lockCode, String eventType, JsonNode dataNode, String description) {
        try {
            String eventSql = """
                INSERT INTO balise_events (device_id, event_time, event_type, raw_data)
                VALUES (?, NOW(), ?, ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, lockCode);
                stmt.setString(2, eventType);
                stmt.setString(3, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("Stored " + eventType + " event: " + lockCode + " - " + description);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
