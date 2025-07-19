package com.maxvision.backend;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced HTTP server implementation for balise management and monitoring.
 * This provides REST API endpoints for balise data, health checks, and management
 * with direct database integration.
 */
public class MinimalBackendServer {
    
    // Database connection details
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    public static void main(String[] args) throws IOException {
        System.out.println("=== Starting Minimal Fleet SDK Backend Service ===");
        
        // Parse port from environment or use default
        int port = 8080;
        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.out.println("Warning: Invalid SERVER_PORT value, using default 8080");
            }
        }
        
        // Create HTTP server on the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Add health check endpoint
        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleHealthCheck(exchange);
            }
        });
        
        // Add home endpoint
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleHome(exchange);
            }
        });
        
        // Add balises list endpoint
        server.createContext("/api/balises", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleBalisesList(exchange);
            }
        });
        
        // Add balise details endpoint
        server.createContext("/api/balise/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleBaliseDetails(exchange);
            }
        });
        
        // Add balise events endpoint
        server.createContext("/api/events", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleBaliseEvents(exchange);
            }
        });
        
        // Add statistics endpoint
        server.createContext("/api/stats", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleStatistics(exchange);
            }
        });
        
        // Start the server
        server.start();
        System.out.println("=== Minimal Fleet SDK Backend Service started on port " + port + " ===");
    }
    
    private static void handleHealthCheck(HttpExchange exchange) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String response = "{\"status\":\"healthy\",\"timestamp\":\"" + LocalDateTime.now() + "\",\"database\":\"connected\"}";
            sendJsonResponse(exchange, 200, response);
        } catch (SQLException e) {
            String response = "{\"status\":\"unhealthy\",\"timestamp\":\"" + LocalDateTime.now() + "\",\"database\":\"disconnected\",\"error\":\"" + e.getMessage() + "\"}";
            sendJsonResponse(exchange, 500, response);
        }
    }
    
    private static void handleHome(HttpExchange exchange) throws IOException {
        String html = "<html><body>" +
                     "<h1>Fleet SDK Backend Service</h1>" +
                     "<p>Ready and Operational - " + LocalDateTime.now() + "</p>" +
                     "<h2>Available APIs:</h2>" +
                     "<ul>" +
                     "<li><a href='/health'>Health Check</a></li>" +
                     "<li><a href='/api/balises'>List All Balises</a></li>" +
                     "<li><a href='/api/events'>Recent Events</a></li>" +
                     "<li><a href='/api/stats'>Statistics</a></li>" +
                     "</ul>" +
                     "</body></html>";
        sendHtmlResponse(exchange, 200, html);
    }
    
    private static void handleBalisesList(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, imei, name, status, battery_level, last_seen, " +
                        "ST_X(location::geometry) as longitude, ST_Y(location::geometry) as latitude " +
                        "FROM balises ORDER BY last_seen DESC";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                StringBuilder json = new StringBuilder("[\n");
                boolean first = true;
                
                while (rs.next()) {
                    if (!first) json.append(",\n");
                    first = false;
                    
                    json.append("  {\n")
                        .append("    \"id\": ").append(rs.getInt("id")).append(",\n")
                        .append("    \"imei\": \"").append(rs.getString("imei")).append("\",\n")
                        .append("    \"name\": \"").append(rs.getString("name")).append("\",\n")
                        .append("    \"status\": \"").append(rs.getString("status")).append("\",\n")
                        .append("    \"battery_level\": ").append(rs.getDouble("battery_level")).append(",\n")
                        .append("    \"last_seen\": \"").append(rs.getTimestamp("last_seen")).append("\",\n")
                        .append("    \"latitude\": ").append(rs.getDouble("latitude")).append(",\n")
                        .append("    \"longitude\": ").append(rs.getDouble("longitude")).append("\n")
                        .append("  }");
                }
                
                json.append("\n]");
                sendJsonResponse(exchange, 200, json.toString());
            }
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    private static void handleBaliseDetails(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String baliseId = path.substring("/api/balise/".length());
        
        if (baliseId.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Balise ID required\"}");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id, imei, name, status, battery_level, last_seen, created_at, " +
                        "ST_X(location::geometry) as longitude, ST_Y(location::geometry) as latitude " +
                        "FROM balises WHERE id = ? OR imei = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, baliseId);
                stmt.setString(2, baliseId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String json = "{\n" +
                                     "  \"id\": " + rs.getInt("id") + ",\n" +
                                     "  \"imei\": \"" + rs.getString("imei") + "\",\n" +
                                     "  \"name\": \"" + rs.getString("name") + "\",\n" +
                                     "  \"status\": \"" + rs.getString("status") + "\",\n" +
                                     "  \"battery_level\": " + rs.getDouble("battery_level") + ",\n" +
                                     "  \"last_seen\": \"" + rs.getTimestamp("last_seen") + "\",\n" +
                                     "  \"created_at\": \"" + rs.getTimestamp("created_at") + "\",\n" +
                                     "  \"latitude\": " + rs.getDouble("latitude") + ",\n" +
                                     "  \"longitude\": " + rs.getDouble("longitude") + "\n" +
                                     "}";
                        sendJsonResponse(exchange, 200, json);
                    } else {
                        sendJsonResponse(exchange, 404, "{\"error\":\"Balise not found\"}");
                    }
                }
            }
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    private static void handleBaliseEvents(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        
        // Parse query parameters for limit
        String query = exchange.getRequestURI().getQuery();
        int limit = 50; // default
        if (query != null && query.contains("limit=")) {
            try {
                String limitStr = query.substring(query.indexOf("limit=") + 6);
                if (limitStr.contains("&")) {
                    limitStr = limitStr.substring(0, limitStr.indexOf("&"));
                }
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                // Use default limit
            }
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT be.id, be.event_type, be.event_time, be.speed, be.heading, be.message_raw, " +
                        "b.imei, b.name, " +
                        "ST_X(be.location::geometry) as longitude, ST_Y(be.location::geometry) as latitude " +
                        "FROM balise_events be " +
                        "JOIN balises b ON be.balise_id = b.id " +
                        "ORDER BY be.event_time DESC LIMIT ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder json = new StringBuilder("[\n");
                    boolean first = true;
                    
                    while (rs.next()) {
                        if (!first) json.append(",\n");
                        first = false;
                        
                        json.append("  {\n")
                            .append("    \"id\": ").append(rs.getInt("id")).append(",\n")
                            .append("    \"event_type\": \"").append(rs.getString("event_type")).append("\",\n")
                            .append("    \"event_time\": \"").append(rs.getTimestamp("event_time")).append("\",\n")
                            .append("    \"balise_imei\": \"").append(rs.getString("imei")).append("\",\n")
                            .append("    \"balise_name\": \"").append(rs.getString("name")).append("\",\n")
                            .append("    \"latitude\": ").append(rs.getDouble("latitude")).append(",\n")
                            .append("    \"longitude\": ").append(rs.getDouble("longitude")).append(",\n")
                            .append("    \"speed\": ").append(rs.getDouble("speed")).append(",\n")
                            .append("    \"heading\": ").append(rs.getDouble("heading")).append(",\n")
                            .append("    \"message_raw\": \"").append(rs.getString("message_raw")).append("\"\n")
                            .append("  }");
                    }
                    
                    json.append("\n]");
                    sendJsonResponse(exchange, 200, json.toString());
                }
            }
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    private static void handleStatistics(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            StringBuilder json = new StringBuilder("{\n");
            
            // Count total balises
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM balises");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    json.append("  \"total_balises\": ").append(rs.getInt(1)).append(",\n");
                }
            }
            
            // Count active balises (seen in last hour)
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM balises WHERE last_seen > NOW() - INTERVAL '1 hour'");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    json.append("  \"active_balises\": ").append(rs.getInt(1)).append(",\n");
                }
            }
            
            // Count total events
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM balise_events");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    json.append("  \"total_events\": ").append(rs.getInt(1)).append(",\n");
                }
            }
            
            // Count events in last 24 hours
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '24 hours'");
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    json.append("  \"events_24h\": ").append(rs.getInt(1)).append(",\n");
                }
            }
            
            json.append("  \"timestamp\": \"").append(LocalDateTime.now()).append("\"\n");
            json.append("}");
            
            sendJsonResponse(exchange, 200, json.toString());
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String htmlResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(statusCode, htmlResponse.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlResponse.getBytes(StandardCharsets.UTF_8));
        }
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
