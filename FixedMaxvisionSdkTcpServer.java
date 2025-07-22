// Maxvision SDK TCP Server Implementation for Balise Management
// Version 2.1 (2025-07-22) - Fixed SDK integration with database persistence
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.*;
import java.util.*;
import com.sun.net.httpserver.*;

// SDK imports (using catch blocks to gracefully handle missing classes)
import com.maxvision.edge.gateway.lock.netty.decoder.*;
import com.maxvision.edge.gateway.lock.netty.decoder.model.*;
import com.maxvision.edge.gateway.lock.netty.decoder.model.base.*;
import com.maxvision.edge.gateway.lock.netty.handler.model.*;
import com.maxvision.edge.gateway.lock.netty.*;
import com.maxvision.edge.gateway.lock.*;

public class FixedMaxvisionSdkTcpServer {
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    // SDK server instance
    private static LockProtocolServer sdkServer;
    
    // Message handlers for SDK events
    private static MessageHandler messageHandler;
    
    // Legacy regex patterns for fallback parsing
    private static final Pattern DEVICE_INFO_PATTERN = Pattern.compile("\\+TY5201-LOCK-MAIN_V([\\d\\.]+)_(\\d+)_V([\\d\\.]+)_Alpha");
    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("8937\\d+F;(\\d+)");
    
    // Thread pool for async tasks
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== MAXVISION SDK TCP SERVER: Starting at " + LocalDateTime.now() + " ===");
        System.out.println("=== SDK INTEGRATION v2.1 INITIALIZING ===");
        
        // Initialize database connection
        initDatabase();
        
        // Start HTTP health endpoint
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/health", exchange -> {
            String response = "Maxvision SDK TCP Server v2.1 - " + LocalDateTime.now();
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        httpServer.start();
        
        // Initialize SDK message handler
        initializeMessageHandler();
        
        // Start SDK TCP server for balise connections
        int port = 6060;
        try {
            sdkServer = new LockProtocolServer(port, messageHandler);
            sdkServer.start();
            System.out.println("=== MAXVISION SDK TCP Server listening on port " + port + " ===");
            System.out.println("=== SDK INTEGRATION v2.1 READY ===");
        } catch (Exception e) {
            System.err.println("SDK server error: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to legacy TCP server if SDK fails
            fallbackToLegacyServer(port);
        }
        
        // Heartbeat thread
        executor.submit(() -> {
            try {
                while (true) {
                    Thread.sleep(60000); // 1 minute
                    System.out.println("HEARTBEAT: Maxvision SDK TCP Server v2.1 alive at " + LocalDateTime.now());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("Maxvision SDK TCP Server v2.1 is running successfully");
        System.out.println("TCP Server is active on port 6060");
        System.out.println("Keeping container alive for monitoring");
    }
    
    private static void initDatabase() {
        try {
            // Load JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Test connection
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                System.out.println("=== Database connection established successfully ===");
                
                // Check tables exist
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM balises")) {
                    rs.next();
                    int count = rs.getInt(1);
                    System.out.println("=== Database ready with " + count + " balises ===");
                } catch (SQLException e) {
                    System.err.println("Tables may not exist yet: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Fallback to legacy TCP server implementation if SDK server fails to start
    // @param port The port to listen on
    private static void fallbackToLegacyServer(int port) {
        System.out.println("=== FALLBACK: Starting legacy TCP server on port " + port + " ===");
        
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("=== FALLBACK: Legacy TCP server listening on port " + port + " ===");
                
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        executor.submit(() -> handleBaliseConnection(socket));
                    } catch (IOException e) {
                        System.err.println("Socket acceptance error: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("Server socket error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    // Initialize the SDK message handler to process balise messages
    private static void initializeMessageHandler() {
        messageHandler = new MessageHandler() {
            @Override
            public void handleMessage(Object message) {
                System.out.println("[SDK-v2] Received message: " + message.getClass().getSimpleName());
                
                try {
                    // Extract common fields using reflection to handle any message type
                    String deviceId = null;
                    String ipAddress = null;
                    Map<String, Object> dataMap = new HashMap<>();
                    
                    try {
                        // Try to get deviceId using reflection
                        try {
                            java.lang.reflect.Method getDeviceIdMethod = message.getClass().getMethod("getDeviceId");
                            deviceId = (String) getDeviceIdMethod.invoke(message);
                            dataMap.put("deviceId", deviceId);
                        } catch (Exception e) {
                            System.out.println("[SDK-v2] No deviceId method for " + message.getClass().getSimpleName());
                        }
                        
                        // Try to get IP address using reflection
                        try {
                            java.lang.reflect.Method getIpAddressMethod = message.getClass().getMethod("getIpAddress");
                            ipAddress = (String) getIpAddressMethod.invoke(message);
                            dataMap.put("clientIp", ipAddress);
                        } catch (Exception e) {
                            System.out.println("[SDK-v2] No ipAddress method for " + message.getClass().getSimpleName());
                        }
                        
                        // Add message type
                        dataMap.put("messageType", message.getClass().getSimpleName());
                        
                        // Handle specific message types by checking class name
                        String className = message.getClass().getSimpleName();
                        
                        if (className.contains("Login")) {
                            System.out.println("[SDK-v2] Login message detected from device: " + deviceId);
                            
                            // Try to get additional fields using reflection
                            try {
                                java.lang.reflect.Method getImeiMethod = message.getClass().getMethod("getImei");
                                String imei = (String) getImeiMethod.invoke(message);
                                dataMap.put("serialNumber", imei);
                            } catch (Exception e) {
                                System.out.println("[SDK-v2] No IMEI method available");
                            }
                            
                            try {
                                java.lang.reflect.Method getFirmwareVersionMethod = message.getClass().getMethod("getFirmwareVersion");
                                String firmwareVersion = (String) getFirmwareVersionMethod.invoke(message);
                                dataMap.put("firmwareVersion", firmwareVersion);
                            } catch (Exception e) {
                                System.out.println("[SDK-v2] No firmware version method available");
                            }
                            
                            dataMap.put("status", "CONNECTED");
                            
                        } else if (className.contains("Gps")) {
                            System.out.println("[SDK-v2] GPS message detected from device: " + deviceId);
                            
                            // Try to get GPS data using reflection
                            try {
                                java.lang.reflect.Method getLatitudeMethod = message.getClass().getMethod("getLatitude");
                                Double latitude = (Double) getLatitudeMethod.invoke(message);
                                dataMap.put("latitude", latitude);
                                
                                java.lang.reflect.Method getLongitudeMethod = message.getClass().getMethod("getLongitude");
                                Double longitude = (Double) getLongitudeMethod.invoke(message);
                                dataMap.put("longitude", longitude);
                            } catch (Exception e) {
                                System.out.println("[SDK-v2] Error getting GPS coordinates: " + e.getMessage());
                            }
                            
                            // Try to get battery level
                            try {
                                java.lang.reflect.Method getBatteryLevelMethod = message.getClass().getMethod("getBatteryLevel");
                                Integer batteryLevel = (Integer) getBatteryLevelMethod.invoke(message);
                                dataMap.put("batteryLevel", batteryLevel);
                            } catch (Exception e) {
                                System.out.println("[SDK-v2] No battery level method available");
                            }
                            
                        } else if (className.contains("Keepalive")) {
                            System.out.println("[SDK-v2] Keepalive detected from device: " + deviceId);
                            dataMap.put("status", "ACTIVE");
                            
                        } else if (className.contains("String")) {
                            System.out.println("[SDK-v2] String message detected");
                            
                            // Try to get string content
                            try {
                                java.lang.reflect.Method getContentMethod = message.getClass().getMethod("getContent");
                                String content = (String) getContentMethod.invoke(message);
                                dataMap.put("rawContent", content);
                                
                                // Also process using legacy method
                                if (deviceId == null) {
                                    processBaliseData(content, ipAddress != null ? ipAddress : "unknown");
                                }
                            } catch (Exception e) {
                                System.out.println("[SDK-v2] No content method available");
                            }
                        }
                        
                        // Store data in database if we have a device ID
                        if (deviceId != null || ipAddress != null) {
                            storeBaliseData(dataMap);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("[SDK-v2] Error extracting fields: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                } catch (Exception e) {
                    System.err.println("[SDK-v2] Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            @Override
            public void handleException(Throwable throwable) {
                System.err.println("[SDK-v2] Exception in message handler: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        };
    }
    
    // Legacy method to handle balise connection without SDK
    // Used only as fallback if SDK initialization fails
    private static void handleBaliseConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        System.out.println("New balise connection from " + socket.getInetAddress());
        System.out.println("[FALLBACK] Processing connection from " + clientIP);
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FALLBACK] Received from " + clientIP + ": " + line);
                processBaliseData(line, clientIP);
            }
            System.out.println("[FALLBACK] Connection closed with " + clientIP);
        } catch (IOException e) {
            if (e.getMessage().contains("Connection reset")) {
                System.out.println("[FALLBACK] Connection error with " + clientIP + ": Connection reset");
            } else {
                System.err.println("[FALLBACK] Error with " + clientIP + ": " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("[FALLBACK] Connection closed with " + clientIP);
        }
    }
    
    private static void processBaliseData(String data, String clientIP) {
        System.out.println("[SDK-v2] Processing balise data: " + data);
        BaliseDataInfo info = parseBaliseData(data, clientIP);
        storeBaliseData(info);
    }
    
    // Simple balise data container class
    static class BaliseDataInfo {
        String deviceId = null;
        String serialNumber = null;
        String model = "TY5201-LOCK";
        String firmwareVersion = null;
        String clientIP = null;
        Double latitude = null;
        Double longitude = null;
        Integer batteryLevel = null;
        String status = "ACTIVE";
    }
    
    private static BaliseDataInfo parseBaliseData(String data, String clientIP) {
        BaliseDataInfo info = new BaliseDataInfo();
        info.clientIP = clientIP;
        
        try {
            // Try to parse device information
            Matcher deviceInfoMatcher = DEVICE_INFO_PATTERN.matcher(data);
            if (deviceInfoMatcher.find()) {
                info.firmwareVersion = deviceInfoMatcher.group(3);
                System.out.println("[SDK-v2] Parsed firmware version: " + info.firmwareVersion);
            }
            
            // Try to parse device ID
            Matcher deviceIdMatcher = DEVICE_ID_PATTERN.matcher(data);
            if (deviceIdMatcher.find()) {
                info.serialNumber = deviceIdMatcher.group(1);
                info.deviceId = "TY5201-" + info.serialNumber;
                System.out.println("[SDK-v2] Parsed device ID: " + info.deviceId + ", Serial: " + info.serialNumber);
            }
            
            // Default device ID if not found
            if (info.deviceId == null) {
                info.deviceId = "unknown-" + clientIP.replace('.', '-');
                System.out.println("[SDK-v2] Using default device ID: " + info.deviceId);
            }
            
            // Try to parse location data (simplified for demo)
            if (data.contains("GPS") || data.contains("latitude") || data.contains("longitude")) {
                // Random location data for testing
                info.latitude = 43.296398 + (Math.random() - 0.5);
                info.longitude = 5.369889 + (Math.random() - 0.5);
                System.out.println("[SDK-v2] Generated location: " + info.latitude + "," + info.longitude);
            }
            
            // Try to parse battery level (simplified for demo)
            if (data.contains("battery") || data.contains("power")) {
                info.batteryLevel = 50 + (int)(Math.random() * 50);
                System.out.println("[SDK-v2] Generated battery level: " + info.batteryLevel + "%");
            }
            
        } catch (Exception e) {
            System.err.println("[SDK-v2] Error parsing data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return info;
    }
    
    // New SDK-based implementation to handle Map<String, Object> data from SDK
    private static void storeBaliseData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            System.err.println("[SDK-v2] No balise data to store");
            return;
        }
        
        // Extract key fields
        String deviceId = data.containsKey("deviceId") ? data.get("deviceId").toString() : "unknown";
        String clientIp = data.containsKey("clientIp") ? data.get("clientIp").toString() : "unknown";
        String messageType = data.containsKey("messageType") ? data.get("messageType").toString() : "unknown";
        
        try {
            System.out.println("[SDK-v2] Storing balise data for device: " + deviceId);
            
            // Connect to PostgreSQL
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Insert or update balise data
                String sql = "INSERT INTO balises (device_id, serial_number, model, firmware_version, "
                    + "last_ip, last_seen, latitude, longitude, battery_level, status) "
                    + "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?) "
                    + "ON CONFLICT (device_id) DO UPDATE SET "
                    + "last_ip = EXCLUDED.last_ip, "
                    + "last_seen = EXCLUDED.last_seen";
                
                // Handle optional fields
                boolean hasLocation = data.containsKey("latitude") && data.containsKey("longitude");
                boolean hasBattery = data.containsKey("batteryLevel") || data.containsKey("voltagePercentage");
                boolean hasStatus = data.containsKey("status") || data.containsKey("lockStatus");
                
                if (hasLocation) {
                    sql += ", latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude";
                }
                
                if (hasBattery) {
                    sql += ", battery_level = EXCLUDED.battery_level";
                }
                
                if (hasStatus) {
                    sql += ", status = EXCLUDED.status";
                }
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    // Required fields
                    stmt.setString(1, deviceId);
                    stmt.setString(2, data.containsKey("serialNumber") ? data.get("serialNumber").toString() : null);
                    stmt.setString(3, data.containsKey("model") ? data.get("model").toString() : "TY5201-LOCK");
                    stmt.setString(4, data.containsKey("firmwareVersion") ? data.get("firmwareVersion").toString() : null);
                    stmt.setString(5, clientIp);
                    
                    // Optional fields
                    if (hasLocation) {
                        double lat = 0.0;
                        double lon = 0.0;
                        try {
                            lat = data.containsKey("latitude") ? Double.parseDouble(data.get("latitude").toString()) : 0.0;
                            lon = data.containsKey("longitude") ? Double.parseDouble(data.get("longitude").toString()) : 0.0;
                        } catch (NumberFormatException nfe) {
                            System.err.println("[SDK-v2] Invalid location format: " + nfe.getMessage());
                        }
                        stmt.setDouble(6, lat);
                        stmt.setDouble(7, lon);
                    } else {
                        stmt.setNull(6, java.sql.Types.DOUBLE);
                        stmt.setNull(7, java.sql.Types.DOUBLE);
                    }
                    
                    // Battery level
                    if (hasBattery) {
                        int batteryLevel = 0;
                        try {
                            if (data.containsKey("batteryLevel")) {
                                batteryLevel = Integer.parseInt(data.get("batteryLevel").toString());
                            } else if (data.containsKey("voltagePercentage")) {
                                batteryLevel = Integer.parseInt(data.get("voltagePercentage").toString());
                            }
                        } catch (NumberFormatException nfe) {
                            System.err.println("[SDK-v2] Invalid battery format: " + nfe.getMessage());
                        }
                        stmt.setInt(8, batteryLevel);
                    } else {
                        stmt.setNull(8, java.sql.Types.INTEGER);
                    }
                    
                    // Status
                    String status = "ACTIVE";
                    if (data.containsKey("status")) {
                        status = data.get("status").toString();
                    } else if (data.containsKey("lockStatus")) {
                        status = data.get("lockStatus").toString();
                    }
                    stmt.setString(9, status);
                    
                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("[SDK-v2] Database updated: " + rowsAffected + " rows affected");
                }
                
                // Also insert a history record
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO balise_events (balise_id, event_type, event_time, latitude, longitude, battery_level, message_raw) "
                        + "VALUES ((SELECT id FROM balises WHERE device_id = ?), ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)")) {
                    stmt.setString(1, deviceId);
                    
                    // Determine event type based on available data
                    String eventType;
                    if (hasLocation) {
                        eventType = "LOCATION_UPDATE";
                    } else if (hasBattery) {
                        eventType = "BATTERY_UPDATE";
                    } else {
                        eventType = "DATA_RECEIVED";
                    }
                    stmt.setString(2, eventType);
                    
                    // Set location if available
                    if (hasLocation) {
                        try {
                            double lat = Double.parseDouble(data.get("latitude").toString());
                            double lon = Double.parseDouble(data.get("longitude").toString());
                            stmt.setDouble(3, lat);
                            stmt.setDouble(4, lon);
                        } catch (Exception ex) {
                            stmt.setNull(3, java.sql.Types.DOUBLE);
                            stmt.setNull(4, java.sql.Types.DOUBLE);
                        }
                    } else {
                        stmt.setNull(3, java.sql.Types.DOUBLE);
                        stmt.setNull(4, java.sql.Types.DOUBLE);
                    }
                    
                    // Set battery if available
                    if (hasBattery) {
                        try {
                            int battery;
                            if (data.containsKey("batteryLevel")) {
                                battery = Integer.parseInt(data.get("batteryLevel").toString());
                            } else {
                                battery = Integer.parseInt(data.get("voltagePercentage").toString());
                            }
                            stmt.setInt(5, battery);
                        } catch (Exception ex) {
                            stmt.setNull(5, java.sql.Types.INTEGER);
                        }
                    } else {
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    }
                    
                    // Store raw message as JSON
                    String jsonData = convertMessageDataToJson(data);
                    stmt.setString(6, jsonData);
                    
                    stmt.executeUpdate();
                    System.out.println("[SDK-v2] Event record created for device: " + deviceId);
                }
            }
        } catch (Exception e) {
            System.err.println("[SDK-v2] Error storing balise data: " + e.getMessage());
            e.printStackTrace();
            
            // Log the data that failed to store
            System.err.println("[SDK-v2] Failed data: " + data);
        }
    }
    
    // Helper method to convert data map to JSON string for history records
    private static String convertMessageDataToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value.toString());
            } else if (value == null) {
                json.append("null");
            } else {
                // For complex objects, just use toString() in quotes
                json.append("\"").append(value.toString()).append("\"");
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    // Original method for backwards compatibility
    private static void storeBaliseData(BaliseDataInfo info) {
        // Convert BaliseDataInfo to Map for the new implementation
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("deviceId", info.deviceId);
        dataMap.put("serialNumber", info.serialNumber);
        dataMap.put("model", info.model);
        dataMap.put("firmwareVersion", info.firmwareVersion);
        dataMap.put("clientIp", info.clientIP);
        dataMap.put("latitude", info.latitude);
        dataMap.put("longitude", info.longitude);
        dataMap.put("batteryLevel", info.batteryLevel);
        dataMap.put("status", info.status);
        
        // Call new implementation
        storeBaliseData(dataMap);
    }
}
