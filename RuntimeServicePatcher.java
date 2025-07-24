package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Runtime patcher that uses reflection to inject LockReportService into SDK handlers
 */
@Component
public class RuntimeServicePatcher {

    @Autowired
    private ApplicationContext applicationContext;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private LockReportService lockReportService;

    @PostConstruct
    public void startPatching() {
        System.out.println("=== RUNTIME SERVICE PATCHER STARTED ===");
        
        try {
            // Get our service
            lockReportService = applicationContext.getBean(LockReportService.class);
            System.out.println("Found LockReportService: " + lockReportService.getClass().getName());
            
            // Start periodic patching attempts
            scheduler.scheduleAtFixedRate(this::patchSdkHandlers, 2, 10, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Failed to start service patcher: " + e.getMessage());
        }
    }

    private void patchSdkHandlers() {
        try {
            System.out.println("=== ATTEMPTING SDK HANDLER PATCHING ===");
            
            // Find all loaded classes that might be SDK handlers
            Thread.getAllStackTraces().keySet().forEach(thread -> {
                try {
                    patchThreadHandlers(thread);
                } catch (Exception e) {
                    // Ignore individual thread errors
                }
            });
            
        } catch (Exception e) {
            System.out.println("Patching attempt failed: " + e.getMessage());
        }
    }

    private void patchThreadHandlers(Thread thread) {
        try {
            // Get the thread's stack trace to find handler instances
            StackTraceElement[] stack = thread.getStackTrace();
            
            for (StackTraceElement element : stack) {
                if (element.getClassName().contains("com.maxvision.edge.gateway.lock.netty.handler")) {
                    System.out.println("Found handler in thread: " + thread.getName() + " -> " + element.getClassName());
                    
                    // Try to patch this handler class
                    patchHandlerClass(element.getClassName());
                }
            }
            
        } catch (Exception e) {
            // Ignore errors for individual threads
        }
    }

    private void patchHandlerClass(String className) {
        try {
            Class<?> handlerClass = Class.forName(className);
            
            // Find the lockReportService field
            Field[] fields = handlerClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class)) {
                    field.setAccessible(true);
                    
                    System.out.println("Found LockReportService field in: " + className);
                    
                    // Try to set static field
                    try {
                        field.set(null, lockReportService);
                        System.out.println("SUCCESS: Patched static field in " + className);
                        return;
                    } catch (Exception e) {
                        System.out.println("Static patch failed, field is instance-based");
                    }
                    
                    // For instance fields, we need to find actual instances
                    // This is more complex but possible with thread inspection
                    System.out.println("Instance patching needed for: " + className);
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("Handler class not found: " + className);
        } catch (Exception e) {
            System.out.println("Patching failed for " + className + ": " + e.getMessage());
        }
    }
}
