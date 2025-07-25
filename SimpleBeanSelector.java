package com.maxvision.fleet.sdk;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.lang.reflect.Field;

@Component
public class SimpleBeanSelector {

    @Autowired
    private ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void selectAndInjectService() {
        System.out.println("=== SIMPLE BEAN SELECTOR STARTED ===");
        
        try {
            // Get all LockReportService beans
            String[] beanNames = applicationContext.getBeanNamesForType(LockReportService.class);
            System.out.println("Found " + beanNames.length + " LockReportService beans:");
            
            LockReportService selectedService = null;
            
            for (String beanName : beanNames) {
                LockReportService service = (LockReportService) applicationContext.getBean(beanName);
                System.out.println("- " + beanName + ": " + service.getClass().getName());
                
                // Prefer the one in the correct package
                if (service.getClass().getName().contains("com.maxvision.edge.gateway.sdk.report")) {
                    selectedService = service;
                    System.out.println("✅ SELECTED: " + beanName);
                    break;
                }
            }
            
            // If not found, use any available
            if (selectedService == null && beanNames.length > 0) {
                selectedService = (LockReportService) applicationContext.getBean(beanNames[0]);
                System.out.println("✅ FALLBACK SELECTED: " + beanNames[0]);
            }
            
            if (selectedService != null) {
                // Now inject it into SDK handlers
                injectIntoSDKHandlers(selectedService);
            } else {
                System.err.println("❌ No LockReportService found!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in bean selection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void injectIntoSDKHandlers(LockReportService service) {
        System.out.println("🎯 Starting injection into SDK handlers...");
        
        // Start a background thread to continuously try injection
        Thread injectionThread = new Thread(() -> {
            for (int i = 0; i < 60; i++) { // Try for 5 minutes
                try {
                    boolean injected = tryInjectService(service);
                    if (injected) {
                        System.out.println("🎉 SUCCESS: Service injected into SDK handler!");
                        break;
                    }
                    Thread.sleep(5000); // Wait 5 seconds between attempts
                } catch (Exception e) {
                    // Silent - continue trying
                }
            }
        });
        
        injectionThread.setDaemon(true);
        injectionThread.start();
        System.out.println("✅ Background injection thread started");
    }
    
    private boolean tryInjectService(LockReportService service) {
        try {
            // Get all threads
            Thread[] threads = new Thread[Thread.activeCount() * 2];
            int count = Thread.enumerate(threads);
            
            for (int i = 0; i < count; i++) {
                Thread thread = threads[i];
                if (thread != null && thread.getName().contains("pool")) {
                    // Try to find handler objects in this thread
                    if (injectIntoThread(thread, service)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean injectIntoThread(Thread thread, LockReportService service) {
        try {
            // Get the thread's context class loader
            ClassLoader classLoader = thread.getContextClassLoader();
            if (classLoader == null) return false;
            
            // Try to load handler classes
            try {
                Class<?> handlerClass = classLoader.loadClass("com.maxvision.edge.gateway.lock.netty.handler.a");
                return injectIntoClass(handlerClass, service);
            } catch (ClassNotFoundException e) {
                // Try other possible handler class names
                String[] possibleClasses = {
                    "com.maxvision.edge.gateway.lock.netty.handler.AbstractBaseHandler",
                    "com.maxvision.edge.gateway.lock.netty.handler.LockHandler"
                };
                
                for (String className : possibleClasses) {
                    try {
                        Class<?> handlerClass = classLoader.loadClass(className);
                        if (injectIntoClass(handlerClass, service)) {
                            return true;
                        }
                    } catch (ClassNotFoundException ignored) {
                        // Continue trying
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean injectIntoClass(Class<?> handlerClass, LockReportService service) {
        try {
            Field[] fields = handlerClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class) || 
                    field.getName().contains("lockReportService")) {
                    
                    field.setAccessible(true);
                    // Set the field to our service (static injection)
                    field.set(null, service);
                    System.out.println("🎯 Injected service into static field: " + field.getName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
