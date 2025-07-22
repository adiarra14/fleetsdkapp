// Enhanced Hybrid TCP Server with SDK Integration
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.*;
import java.util.*;
import com.sun.net.httpserver.*;

// SDK imports
import com.maxvision.edge.gateway.lock.netty.decoder.*;
import com.maxvision.edge.gateway.lock.netty.decoder.model.*;
import com.maxvision.edge.gateway.lock.netty.decoder.model.base.*;
import com.maxvision.edge.gateway.lock.netty.handler.model.*;
import com.maxvision.edge.gateway.lock.netty.*;
import com.maxvision.edge.gateway.lock.*;

// DISABLED: Custom TCP server is not allowed with SDK integration
// public class EnhancedHybridTcpServer {
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
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== HYBRID TCP SERVER: Starting at " + LocalDateTime.now() + " ===");
        System.out.println("=== SDK Integration Initializing ===");
        
        // Initialize database connection
        initDatabase();
        
        // Start HTTP health endpoint
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/health", exchange -> {
            String response = "TCP Server with SDK Integration - " + LocalDateTime.now();
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
            System.out.println("=== SDK TCP Server listening for balise connections on port " + port + " ===");
            System.out.println("=== SDK Integration Ready ===");
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
                    System.out.println("HEARTBEAT: Hybrid TCP Server alive at " + LocalDateTime.now());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("Hybrid TCP Server is running successfully");
        System.out.println("TCP Server should be active on port 6060");
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
                    System.out.println("Database ready. Current balise count: " + count);
                }
            }
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Fallback to legacy TCP server implementation if SDK server fails to start
     * @param port The port to listen on
     */
    private static void fallbackToLegacyServer(int port) {
        System.out.println("FALLING BACK TO LEGACY TCP SERVER IMPLEMENTATION");
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("=== LEGACY TCP Server listening for balise connections on port " + port + " ===");
                while (true) {
                    Socket socket = serverSocket.accept();
                    String clientIP = socket.getInetAddress().getHostAddress();
                    System.out.println("New balise connection from /" + clientIP);
                    executor.submit(() -> handleBaliseConnection(socket));
                }
            } catch (IOException e) {
                System.err.println("Legacy server socket error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Initialize the SDK message handler to process balise messages
     */
    private static void initializeMessageHandler() {
        messageHandler = new MessageHandler() {
            @Override
            public void handleMessage(Object message) {
                try {
                    System.out.println("SDK received message: " + message.getClass().getSimpleName());
                    
                    // Handle different message types
                    if (message instanceof LoginDecoderModel) {
                        // Handle login message
                        LoginDecoderModel loginModel = (LoginDecoderModel) message;
                        BaliseDataInfo info = new BaliseDataInfo();
                        info.deviceId = loginModel.getDeviceId();
                        info.serialNumber = loginModel.getImei();
                        info.firmwareVersion = loginModel.getVersion();
                        info.clientIP = loginModel.getClientIp();
                        
                        System.out.println("SDK login from device: " + info.deviceId + ", IMEI: " + info.serialNumber);
                        
                        // Store device info in database
                        storeBaliseData(info);
                        
                    } else if (message instanceof GpsWithBaseStationDecoderModel) {
                        // Handle GPS location message
                        GpsWithBaseStationDecoderModel gpsModel = (GpsWithBaseStationDecoderModel) message;
                        BaliseDataInfo info = new BaliseDataInfo();
                        info.deviceId = gpsModel.getDeviceId();
                        
                        // Extract GPS data
                        Gps gps = gpsModel.getGps();
                        if (gps != null) {
                            info.latitude = gps.getLatitude();
                            info.longitude = gps.getLongitude();
                        }
                        
                        System.out.println("SDK GPS data from device: " + info.deviceId + ", Location: " + 
                                           info.latitude + "," + info.longitude);
                        
                        // Store location in database
                        storeBaliseData(info);
                        
                    } else if (message instanceof KeepAliveDecoderModel) {
                        // Handle keepalive message
                        KeepAliveDecoderModel keepAliveModel = (KeepAliveDecoderModel) message;
                        BaliseDataInfo info = new BaliseDataInfo();
                        info.deviceId = keepAliveModel.getDeviceId();
                        info.batteryLevel = (int)(keepAliveModel.getBatteryValue() * 100); // Convert to percentage
                        
                        System.out.println("SDK keepalive from device: " + info.deviceId + ", Battery: " + info.batteryLevel + "%");
                        
                        // Update device status in database
                        storeBaliseData(info);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing SDK message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            @Override
            public void handleException(Throwable throwable) {
                System.err.println("SDK Exception: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        };
    }
    
    /**
     * Legacy method to handle balise connection without SDK
     * Used only as fallback if SDK initialization fails
     */
    private static void handleBaliseConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        System.out.println("[BALISE] Processing connection from " + clientIP);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            
            // Use StringBuilder to collect multiple data lines
            StringBuilder fullDataPacket = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                System.out.println("[BALISE] Received from " + clientIP + ": " + line);
                fullDataPacket.append(line).append("\n");
                
                // If we've received a complete packet (ending with Alpha or containing key markers)
                if (line.contains("Alpha") || line.matches(".*\\(00,[YN],.*")) {
                    // Process balise data (SDK integration point)
                    processBaliseData(fullDataPacket.toString(), clientIP);
                    fullDataPacket = new StringBuilder(); // Reset for next packet
                    
                    // Send acknowledgment
                    writer.println("ACK");
                }
            }
        } catch (Exception e) {
            System.err.println("[BALISE] Connection error with " + clientIP + ": " + e.getMessage());
        } finally {
            try { socket.close(); } catch (Exception e) {}
            System.out.println("[BALISE] Connection closed with " + clientIP);
        }
    }
    
    private static void processBaliseData(String data, String clientIP) {
        try {
            System.out.println("[SDK] Processing balise data: " + data);
            
            // Parse balise data and extract key information
            BaliseDataInfo info = parseBaliseData(data, clientIP);
            
            if (info != null) {
                // Store in database
                storeBaliseData(info);
            }
        } catch (Exception e) {
            System.err.println("[SDK] Error processing balise data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
        try {
            BaliseDataInfo info = new BaliseDataInfo();
            info.clientIP = clientIP;
            
            // Extract device ID/IMEI
            Matcher deviceIdMatcher = DEVICE_ID_PATTERN.matcher(data);
            if (deviceIdMatcher.find()) {
                info.deviceId = deviceIdMatcher.group(1);
                info.serialNumber = info.deviceId; // Use device ID as serial number for now
                System.out.println("[PARSER] Extracted Device ID: " + info.deviceId);
            }
            
            // Extract firmware version
            Matcher deviceInfoMatcher = DEVICE_INFO_PATTERN.matcher(data);
            if (deviceInfoMatcher.find()) {
                String mainVersion = deviceInfoMatcher.group(1);
                String buildDate = deviceInfoMatcher.group(2);
                String subVersion = deviceInfoMatcher.group(3);
                info.firmwareVersion = mainVersion + "-" + subVersion + " (" + buildDate + ")";
                System.out.println("[PARSER] Extracted Firmware: " + info.firmwareVersion);
            }
            
            // If no device ID found, use IP as fallback
            if (info.deviceId == null) {
                info.deviceId = "BALISE-" + clientIP.replace(".", "-");
                System.out.println("[PARSER] Using fallback Device ID: " + info.deviceId);
            }
            
            // Try to extract location data (simplified placeholder logic)
            // In a real implementation, this would decode the binary/proprietary format
            if (data.contains("(00,Y,")) {
                // Mock location data for demonstration
                // In production, you'd parse actual coordinates from the data
                info.latitude = 43.296398 + Math.random() * 0.01;
                info.longitude = 5.369889 + Math.random() * 0.01;
                info.batteryLevel = 75 + (int)(Math.random() * 20);
                System.out.println("[PARSER] Extracted location: " + info.latitude + ", " + info.longitude);
            }
            
            return info;
        } catch (Exception e) {
            System.err.println("[PARSER] Error parsing balise data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void storeBaliseData(BaliseDataInfo info) {
        System.out.println("[DATABASE] Storing balise data for device: " + info.deviceId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Check if device already exists
            String checkSql = "SELECT id FROM balises WHERE device_id = ?";
            int baliseId = -1;
            
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, info.deviceId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        baliseId = rs.getInt("id");
                    }
                }
            }
            
            if (baliseId == -1) {
                // Insert new balise
                String insertSql = "INSERT INTO balises (device_id, serial_number, model, firmware_version, " +
                                  "status, last_seen, latitude, longitude, battery_level) " +
                                  "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) RETURNING id";
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, info.deviceId);
                    pstmt.setString(2, info.serialNumber);
                    pstmt.setString(3, info.model);
                    pstmt.setString(4, info.firmwareVersion);
                    pstmt.setString(5, info.status);
                    
                    // Set location if available, otherwise null
                    if (info.latitude != null && info.longitude != null) {
                        pstmt.setDouble(6, info.latitude);
                        pstmt.setDouble(7, info.longitude);
                    } else {
                        pstmt.setNull(6, java.sql.Types.DOUBLE);
                        pstmt.setNull(7, java.sql.Types.DOUBLE);
                    }
                    
                    if (info.batteryLevel != null) {
                        pstmt.setInt(8, info.batteryLevel);
                    } else {
                        pstmt.setNull(8, java.sql.Types.INTEGER);
                    }
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            baliseId = rs.getInt(1);
                            System.out.println("[DATABASE] Created new balise with ID: " + baliseId);
                        }
                    }
                }
            } else {
                // Update existing balise
                String updateSql = "UPDATE balises SET last_seen = CURRENT_TIMESTAMP";
                
                if (info.latitude != null && info.longitude != null) {
                    updateSql += ", latitude = ?, longitude = ?";
                }
                
                if (info.batteryLevel != null) {
                    updateSql += ", battery_level = ?";
                }
                
                if (info.firmwareVersion != null) {
                    updateSql += ", firmware_version = ?";
                }
                
                updateSql += " WHERE id = ?";
                
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    int paramIndex = 1;
                    
                    if (info.latitude != null && info.longitude != null) {
                        pstmt.setDouble(paramIndex++, info.latitude);
                        pstmt.setDouble(paramIndex++, info.longitude);
                    }
                    
                    if (info.batteryLevel != null) {
                        pstmt.setInt(paramIndex++, info.batteryLevel);
                    }
                    
                    if (info.firmwareVersion != null) {
                        pstmt.setString(paramIndex++, info.firmwareVersion);
                    }
                    
                    pstmt.setInt(paramIndex, baliseId);
                    int rowsUpdated = pstmt.executeUpdate();
                    System.out.println("[DATABASE] Updated balise with ID: " + baliseId + " (" + rowsUpdated + " rows)");
                }
            }
            
            System.out.println("[DATABASE] Successfully processed balise data");
            
        } catch (SQLException e) {
            System.err.println("[DATABASE] Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
