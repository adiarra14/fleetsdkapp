package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * FINAL DEFINITIVE SOLUTION: Self-Contained SDK Integration
 * 
 * This class provides a LockReportService implementation that:
 * 1. Does NOT depend on Spring or any framework
 * 2. Connects directly to PostgreSQL using JDBC
 * 3. Can be instantiated and used by the SDK immediately
 * 4. Provides detailed logging for troubleshooting
 */
public class FinalSdkIntegration implements LockReportService {
    
    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    private Connection connection;
    
    public FinalSdkIntegration() {
        System.out.println("=== FINAL SDK INTEGRATION INITIALIZING ===");
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Create database connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            System.out.println("SUCCESS: Database connection established");
            System.out.println("Database URL: " + DB_URL);
            System.out.println("Database User: " + DB_USER);
            
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize database connection");
            e.printStackTrace();
        }
    }
    
    @Override
    public void reportLockMsg(String message) {
        System.out.println("=== FINAL SDK INTEGRATION - MESSAGE RECEIVED ===");
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("Message: " + message);
        System.out.println("Connection available: " + (connection != null));
        
        try {
            if (connection != null && !connection.isClosed()) {
                // Store the message in the database
                storeBaliseMessage(message);
                System.out.println("SUCCESS: Message stored in database");
            } else {
                System.err.println("WARNING: Database connection not available, message logged only");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to process message");
            e.printStackTrace();
        }
        
        System.out.println("=== FINAL SDK INTEGRATION - MESSAGE PROCESSED ===");
    }
    
    private void storeBaliseMessage(String message) throws SQLException {
        String sql = "INSERT INTO balise_events (balise_id, event_type, event_time, message_raw) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, 1); // Default balise_id
            stmt.setString(2, "SDK_MESSAGE");
            stmt.setObject(3, LocalDateTime.now());
            stmt.setString(4, message);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Database insert successful, rows affected: " + rowsAffected);
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    // Static factory method for easy instantiation
    public static LockReportService createInstance() {
        System.out.println("=== CREATING FINAL SDK INTEGRATION INSTANCE ===");
        return new FinalSdkIntegration();
    }
}
