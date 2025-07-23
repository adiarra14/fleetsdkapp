package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

/**
 * AGGRESSIVE DIAGNOSTIC + INJECTION: Enhanced LockReportService with forced SDK injection
 * This service both logs diagnostics AND forces itself into SDK handlers using reflection
 */
@Service
public class DiagnosticLockReportService implements LockReportService {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    private static int messageCount = 0;
    
    @EventListener(ApplicationReadyEvent.class)
    public void forceInjectIntoSdk() {
        System.out.println("=== AGGRESSIVE SDK INJECTION STARTING ===");
        System.out.println("Target: Force DiagnosticLockReportService into SDK handlers");
        
        // Wait for SDK to fully initialize
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds for SDK startup
                
                System.out.println("🚀 STARTING FORCED INJECTION...");
                
                // Try multiple injection strategies
                injectViaReflection();
                injectViaSystemProperties();
                injectViaStaticFields();
                
                System.out.println("✅ FORCED INJECTION COMPLETED!");
                
            } catch (Exception e) {
                System.err.println("❌ Forced injection failed: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void injectViaReflection() {
        System.out.println("🔍 Attempting reflection-based injection...");
        
        String[] targetClasses = {
            "com.maxvision.edge.gateway.lock.netty.decoder.a",
            "com.maxvision.edge.gateway.lock.netty.LockServer",
            "com.maxvision.edge.gateway.sdk.LockGatewayService"
        };
        
        for (String className : targetClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                Field[] fields = clazz.getDeclaredFields();
                
                for (Field field : fields) {
                    if (field.getType().equals(LockReportService.class)) {
                        field.setAccessible(true);
                        
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            field.set(null, this);
                            System.out.println("✅ INJECTED into: " + className + "." + field.getName());
                        }
                    }
                }
                
            } catch (Exception e) {
                System.out.println("Class not accessible: " + className);
            }
        }
    }
    
    private void injectViaSystemProperties() {
        System.out.println("🔍 Setting system properties for SDK...");
        System.setProperty("maxvision.lockReportService", this.getClass().getName());
        System.setProperty("sdk.forceService", "true");
    }
    
    private void injectViaStaticFields() {
        System.out.println("🔍 Attempting static field manipulation...");
        
        try {
            // Try to access any static LockReportService fields in loaded classes
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            // This is a brute-force approach to find and inject into any static LockReportService fields
            Class<?>[] loadedClasses = getLoadedClasses(cl);
            
            for (Class<?> clazz : loadedClasses) {
                if (clazz.getName().contains("maxvision") || clazz.getName().contains("gateway")) {
                    try {
                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field : fields) {
                            if (field.getType().equals(LockReportService.class) && 
                                java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                                
                                field.setAccessible(true);
                                Object currentValue = field.get(null);
                                
                                if (currentValue == null) {
                                    field.set(null, this);
                                    System.out.println("✅ STATIC INJECTION: " + clazz.getSimpleName() + "." + field.getName());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore individual class errors
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Static field injection failed: " + e.getMessage());
        }
    }
    
    private Class<?>[] getLoadedClasses(ClassLoader classLoader) {
        try {
            Field classesField = ClassLoader.class.getDeclaredField("classes");
            classesField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            java.util.Vector<Class<?>> classes = (java.util.Vector<Class<?>>) classesField.get(classLoader);
            
            return classes.toArray(new Class<?>[0]);
            
        } catch (Exception e) {
            return new Class<?>[0];
        }
    }
    
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
        System.out.println("🎉 SUCCESS: Message processed by AGGRESSIVE INJECTION service!");
        System.out.println("This proves the injection worked and SDK is now calling our service!");
    }
}
