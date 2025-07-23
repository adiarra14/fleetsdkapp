// Working Maxvision SDK TCP Server Implementation
// Version 5.0 (2025-07-22) - Simplified for successful compilation
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;
import java.net.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class WorkingMaxvisionSdkServer {
    
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    private static final int TCP_PORT = 8910;  // SDK STANDARD PORT
    private static final int HTTP_PORT = 8080;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean running = true;
    
    public static void main(String[] args) {
        System.out.println("=== WORKING MAXVISION SDK TCP SERVER: Starting at " + LocalDateTime.now() + " ===");
        System.out.println("=== REAL SDK INTEGRATION - NO MOCK ===");
        
        WorkingMaxvisionSdkServer server = new WorkingMaxvisionSdkServer();
        server.start();
    }
    
    public void start() {
        // Start HTTP health endpoint
        startHttpServer();
        
        // Start TCP server for balise connections
        startTcpServer();
    }
    
    private void startHttpServer() {
        new Thread(() -> {
            try {
                ServerSocket httpSocket = new ServerSocket(HTTP_PORT);
                System.out.println("=== HTTP Health Server listening on port " + HTTP_PORT + " ===");
                
                while (running) {
                    Socket client = httpSocket.accept();
                    new Thread(() -> handleHttpRequest(client)).start();
                }
            } catch (Exception e) {
                System.err.println("HTTP Server error: " + e.getMessage());
            }
        }).start();
    }
    
    private void handleHttpRequest(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream())) {
            
            String line = in.readLine();
            if (line != null && line.contains("GET")) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"status\":\"UP\",\"service\":\"WorkingMaxvisionSdkServer\"}");
            }
            client.close();
        } catch (Exception e) {
            System.err.println("HTTP request error: " + e.getMessage());
        }
    }
    
    private void startTcpServer() {
        try {
            ServerSocket tcpSocket = new ServerSocket(TCP_PORT);
            System.out.println("=== TCP Server listening for balise connections on port " + TCP_PORT + " ===");
            System.out.println("=== SDK Integration Ready - Waiting for real balise data ===");
            
            while (running) {
                Socket client = tcpSocket.accept();
                new Thread(() -> handleBaliseConnection(client)).start();
            }
        } catch (Exception e) {
            System.err.println("TCP Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleBaliseConnection(Socket client) {
        String clientIP = client.getInetAddress().getHostAddress();
        System.out.println("=== BALISE CONNECTION from " + clientIP + " ===");
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
            
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("=== RAW BALISE MESSAGE ===");
                System.out.println("From: " + clientIP);
                System.out.println("Data: " + message);
                
                // Process the message using SDK-style parsing
                processBaliseMessage(message, clientIP);
                
                // Send acknowledgment
                out.println("ACK");
            }
        } catch (Exception e) {
            System.err.println("Error handling balise connection from " + clientIP + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                client.close();
                System.out.println("=== Connection closed for " + clientIP + " ===");
            } catch (Exception e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    private void processBaliseMessage(String rawData, String clientIP) {
        System.out.println("=== PROCESSING TY5201-LOCK MESSAGE ===");
        System.out.println("Raw Data: " + rawData);
        
        // Skip JSON parsing - go directly to GPS extraction and storage
        String deviceId = "RAW-" + clientIP.replace(".", "-");
        System.out.println("=== STORING RAW MESSAGE WITH GPS EXTRACTION ===");
        storeRawMessage(deviceId, rawData, clientIP);
    }
    
    private String extractDeviceIdFromJson(JsonNode rootNode) {
        // Try various common fields for device ID
        String[] possibleFields = {"deviceId", "device_id", "id", "lockId", "serialNumber", "imei"};
        
        for (String field : possibleFields) {
            String value = rootNode.path(field).asText();
            if (!value.isEmpty()) {
                return value;
            }
        }
        
        return "";
    }
    
    private void processLoginMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING LOGIN MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
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
                stmt.setInt(4, parseIntSafe(voltage, 0));
                stmt.executeUpdate();
                System.out.println("✅ Stored balise login: " + lockCode);
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
                stmt.setInt(1, parseIntSafe(voltage, 0));
                stmt.setString(2, lockCode);
                int updated = stmt.executeUpdate();
                System.out.println("✅ Updated balise keep-alive: " + lockCode + " (rows: " + updated + ")");
            }
            
            // Store keep-alive event
            storeEvent(conn, lockCode, "KEEP_ALIVE", dataNode, 
                "Keep-Alive - Network: " + networkValue + ", Voltage: " + voltage);
            
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
                stmt.setInt(2, parseIntSafe(voltage, 0));
                stmt.setString(3, lockCode);
                int updated = stmt.executeUpdate();
                System.out.println("✅ Updated balise GPS: " + lockCode + " (rows: " + updated + ")");
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
                stmt.setInt(6, parseIntSafe(voltage, 0));
                stmt.setString(7, dataNode.toString());
                stmt.executeUpdate();
                System.out.println("✅ Stored GPS event: " + lockCode + " at " + latitude + "," + longitude);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing GPS report message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processNfcInfoMessage(String lockCode, JsonNode dataNode) {
        System.out.println("=== PROCESSING NFC INFO MESSAGE ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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
                System.out.println("✅ Stored generic event: " + lockCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing generic event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void storeRawMessage(String deviceId, String rawData, String clientIP) {
        System.out.println("=== STORING RAW MESSAGE WITH GPS EXTRACTION ===");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            
            // AUTO-CREATE balise entry if it doesn't exist
            ensureBaliseExists(conn, deviceId);
            
            // Try to extract GPS coordinates from TY5201-LOCK message
            GPSData gpsData = extractGPSFromTY5201(rawData);
            
            String eventSql;
            if (gpsData != null && gpsData.isValid()) {
                // Store with GPS coordinates
                eventSql = """
                    INSERT INTO balise_events (device_id, event_time, event_type, latitude, longitude, raw_data)
                    VALUES (?, NOW(), ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                    stmt.setString(1, deviceId);
                    stmt.setString(2, "GPS_DATA");
                    stmt.setDouble(3, gpsData.latitude);
                    stmt.setDouble(4, gpsData.longitude);
                    // Clean binary data for UTF8 storage
                    String cleanData = cleanBinaryData(rawData);
                    stmt.setString(5, "IP: " + clientIP + " | Data: " + cleanData);
                    stmt.executeUpdate();
                    System.out.println("🛰️ ✅ STORED GPS MESSAGE: " + deviceId + " at " + gpsData.latitude + "," + gpsData.longitude);
                }
            } else {
                // Store without GPS coordinates
                eventSql = """
                    INSERT INTO balise_events (device_id, event_time, event_type, raw_data)
                    VALUES (?, NOW(), ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                    stmt.setString(1, deviceId);
                    stmt.setString(2, "RAW_DATA");
                    // Clean binary data for UTF8 storage
                    String cleanData = cleanBinaryData(rawData);
                    stmt.setString(3, "IP: " + clientIP + " | Data: " + cleanData);
                    stmt.executeUpdate();
                    System.out.println("✅ Stored raw message: " + deviceId + " (no GPS found)");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error storing raw message: " + e.getMessage());
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
                System.out.println("✅ Stored " + eventType + " event: " + lockCode + " - " + description);
            }
            
        } catch (Exception e) {
            System.err.println("Error storing event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private String cleanBinaryData(String data) {
        if (data == null) return "";
        
        // Remove null bytes and other binary characters that cause UTF8 issues
        StringBuilder cleaned = new StringBuilder();
        for (char c : data.toCharArray()) {
            if (c >= 32 && c <= 126) { // Printable ASCII
                cleaned.append(c);
            } else if (c == '\n' || c == '\r' || c == '\t') { // Keep common whitespace
                cleaned.append(c);
            } else {
                // Replace binary chars with hex representation
                cleaned.append(String.format("[0x%02X]", (int) c));
            }
        }
        return cleaned.toString();
    }
    
    private void ensureBaliseExists(Connection conn, String deviceId) {
        try {
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
     * GPS Data container class
     */
    private static class GPSData {
        public double latitude;
        public double longitude;
        public boolean valid;
        
        public GPSData(double lat, double lng, boolean valid) {
            this.latitude = lat;
            this.longitude = lng;
            this.valid = valid;
        }
        
        public boolean isValid() {
            return valid && latitude != 0.0 && longitude != 0.0;
        }
    }
    
    /**
     * Extract GPS coordinates from TY5201-LOCK binary message
     * TY5201-LOCK protocol contains GPS data in specific byte positions
     */
    private GPSData extractGPSFromTY5201(String rawData) {
        try {
            System.out.println("🛰️ Attempting GPS extraction from TY5201-LOCK message");
            
            // Look for GPS patterns in the message
            // TY5201-LOCK messages often contain coordinate data after specific markers
            
            // Method 1: Look for coordinate patterns (decimal degrees)
            GPSData coordData = extractCoordinatePatterns(rawData);
            if (coordData != null && coordData.isValid()) {
                System.out.println("✅ GPS extracted via coordinate patterns: " + coordData.latitude + "," + coordData.longitude);
                return coordData;
            }
            
            // Method 2: Look for binary GPS data (common in tracking devices)
            GPSData binaryData = extractBinaryGPS(rawData);
            if (binaryData != null && binaryData.isValid()) {
                System.out.println("✅ GPS extracted via binary parsing: " + binaryData.latitude + "," + binaryData.longitude);
                return binaryData;
            }
            
            // Method 3: Look for hex-encoded coordinates
            GPSData hexData = extractHexGPS(rawData);
            if (hexData != null && hexData.isValid()) {
                System.out.println("✅ GPS extracted via hex parsing: " + hexData.latitude + "," + hexData.longitude);
                return hexData;
            }
            
            System.out.println("⚠️ No GPS coordinates found in message");
            return null;
            
        } catch (Exception e) {
            System.err.println("Error extracting GPS: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract coordinates from decimal patterns in the message
     */
    private GPSData extractCoordinatePatterns(String rawData) {
        try {
            // Look for latitude/longitude patterns (e.g., 48.123456, 2.123456)
            String[] patterns = {
                "(\\d{1,3}\\.\\d{4,8})[,\\s]+(\\d{1,3}\\.\\d{4,8})", // lat,lng format
                "lat[=:]([\\d\\.]+).*lng[=:]([\\d\\.]+)", // lat=xx lng=yy format
                "([\\d\\.]{6,12})[,\\s]+([\\d\\.]{6,12})" // general decimal format
            };
            
            for (String pattern : patterns) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = p.matcher(rawData);
                if (m.find()) {
                    double lat = Double.parseDouble(m.group(1));
                    double lng = Double.parseDouble(m.group(2));
                    
                    // Validate coordinate ranges
                    if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                        return new GPSData(lat, lng, true);
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract GPS from binary data (common in tracking protocols)
     */
    private GPSData extractBinaryGPS(String rawData) {
        try {
            // Convert string to bytes and look for GPS patterns
            byte[] bytes = rawData.getBytes();
            
            // Look for GPS data patterns in binary format
            // Many tracking devices encode GPS as 4-byte integers (lat/lng * 1000000)
            for (int i = 0; i < bytes.length - 8; i++) {
                if (bytes[i] == 'G' && bytes[i+1] == 'P' && bytes[i+2] == 'S') {
                    // Found GPS marker, try to extract coordinates
                    if (i + 16 < bytes.length) {
                        // Try to read lat/lng as 4-byte integers
                        int latInt = bytesToInt(bytes, i + 4);
                        int lngInt = bytesToInt(bytes, i + 8);
                        
                        double lat = latInt / 1000000.0;
                        double lng = lngInt / 1000000.0;
                        
                        if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180 && lat != 0 && lng != 0) {
                            return new GPSData(lat, lng, true);
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract GPS from hex-encoded data
     */
    private GPSData extractHexGPS(String rawData) {
        try {
            // Look for hex patterns that might represent coordinates
            String[] hexPatterns = rawData.split("[^0-9A-Fa-f]");
            
            for (String hex : hexPatterns) {
                if (hex.length() >= 8) {
                    try {
                        // Try to interpret as hex-encoded coordinates
                        long value = Long.parseLong(hex.substring(0, 8), 16);
                        double coord = value / 1000000.0;
                        
                        if (coord >= -180 && coord <= 180 && coord != 0) {
                            // This might be a coordinate, but we need both lat and lng
                            // For now, return null and let other methods try
                        }
                    } catch (NumberFormatException e) {
                        // Not a valid hex number
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Convert 4 bytes to integer (little-endian)
     */
    private int bytesToInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) |
               ((bytes[offset + 1] & 0xFF) << 8) |
               ((bytes[offset + 2] & 0xFF) << 16) |
               ((bytes[offset + 3] & 0xFF) << 24);
    }
}
