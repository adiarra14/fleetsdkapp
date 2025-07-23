package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * SIMPLE DIAGNOSTIC SERVICE - Minimal implementation that will compile in Docker
 * Focuses on logging when reportLockMsg is called and storing data in database
 */
@Service
public class SimpleDiagnosticService implements LockReportService {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    private static int messageCount = 0;
    
    @Override
    public void reportLockMsg(String message) {
        messageCount++;
        
        System.out.println("=== SIMPLE DIAGNOSTIC: LOCKREPORTSERVICE CALLED ===");
        System.out.println("Call #" + messageCount);
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("Message length: " + (message != null ? message.length() : "NULL"));
        System.out.println("JdbcTemplate available: " + (jdbcTemplate != null));
        
        if (message != null && message.length() > 0) {
            System.out.println("Message preview: " + message.substring(0, Math.min(100, message.length())));
        }
        
        if (jdbcTemplate != null) {
            try {
                // Try to store the message in database
                String insertSql = "INSERT INTO balise_events (balise_id, event_type, event_time, message_raw) VALUES (?, ?, ?, ?)";
                int rowsAffected = jdbcTemplate.update(insertSql, 1, "SDK_MESSAGE", LocalDateTime.now(), message);
                
                System.out.println("SUCCESS: Database insert completed, rows affected: " + rowsAffected);
                
                // Verify the insert
                String countSql = "SELECT COUNT(*) FROM balise_events WHERE event_type = 'SDK_MESSAGE'";
                Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
                System.out.println("Total SDK messages in database: " + count);
                
            } catch (Exception e) {
                System.err.println("ERROR: Database operation failed");
                e.printStackTrace();
            }
        } else {
            System.err.println("WARNING: JdbcTemplate is null, cannot store message");
        }
        
        System.out.println("=== SIMPLE DIAGNOSTIC: COMPLETED ===");
        System.out.println("SUCCESS: This proves the SDK is calling our service!");
    }
}
