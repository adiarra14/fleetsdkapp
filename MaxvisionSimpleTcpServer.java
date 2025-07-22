// Simple Maxvision TCP Server Implementation for Balise Management
// Version 3.0 (2025-07-22) - Simplified standalone server with direct socket handling
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.*;
import java.util.*;
import com.sun.net.httpserver.*;

public class MaxvisionSimpleTcpServer {
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    // Flag to track database connection status
    private static boolean databaseConnected = false;
    
    // Legacy regex patterns for parsing
    private static final Pattern DEVICE_INFO_PATTERN = Pattern.compile("\\+TY5201-LOCK-MAIN_V([\\d\\.]+)_(\\d+)_V([\\d\\.]+)_Alpha");
    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("8937\\d+F;(\\d+)");
    
    // Thread pool for async tasks
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== MAXVISION SIMPLE TCP SERVER: Starting at " + LocalDateTime.now() + " ===");
        
        // Start HTTP health endpoint (first for quick health response)
        startHealthServer();
        
        // Initialize database connection (with retry)
        initDatabaseWithRetry();
        
        // Start basic TCP server for balise connections - NO SDK DEPENDENCY
        int port = 6060;
        startDirectTcpServer(port);
        
        // Heartbeat thread
        executor.submit(() -> {
            try {
                while (true) {
                    Thread.sleep(60000); // 1 minute
                    System.out.println("HEARTBEAT: Maxvision Simple TCP Server v3.0 alive at " + LocalDateTime.now());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("Maxvision Simple TCP Server v3.0 is running successfully");
        System.out.println("TCP Server is active on port 6060");
    }
    
    private static void startHealthServer() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Health endpoint that returns status based on database connection
            httpServer.createContext("/health", exchange -> {
                String response = "Maxvision Simple TCP Server v3.0 - " + LocalDateTime.now() + "\n";
                if (databaseConnected) {
                    response += "Status: HEALTHY - Database connected";
                    exchange.sendResponseHeaders(200, response.length());
                } else {
                    response += "Status: DEGRADED - Database not connected";
                    exchange.sendResponseHeaders(503, response.length());
                }
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            
            // Root endpoint (always healthy)
            httpServer.createContext("/", exchange -> {
                String response = "Maxvision Simple TCP Server v3.0 - " + LocalDateTime.now();
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            
            httpServer.setExecutor(executor);
            httpServer.start();
            System.out.println("=== Health server started on port 8080 ===");
            
        } catch (Exception e) {
            System.err.println("Warning: Could not start health server: " + e.getMessage());
        }
    }
    
    private static void initDatabaseWithRetry() {
        executor.submit(() -> {
            int retries = 30;
            int delaySeconds = 5;
            
            while (retries > 0 && !databaseConnected) {
                try {
                    Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                    
                    // Check if balises table exists
                    DatabaseMetaData meta = conn.getMetaData();
                    ResultSet tables = meta.getTables(null, null, "balises", null);
                    
                    if (!tables.next()) {
                        System.out.println("=== Warning: balises table does not exist ===");
                        System.out.println("=== Please create the database schema manually ===");
                    } else {
                        // Count balises
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM balises");
                        rs.next();
                        int count = rs.getInt(1);
                        System.out.println("=== Database ready with " + count + " balises ===");
                    }
                    
                    conn.close();
                    databaseConnected = true;
                    System.out.println("=== Database connection established successfully ===");
                    
                } catch (Exception e) {
                    retries--;
                    System.err.println("Database connection error (" + retries + " retries left): " + e.getMessage());
                    try {
                        Thread.sleep(delaySeconds * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            if (!databaseConnected) {
                System.err.println("=== WARNING: Failed to connect to database after multiple retries ===");
                System.err.println("=== Server will continue running but data will not be stored ===");
            }
        });
    }
    
    private static void startDirectTcpServer(int port) {
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("=== MAXVISION Simple TCP Server listening on port " + port + " ===");
                
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress());
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
    
    private static void handleBaliseConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        System.out.println("Handling connection from " + clientIP);
        
        try (
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received from " + clientIP + ": " + line);
                
                // Process the received data
                processBaliseData(line, clientIP);
                
                // Send acknowledgment back to the balise
                String response = "ACK\n";
                output.write(response.getBytes());
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Connection closed: " + clientIP);
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private static void processBaliseData(String data, String clientIP) {
        BaliseDataInfo info = parseBaliseData(data, clientIP);
        if (info != null) {
            System.out.println("Parsed balise data: deviceId=" + info.deviceId + ", sn=" + info.serialNumber);
            storeBaliseData(info);
        }
    }
    
    private static class BaliseDataInfo {
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
        System.out.println("[DATA-PARSER] Parsing data: " + data);
        
        BaliseDataInfo info = new BaliseDataInfo();
        info.clientIP = clientIP;
        
        try {
            // Try to extract device ID
            Matcher deviceIdMatcher = DEVICE_ID_PATTERN.matcher(data);
            if (deviceIdMatcher.find()) {
                info.deviceId = deviceIdMatcher.group(1);
                info.serialNumber = info.deviceId; // Use device ID as serial number if nothing else found
            }
            
            // Try to extract firmware version and serial from device info
            Matcher deviceInfoMatcher = DEVICE_INFO_PATTERN.matcher(data);
            if (deviceInfoMatcher.find()) {
                info.firmwareVersion = deviceInfoMatcher.group(1) + "." + deviceInfoMatcher.group(3);
                info.serialNumber = deviceInfoMatcher.group(2);
            }
            
            // Try to extract battery level (basic parsing for common formats)
            if (data.contains("BAT:")) {
                int batIndex = data.indexOf("BAT:");
                if (batIndex >= 0 && batIndex + 4 < data.length()) {
                    String batValue = data.substring(batIndex + 4).split("[^0-9]", 2)[0];
                    try {
                        info.batteryLevel = Integer.parseInt(batValue);
                    } catch (NumberFormatException e) {
                        // Ignore parsing errors
                    }
                }
            }
            
            // Try to extract GPS coordinates (basic parsing for common formats)
            if (data.contains("GPS:")) {
                int gpsIndex = data.indexOf("GPS:");
                if (gpsIndex >= 0) {
                    String gpsData = data.substring(gpsIndex + 4);
                    String[] parts = gpsData.split(",");
                    if (parts.length >= 2) {
                        try {
                            info.latitude = Double.parseDouble(parts[0]);
                            info.longitude = Double.parseDouble(parts[1]);
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    }
                }
            }
            
            // If we couldn't parse a device ID, generate one from IP as fallback
            if (info.deviceId == null) {
                info.deviceId = "UNKNOWN-" + clientIP.replace(".", "-");
                System.out.println("[PARSER-WARNING] Could not parse device ID, using: " + info.deviceId);
            }
            
            return info;
        } catch (Exception e) {
            System.err.println("Error parsing balise data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void storeBaliseData(BaliseDataInfo info) {
        if (!databaseConnected) {
            System.out.println("Database not connected, skipping data storage");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // First, check if the device exists in the balises table
            String checkSql = "SELECT id FROM balises WHERE device_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, info.deviceId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    // Device doesn't exist, insert it
                    String insertSql = "INSERT INTO balises (device_id, serial_number, model, status, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setString(1, info.deviceId);
                        insertStmt.setString(2, info.serialNumber);
                        insertStmt.setString(3, info.model);
                        insertStmt.setString(4, info.status);
                        insertStmt.executeUpdate();
                        System.out.println("Inserted new balise: " + info.deviceId);
                    }
                }
            }
            
            // Now record the event
            String eventSql = "INSERT INTO balise_events (device_id, event_time, latitude, longitude, battery_level, raw_data) VALUES (?, NOW(), ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(eventSql)) {
                stmt.setString(1, info.deviceId);
                
                if (info.latitude != null && info.longitude != null) {
                    stmt.setDouble(2, info.latitude);
                    stmt.setDouble(3, info.longitude);
                } else {
                    stmt.setNull(2, Types.DOUBLE);
                    stmt.setNull(3, Types.DOUBLE);
                }
                
                if (info.batteryLevel != null) {
                    stmt.setInt(4, info.batteryLevel);
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                
                // Create a simple JSON representation of the data
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("{");
                jsonBuilder.append("\"deviceId\":\"").append(info.deviceId).append("\",");
                jsonBuilder.append("\"serialNumber\":\"").append(info.serialNumber).append("\",");
                jsonBuilder.append("\"model\":\"").append(info.model).append("\",");
                if (info.firmwareVersion != null) {
                    jsonBuilder.append("\"firmwareVersion\":\"").append(info.firmwareVersion).append("\",");
                }
                jsonBuilder.append("\"clientIP\":\"").append(info.clientIP).append("\",");
                if (info.latitude != null) {
                    jsonBuilder.append("\"latitude\":").append(info.latitude).append(",");
                }
                if (info.longitude != null) {
                    jsonBuilder.append("\"longitude\":").append(info.longitude).append(",");
                }
                if (info.batteryLevel != null) {
                    jsonBuilder.append("\"batteryLevel\":").append(info.batteryLevel).append(",");
                }
                jsonBuilder.append("\"status\":\"").append(info.status).append("\"");
                jsonBuilder.append("}");
                
                stmt.setString(5, jsonBuilder.toString());
                stmt.executeUpdate();
                System.out.println("Recorded event for balise: " + info.deviceId);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error storing balise data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
