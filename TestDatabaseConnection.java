import java.sql.*;
import java.time.LocalDateTime;

/**
 * Simple database connection test for TCP server container
 * Tests if TCP server can connect to PostgreSQL and perform basic operations
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("=== TCP SERVER DATABASE CONNECTION TEST ===");
        System.out.println("Timestamp: " + LocalDateTime.now());
        
        // Database connection parameters (same as in application.yml)
        String url = "jdbc:postgresql://balise-postgres:5432/balisedb";
        String username = "adminbdb";
        String password = "adminbdb123";
        
        try {
            System.out.println("Attempting to connect to: " + url);
            System.out.println("Username: " + username);
            
            // Test connection
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ SUCCESS: Database connection established!");
            
            // Test basic query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT current_timestamp, current_database(), current_user, version()");
            
            if (rs.next()) {
                System.out.println("✅ Database info:");
                System.out.println("  - Current time: " + rs.getTimestamp(1));
                System.out.println("  - Database: " + rs.getString(2));
                System.out.println("  - User: " + rs.getString(3));
                System.out.println("  - Version: " + rs.getString(4));
            }
            
            // Test table access
            rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name");
            System.out.println("✅ Available tables:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }
            
            // Test insert capability
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO balise_events (balise_id, event_type, event_time, message_raw) VALUES (?, ?, ?, ?)"
            );
            pstmt.setInt(1, 1);
            pstmt.setString(2, "CONNECTION_TEST");
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(4, "Database connection test from TCP server");
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Test insert successful, rows affected: " + rowsAffected);
            
            // Verify the insert
            rs = stmt.executeQuery("SELECT COUNT(*) FROM balise_events WHERE event_type = 'CONNECTION_TEST'");
            if (rs.next()) {
                System.out.println("✅ Test records in database: " + rs.getInt(1));
            }
            
            // Clean up test data
            stmt.executeUpdate("DELETE FROM balise_events WHERE event_type = 'CONNECTION_TEST'");
            System.out.println("✅ Test data cleaned up");
            
            conn.close();
            System.out.println("✅ SUCCESS: All database operations completed successfully!");
            System.out.println("🎯 RESULT: TCP server CAN connect to PostgreSQL and perform all operations");
            
        } catch (SQLException e) {
            System.err.println("❌ ERROR: Database connection failed!");
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("SQL state: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.out.println("🚨 RESULT: TCP server CANNOT connect to PostgreSQL");
        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED ERROR:");
            e.printStackTrace();
        }
        
        System.out.println("=== DATABASE CONNECTION TEST COMPLETED ===");
    }
}
