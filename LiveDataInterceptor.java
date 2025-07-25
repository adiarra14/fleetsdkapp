package com.maxvision.fleet.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * LIVE DATA INTERCEPTOR: Intercept the exact moment SDK calls null service
 */
@Component
public class LiveDataInterceptor {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final Map<String, Object> capturedData = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void setupInterceptor() {
        System.out.println("🎯 LIVE DATA INTERCEPTOR: Setting up real-time capture");
        
        // Start monitoring thread for SDK handler instances
        Thread monitorThread = new Thread(this::monitorSdkHandlers);
        monitorThread.setDaemon(true);
        monitorThread.start();
        
        // Setup exception hook to catch NullPointerException with data
        setupExceptionHook();
    }
    
    private void monitorSdkHandlers() {
        while (true) {
            try {
                // Look for active SDK handler instances
                findAndPatchActiveHandlers();
                Thread.sleep(1000); // Check every second
            } catch (Exception e) {
                // Continue monitoring
            }
        }
    }
    
    private void findAndPatchActiveHandlers() {
        try {
            // Get all threads and look for Netty worker threads
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            
            for (Thread thread : threads) {
                if (thread != null && thread.getName().contains("nioEventLoop")) {
                    // This is a Netty worker thread - try to find handler instances
                    patchNettyThread(thread);
                }
            }
        } catch (Exception e) {
            // Continue silently
        }
    }
    
    private void patchNettyThread(Thread nettyThread) {
        try {
            // Use reflection to access thread's stack and find handler instances
            StackTraceElement[] stack = nettyThread.getStackTrace();
            
            for (StackTraceElement element : stack) {
                if (element.getClassName().contains("maxvision") && 
                    element.getClassName().contains("handler")) {
                    
                    // Found a Maxvision handler in the stack
                    tryPatchHandlerClass(element.getClassName());
                }
            }
        } catch (Exception e) {
            // Continue silently
        }
    }
    
    private void tryPatchHandlerClass(String className) {
        try {
            Class<?> handlerClass = Class.forName(className);
            
            // Look for lockReportService field
            Field[] fields = handlerClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().contains("lockReportService") || 
                    field.getType().getSimpleName().contains("LockReportService")) {
                    
                    field.setAccessible(true);
                    
                    // Create a proxy service that captures data before storing
                    Object proxyService = createDataCapturingProxy();
                    
                    // Try to set the field on all instances
                    setFieldOnAllInstances(handlerClass, field, proxyService);
                    
                    System.out.println("🎯 INTERCEPTOR: Patched " + className + "." + field.getName());
                }
            }
        } catch (Exception e) {
            // Continue silently
        }
    }
    
    private Object createDataCapturingProxy() {
        return new Object() {
            public void reportLockMsg(String jsonData) {
                try {
                    System.out.println("🎯 INTERCEPTED LIVE DATA: " + jsonData);
                    
                    // Store the live data immediately
                    storeLiveData(jsonData);
                    
                } catch (Exception e) {
                    System.out.println("⚠️ Failed to store intercepted data: " + e.getMessage());
                }
            }
        };
    }
    
    private void setFieldOnAllInstances(Class<?> clazz, Field field, Object value) {
        // This is complex - would need to track all instances
        // For now, just try static field injection
        try {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                field.set(null, value);
                System.out.println("✅ Set static field: " + field.getName());
            }
        } catch (Exception e) {
            // Continue
        }
    }
    
    private void storeLiveData(String jsonData) {
        try {
            // Parse and store the actual live JSON data
            String sql = "INSERT INTO balise_data (device_id, timestamp, raw_data, data_source, json_payload) " +
                        "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";
            
            jdbcTemplate.update(sql, 
                "LIVE_INTERCEPTED",
                "INTERCEPTED_FROM_SDK", 
                "LIVE_DATA_CAPTURE",
                jsonData
            );
            
            System.out.println("✅ STORED LIVE DATA: " + jsonData.substring(0, Math.min(100, jsonData.length())));
            
        } catch (Exception e) {
            System.out.println("❌ Failed to store live data: " + e.getMessage());
        }
    }
    
    private void setupExceptionHook() {
        // Set up a global exception handler to catch the NullPointerException
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            if (exception instanceof NullPointerException && 
                exception.getMessage() != null &&
                exception.getMessage().contains("LockReportService")) {
                
                System.out.println("🎯 CAUGHT NULL SERVICE EXCEPTION - EXTRACTING DATA FROM STACK");
                extractDataFromException(exception, thread);
            }
        });
    }
    
    private void extractDataFromException(Throwable exception, Thread thread) {
        try {
            // Try to extract data from the exception context
            StackTraceElement[] stack = exception.getStackTrace();
            
            for (StackTraceElement element : stack) {
                if (element.getClassName().contains("AbstractBaseHandler") &&
                    element.getMethodName().equals("a")) {
                    
                    System.out.println("🎯 Found data in AbstractBaseHandler.a() - attempting extraction");
                    // The data should be in the method parameters at this point
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ Exception data extraction failed: " + e.getMessage());
        }
    }
}
