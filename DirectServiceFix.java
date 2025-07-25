package com.maxvision.fleet.sdk;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Qualifier;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DirectServiceFix {

    @Autowired
    private ApplicationContext applicationContext;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @EventListener(ApplicationReadyEvent.class)
    public void fixServiceInjection() {
        System.out.println("=== DIRECT SERVICE FIX STARTED ===");
        
        // Get the correct service by trying each bean name
        LockReportService service = null;
        
        try {
            // Try the specific bean name we want
            service = (LockReportService) applicationContext.getBean("com_maxvision_edge_gateway_sdk_report_lockreportserviceimpl");
            System.out.println("✅ Got correct service: " + service.getClass().getName());
        } catch (Exception e1) {
            try {
                service = (LockReportService) applicationContext.getBean("lockReportService");
                System.out.println("✅ Got fallback service: " + service.getClass().getName());
            } catch (Exception e2) {
                try {
                    service = (LockReportService) applicationContext.getBean("com_maxvision_fleet_sdk_lockreportserviceimpl");
                    System.out.println("✅ Got any service: " + service.getClass().getName());
                } catch (Exception e3) {
                    System.err.println("❌ Could not get any LockReportService");
                    return;
                }
            }
        }

        if (service != null) {
            final LockReportService finalService = service;
            
            // Start aggressive injection every 2 seconds
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    injectServiceDirectly(finalService);
                } catch (Exception e) {
                    // Silent - keep trying
                }
            }, 1, 2, TimeUnit.SECONDS);
            
            System.out.println("✅ Started aggressive service injection every 2 seconds");
        }
    }

    private void injectServiceDirectly(LockReportService service) {
        try {
            // Try to find and patch the SDK handler class directly
            Class<?> handlerClass = Class.forName("com.maxvision.edge.gateway.lock.netty.handler.a");
            
            // Get all declared fields
            Field[] fields = handlerClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().getName().contains("LockReportService")) {
                    field.setAccessible(true);
                    
                    // Try to set it as a static field first
                    try {
                        field.set(null, service);
                        System.out.println("🎯 INJECTED into static field: " + field.getName());
                    } catch (Exception e) {
                        // If not static, try to find instances
                        injectIntoInstances(handlerClass, field, service);
                    }
                }
            }
            
            // Also try superclass
            Class<?> superClass = handlerClass.getSuperclass();
            if (superClass != null) {
                Field[] superFields = superClass.getDeclaredFields();
                for (Field field : superFields) {
                    if (field.getType().getName().contains("LockReportService")) {
                        field.setAccessible(true);
                        try {
                            field.set(null, service);
                            System.out.println("🎯 INJECTED into superclass static field: " + field.getName());
                        } catch (Exception e) {
                            injectIntoInstances(superClass, field, service);
                        }
                    }
                }
            }
            
        } catch (ClassNotFoundException e) {
            // Try alternative class names
            String[] alternatives = {
                "com.maxvision.edge.gateway.lock.netty.handler.AbstractBaseHandler",
                "com.maxvision.edge.gateway.lock.netty.handler.LockHandler"
            };
            
            for (String className : alternatives) {
                try {
                    Class<?> altClass = Class.forName(className);
                    injectIntoClass(altClass, service);
                } catch (Exception ignored) {
                    // Continue trying
                }
            }
        } catch (Exception e) {
            // Silent - keep trying
        }
    }
    
    private void injectIntoClass(Class<?> clazz, LockReportService service) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().getName().contains("LockReportService")) {
                field.setAccessible(true);
                try {
                    field.set(null, service);
                    System.out.println("🎯 INJECTED into class " + clazz.getSimpleName() + " field: " + field.getName());
                } catch (Exception e) {
                    // Try instance injection
                    injectIntoInstances(clazz, field, service);
                }
            }
        }
    }

    private void injectIntoInstances(Class<?> handlerClass, Field field, LockReportService service) {
        // This is more complex - try to find actual instances
        // For now, just log that we tried
        System.out.println("⚠️ Attempted instance injection for field: " + field.getName());
    }
}
