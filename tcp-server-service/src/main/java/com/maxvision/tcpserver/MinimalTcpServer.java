package com.maxvision.tcpserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.charset.StandardCharsets;

/**
 * Enhanced TCP server implementation for real balise protocol handling.
 * This provides TCP server on port 6060 for balise connections, HTTP health endpoint,
 * database integration for storing balise data, and basic protocol parsing.
 */
public class MinimalTcpServer {
    
    // Database connection details
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    // Simple GPS data pattern (basic NMEA-like format)
    private static final Pattern GPS_PATTERN = Pattern.compile(
        "\\$GPRMC,([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*),([^,]*)\\*([A-F0-9]{2})"
    );
    
    public static void main(String[] args) throws IOException {
        System.out.println("=== Starting Minimal Fleet SDK TCP Server ===");
        
        // Parse HTTP port from environment or use default
        int httpPort = 6060;
        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                httpPort = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid SERVER_PORT value, using default 6060");
            }
        }
        
        // Parse TCP port from environment or use default
        int tcpPort = 6060;
        String tcpPortEnv = System.getenv("TCP_SERVER_PORT");
        if (tcpPortEnv != null && !tcpPortEnv.isEmpty()) {
            try {
                tcpPort = Integer.parseInt(tcpPortEnv);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid TCP_SERVER_PORT value, using default 6060");
            }
        }
        
        // Start HTTP server for health checks
        startHttpServer(httpPort);
        
        // Start TCP server for balise connections
        startTcpServer(tcpPort);
    }
    
    private static void startHttpServer(int port) throws IOException {
        // Create HTTP server on the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Add health check endpoint
        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Fleet SDK TCP Server is running - " + LocalDateTime.now();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        
        // Add home endpoint
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "Fleet SDK TCP Server - Ready and Operational";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        
        // Start the server
        server.start();
        System.out.println("=== Minimal Fleet SDK TCP Server HTTP endpoint started on port " + port + " ===");
    }
    
    private static void startTcpServer(int port) {
        // Create thread pool for handling TCP connections
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // Start TCP server in a separate thread
        executor.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("=== Minimal Fleet SDK TCP Server listening for connections on port " + port + " ===");
                
                while (true) {
                    try {
                        // Accept incoming connection
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress());
                        
                        // Handle connection in a separate thread
                        executor.submit(() -> handleTcpConnection(clientSocket));
                    } catch (IOException e) {
                        System.out.println("Error accepting connection: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println("Error starting TCP server: " + e.getMessage());
            }
        });
    }
    
    private static void handleTcpConnection(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();
        System.out.println("[BALISE] New connection from " + clientIP);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[BALISE] Received data from " + clientIP + ": " + line);
                
                // Process the received balise data
                processBaliseData(line, clientIP);
                
                // Send acknowledgment back to balise
                writer.println("ACK");
            }
            
        } catch (IOException e) {
            System.out.println("[BALISE] Connection error with " + clientIP + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("[BALISE] Connection closed with " + clientIP);
            } catch (IOException e) {
                System.out.println("[BALISE] Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private static void processBaliseData(String data, String clientIP) {
        try {
            // Try to parse as GPS NMEA data
            Matcher matcher = GPS_PATTERN.matcher(data);
            if (matcher.matches()) {
                processGPSData(matcher, clientIP);
                return;
            }
            
            // Try to parse as simple balise status format: IMEI,STATUS,BATTERY,LAT,LON
            if (data.contains(",")) {
                processSimpleBaliseData(data, clientIP);
                return;
            }
            
            // Store raw data if no specific format recognized
            storeRawBaliseData(data, clientIP);
            
        } catch (Exception e) {
            System.out.println("[BALISE] Error processing data from " + clientIP + ": " + e.getMessage());
        }
    }
    
    private static void processGPSData(Matcher matcher, String clientIP) {
        try {
            String time = matcher.group(1);
            String status = matcher.group(2);
            String latitude = matcher.group(3);
            String latDirection = matcher.group(4);
            String longitude = matcher.group(5);
            String lonDirection = matcher.group(6);
            String speed = matcher.group(7);
            String course = matcher.group(8);
            String date = matcher.group(9);
            
            if ("A".equals(status) && !latitude.isEmpty() && !longitude.isEmpty()) {
                double lat = convertNMEACoordinate(latitude, latDirection);
                double lon = convertNMEACoordinate(longitude, lonDirection);
                double speedKnots = speed.isEmpty() ? 0.0 : Double.parseDouble(speed);
                double heading = course.isEmpty() ? 0.0 : Double.parseDouble(course);
                
                storeBaliseEvent(clientIP, "GPS", lat, lon, speedKnots, heading, matcher.group(0));
                System.out.println(String.format("[BALISE] GPS data from %s: %.6f,%.6f speed=%.1f heading=%.1f", 
                    clientIP, lat, lon, speedKnots, heading));
            }
        } catch (Exception e) {
            System.out.println("[BALISE] Error parsing GPS data: " + e.getMessage());
        }
    }
    
    private static void processSimpleBaliseData(String data, String clientIP) {
        try {
            String[] parts = data.split(",");
            if (parts.length >= 5) {
                String imei = parts[0].trim();
                String status = parts[1].trim();
                double battery = Double.parseDouble(parts[2].trim());
                double lat = Double.parseDouble(parts[3].trim());
                double lon = Double.parseDouble(parts[4].trim());
                
                // Update or create balise record
                updateBaliseRecord(imei, status, battery, lat, lon, clientIP);
                storeBaliseEvent(imei, status, lat, lon, 0.0, 0.0, data);
                
                System.out.println(String.format("[BALISE] Simple data from %s (IMEI: %s): %.6f,%.6f battery=%.1f%% status=%s", 
                    clientIP, imei, lat, lon, battery, status));
            }
        } catch (Exception e) {
            System.out.println("[BALISE] Error parsing simple balise data: " + e.getMessage());
        }
    }
    
    private static double convertNMEACoordinate(String coord, String direction) {
        if (coord.length() < 4) return 0.0;
        
        int dotIndex = coord.indexOf('.');
        if (dotIndex < 3) return 0.0;
        
        double degrees = Double.parseDouble(coord.substring(0, dotIndex - 2));
        double minutes = Double.parseDouble(coord.substring(dotIndex - 2));
        double result = degrees + minutes / 60.0;
        
        if ("S".equals(direction) || "W".equals(direction)) {
            result = -result;
        }
        
        return result;
    }
    
    private static void updateBaliseRecord(String imei, String status, double battery, double lat, double lon, String clientIP) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO balises (imei, name, status, battery_level, last_seen, location) " +
                        "VALUES (?, ?, ?, ?, NOW(), ST_SetSRID(ST_MakePoint(?, ?), 4326)) " +
                        "ON CONFLICT (imei) DO UPDATE SET " +
                        "status = EXCLUDED.status, battery_level = EXCLUDED.battery_level, " +
                        "last_seen = EXCLUDED.last_seen, location = EXCLUDED.location";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, imei);
                stmt.setString(2, "Balise-" + imei.substring(Math.max(0, imei.length() - 4)));
                stmt.setString(3, status);
                stmt.setDouble(4, battery);
                stmt.setDouble(5, lon);
                stmt.setDouble(6, lat);
                
                int updated = stmt.executeUpdate();
                System.out.println("[DATABASE] Updated balise record for IMEI: " + imei + " (" + updated + " rows)");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE] Error updating balise record: " + e.getMessage());
        }
    }
    
    private static void storeBaliseEvent(String identifier, String eventType, double lat, double lon, double speed, double heading, String rawMessage) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // First, get or create balise ID
            Integer baliseId = getBaliseId(conn, identifier);
            if (baliseId == null) {
                System.out.println("[DATABASE] Could not find/create balise for identifier: " + identifier);
                return;
            }
            
            String sql = "INSERT INTO balise_events (balise_id, event_type, location, speed, heading, message_raw) " +
                        "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, baliseId);
                stmt.setString(2, eventType);
                stmt.setDouble(3, lon);
                stmt.setDouble(4, lat);
                stmt.setDouble(5, speed);
                stmt.setDouble(6, heading);
                stmt.setString(7, rawMessage);
                
                int inserted = stmt.executeUpdate();
                System.out.println("[DATABASE] Stored balise event for " + identifier + " (" + inserted + " rows)");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE] Error storing balise event: " + e.getMessage());
        }
    }
    
    private static void storeRawBaliseData(String data, String clientIP) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Store as raw event with client IP as identifier
            Integer baliseId = getBaliseId(conn, clientIP);
            if (baliseId == null) return;
            
            String sql = "INSERT INTO balise_events (balise_id, event_type, message_raw) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, baliseId);
                stmt.setString(2, "RAW");
                stmt.setString(3, data);
                
                stmt.executeUpdate();
                System.out.println("[DATABASE] Stored raw data from " + clientIP);
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE] Error storing raw data: " + e.getMessage());
        }
    }
    
    private static Integer getBaliseId(Connection conn, String identifier) throws SQLException {
        // Try to find existing balise by IMEI or IP
        String selectSql = "SELECT id FROM balises WHERE imei = ? OR name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, "Balise-" + identifier);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        // Create new balise if not found
        String insertSql = "INSERT INTO balises (imei, name, status, created_at) VALUES (?, ?, ?, NOW()) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, identifier);
            stmt.setString(2, "Balise-" + identifier);
            stmt.setString(3, "UNKNOWN");
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt("id");
                    System.out.println("[DATABASE] Created new balise record with ID: " + newId + " for " + identifier);
                    return newId;
                }
            }
        }
        
        return null;
    }
    
    static {
        // Load PostgreSQL JDBC driver
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("[DATABASE] PostgreSQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("[DATABASE] Warning: PostgreSQL JDBC driver not found - database features may not work");
        }
    }
}
