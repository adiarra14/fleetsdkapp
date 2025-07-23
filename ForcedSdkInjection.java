package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * FORCED SDK INJECTION SOLUTION
 * Uses reflection to inject LockReportService into SDK handlers after Spring startup
 * This solves the timing issue where SDK creates handlers before Spring beans are ready
 */
@Component
public class ForcedSdkInjection {
    
    @Autowired
    private LockReportService lockReportService;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @EventListener(ApplicationReadyEvent.class)
    public void injectSdkHandlers() {
        System.out.println("=== FORCED SDK INJECTION STARTING ===");
        
        // Start injection attempts after Spring is fully ready
        scheduler.scheduleWithFixedDelay(this::attemptInjection, 2, 5, TimeUnit.SECONDS);
    }
    
    private void attemptInjection() {
        try {
            System.out.println("=== ATTEMPTING SDK HANDLER INJECTION ===");
            System.out.println("LockReportService available: " + (lockReportService != null));
            
            // Find all loaded classes that might contain LockReportService fields
            Class<?>[] loadedClasses = getAllLoadedClasses();
            int injectionsPerformed = 0;
            
            for (Class<?> clazz : loadedClasses) {
                if (clazz.getName().contains("maxvision") || 
                    clazz.getName().contains("gateway") ||
                    clazz.getName().contains("lock")) {
                    
                    injectionsPerformed += injectIntoClass(clazz);
                }
            }
            
            System.out.println("Total injections performed: " + injectionsPerformed);
            
            if (injectionsPerformed > 0) {
                System.out.println("🎉 SDK HANDLER INJECTION SUCCESSFUL!");
                scheduler.shutdown(); // Stop trying once successful
            }
            
        } catch (Exception e) {
            System.err.println("SDK injection attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int injectIntoClass(Class<?> clazz) {
        int injections = 0;
        
        try {
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class)) {
                    field.setAccessible(true);
                    
                    // Try to find instances of this class
                    Object[] instances = findInstancesOfClass(clazz);
                    
                    for (Object instance : instances) {
                        Object currentValue = field.get(instance);
                        
                        if (currentValue == null) {
                            field.set(instance, lockReportService);
                            injections++;
                            
                            System.out.println("✅ INJECTED LockReportService into: " + 
                                clazz.getSimpleName() + "." + field.getName());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            // Ignore reflection errors for classes we can't access
        }
        
        return injections;
    }
    
    private Class<?>[] getAllLoadedClasses() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Field classesField = ClassLoader.class.getDeclaredField("classes");
            classesField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            java.util.Vector<Class<?>> classes = (java.util.Vector<Class<?>>) classesField.get(classLoader);
            
            return classes.toArray(new Class<?>[0]);
            
        } catch (Exception e) {
            System.err.println("Could not get loaded classes: " + e.getMessage());
            return new Class<?>[0];
        }
    }
    
    private Object[] findInstancesOfClass(Class<?> clazz) {
        // This is a simplified approach - in a real implementation,
        // you might need to use more sophisticated techniques to find instances
        try {
            // Try to find static instances or singletons
            Field[] staticFields = clazz.getDeclaredFields();
            java.util.List<Object> instances = new java.util.ArrayList<>();
            
            for (Field field : staticFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    field.getType().equals(clazz)) {
                    
                    field.setAccessible(true);
                    Object instance = field.get(null);
                    if (instance != null) {
                        instances.add(instance);
                    }
                }
            }
            
            return instances.toArray();
            
        } catch (Exception e) {
            return new Object[0];
        }
    }
}
