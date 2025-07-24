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
 * Runtime service injector that uses reflection to inject LockReportService
 * into the SDK handler after it's created
 */
@Component
public class RuntimeServiceInjector {

    @Autowired
    private ApplicationContext applicationContext;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void startInjection() {
        System.out.println("=== RUNTIME SERVICE INJECTOR STARTED ===");
        
        // Try injection every 5 seconds for 2 minutes
        scheduler.scheduleAtFixedRate(this::attemptInjection, 5, 5, TimeUnit.SECONDS);
        
        // Stop after 2 minutes
        scheduler.schedule(() -> {
            scheduler.shutdown();
            System.out.println("=== RUNTIME INJECTOR STOPPED ===");
        }, 2, TimeUnit.MINUTES);
    }

    private void attemptInjection() {
        try {
            System.out.println("=== ATTEMPTING RUNTIME INJECTION ===");
            
            // Get our LockReportService bean
            LockReportService service = applicationContext.getBean(LockReportService.class);
            System.out.println("Found LockReportService: " + service.getClass().getName());
            
            // Try to inject into the SDK handler class
            injectIntoSdkHandler(service);
            
        } catch (Exception e) {
            System.out.println("Injection attempt failed: " + e.getMessage());
        }
    }

    private void injectIntoSdkHandler(LockReportService service) {
        try {
            // The SDK handler class that needs the service
            Class<?> handlerClass = Class.forName("com.maxvision.edge.gateway.lock.netty.handler.a.l");
            
            // Find all instances of this class
            Field[] fields = handlerClass.getDeclaredFields();
            
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class)) {
                    field.setAccessible(true);
                    System.out.println("Found LockReportService field: " + field.getName());
                    
                    // Try to inject into static field (if exists)
                    try {
                        field.set(null, service);
                        System.out.println("SUCCESS: Injected into static field");
                        return;
                    } catch (Exception e) {
                        System.out.println("Static injection failed, trying instance injection");
                    }
                    
                    // For instance fields, we need to find active instances
                    // This is more complex and may require additional reflection
                    System.out.println("Instance injection not yet implemented");
                }
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("SDK handler class not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Injection failed: " + e.getMessage());
        }
    }
}
