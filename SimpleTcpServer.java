// Simple TCP Server with SDK Stub Implementation
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

// DISABLED: Custom TCP server is not allowed with SDK integration
// public class SimpleTcpServer {
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    // Thread pool for async tasks
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLE TCP SERVER FOR MAXVISION SDK: Starting at " + LocalDateTime.now() + " ===");
        
        // Initialize database connection
        initDatabase();
        
        // Start TCP server
        int port = 6060;
        startTcpServer(port);
        
        // Heartbeat thread
        executor.submit(() -> {
            try {
                while (true) {
                    Thread.sleep(60000); // 1 minute
                    System.out.println("HEARTBEAT: Simple TCP Server alive at " + LocalDateTime.now());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        System.out.println("Simple TCP Server is running successfully");
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
    
    private static void startTcpServer(int port) {
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("=== Simple TCP Server listening on port " + port + " ===");
                
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        executor.submit(() -> handleConnection(socket));
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
    
    private static void handleConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        System.out.println("New connection from " + clientIP);
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received from " + clientIP + ": " + line);
                
                // Store data in database
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("deviceId", "SIMPLE-" + clientIP.replace('.', '-'));
                dataMap.put("clientIp", clientIP);
                dataMap.put("messageType", "DATA");
                dataMap.put("rawMessage", line);
                
                // Process data
                storeBaliseData(dataMap);
                
                // Echo response
                writer.println("RECEIVED: " + line);
            }
            
            System.out.println("Connection closed with " + clientIP);
        } catch (IOException e) {
            System.err.println("Error with " + clientIP + ": " + e.getMessage());
        }
    }
    
    private static void storeBaliseData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            System.err.println("No balise data to store");
            return;
        }
        
        // Extract key fields
        String deviceId = data.containsKey("deviceId") ? data.get("deviceId").toString() : "unknown";
        String clientIp = data.containsKey("clientIp") ? data.get("clientIp").toString() : "unknown";
        
        try {
            System.out.println("Storing balise data for device: " + deviceId);
            
            // Connect to PostgreSQL
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Insert or update balise data
                String sql = "INSERT INTO balises (device_id, serial_number, model, last_ip, last_seen, status) "
                    + "VALUES (?, ?, 'SIMPLE-TCP', ?, NOW(), 'ACTIVE') "
                    + "ON CONFLICT (device_id) DO UPDATE SET "
                    + "last_ip = EXCLUDED.last_ip, "
                    + "last_seen = EXCLUDED.last_seen";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, deviceId);
                    stmt.setString(2, deviceId); // Using deviceId as serial number too
                    stmt.setString(3, clientIp);
                    
                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("Database updated: " + rowsAffected + " rows affected");
                }
                
                // Also insert a history record
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO balise_events (balise_id, event_type, event_time, message_raw) "
                        + "VALUES ((SELECT id FROM balises WHERE device_id = ?), 'DATA_RECEIVED', CURRENT_TIMESTAMP, ?)")) {
                    stmt.setString(1, deviceId);
                    stmt.setString(2, convertToJson(data));
                    
                    stmt.executeUpdate();
                    System.out.println("Event record created for device: " + deviceId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error storing balise data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Simple JSON conversion
    private static String convertToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) json.append(",");
            
            String key = entry.getKey();
            Object value = entry.getValue();
            
            json.append("\"").append(key).append("\":");
            
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value == null) {
                json.append("null");
            } else {
                json.append(value.toString());
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}
