package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * SIMPLE SDK INJECTION SOLUTION
 * Directly injects our LockReportService into the SDK's static handler after Spring startup
 * This is much simpler than reflection scanning and targets the specific issue
 */
@Component
public class SimpleSdkInjection {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private LockReportService lockReportService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void injectSdkHandler() {
        System.out.println("=== SIMPLE SDK INJECTION STARTING ===");
        System.out.println("LockReportService available: " + (lockReportService != null));
        
        try {
            // Wait a bit for SDK to fully initialize
            Thread.sleep(3000);
            
            // Try to find and inject into the SDK's handler classes
            injectIntoSdkClasses();
            
            System.out.println("🎉 SDK INJECTION COMPLETED!");
            
        } catch (Exception e) {
            System.err.println("❌ SDK injection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void injectIntoSdkClasses() {
        String[] targetClasses = {
            "com.maxvision.edge.gateway.lock.netty.decoder.a",
            "com.maxvision.edge.gateway.lock.netty.LockServer",
            "com.maxvision.edge.gateway.lock.netty.handler.LockHandler",
            "com.maxvision.edge.gateway.lock.netty.handler.MessageHandler",
            "com.maxvision.edge.gateway.sdk.LockGatewayService"
        };
        
        for (String className : targetClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                injectIntoClass(clazz);
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found (expected): " + className);
            } catch (Exception e) {
                System.err.println("Error injecting into " + className + ": " + e.getMessage());
            }
        }
    }
    
    private void injectIntoClass(Class<?> clazz) {
        System.out.println("Checking class: " + clazz.getSimpleName());
        
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.getType().equals(LockReportService.class)) {
                try {
                    field.setAccessible(true);
                    
                    // For static fields
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        Object currentValue = field.get(null);
                        if (currentValue == null) {
                            field.set(null, lockReportService);
                            System.out.println("✅ INJECTED into static field: " + 
                                clazz.getSimpleName() + "." + field.getName());
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Failed to inject into field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
