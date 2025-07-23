package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * DIAGNOSTIC VERSION: Enhanced LockReportService with extensive logging
 * to diagnose why data isn't reaching the database
 */
@Service
public class DiagnosticLockReportService implements LockReportService {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    private static int messageCount = 0;
    
    @Override
    public void reportLockMsg(String message) {
        messageCount++;
        
        System.out.println("=== DIAGNOSTIC: LOCKREPORTSERVICE CALLED ===");
        System.out.println("Call #" + messageCount);
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("Thread: " + Thread.currentThread().getName());
        System.out.println("Message length: " + (message != null ? message.length() : "NULL"));
        System.out.println("Message preview: " + (message != null ? message.substring(0, Math.min(100, message.length())) : "NULL"));
        System.out.println("JdbcTemplate available: " + (jdbcTemplate != null));
        
        if (jdbcTemplate != null) {
            try {
                // Test database connectivity
                String testQuery = "SELECT current_timestamp, current_database(), current_user";
                jdbcTemplate.queryForList(testQuery).forEach(row -> {
                    System.out.println("Database test result: " + row);
                });
                
                // Check if tables exist
                String tableCheck = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'";
                jdbcTemplate.queryForList(tableCheck, String.class).forEach(table -> {
                    System.out.println("Table found: " + table);
                });
                
                // Try to insert the message
                String insertSql = "INSERT INTO balise_events (balise_id, event_type, event_time, message_raw) VALUES (?, ?, ?, ?)";
                int rowsAffected = jdbcTemplate.update(insertSql, 1, "DIAGNOSTIC_MESSAGE", LocalDateTime.now(), message);
                
                System.out.println("SUCCESS: Database insert completed, rows affected: " + rowsAffected);
                
                // Verify the insert
                String countSql = "SELECT COUNT(*) FROM balise_events WHERE event_type = 'DIAGNOSTIC_MESSAGE'";
                Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                System.out.println("Total diagnostic messages in database: " + count);
                
            } catch (Exception e) {
                System.err.println("ERROR: Database operation failed");
                e.printStackTrace();
            }
        } else {
            System.err.println("WARNING: JdbcTemplate is null, cannot store message");
        }
        
        System.out.println("=== DIAGNOSTIC: LOCKREPORTSERVICE COMPLETED ===");
    }
}
