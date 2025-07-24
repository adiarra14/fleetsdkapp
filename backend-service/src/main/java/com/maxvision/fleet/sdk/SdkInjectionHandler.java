package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;

/**
 * Enhanced SDK injection handler to ensure LockReportService is properly injected
 * This solves the SDK timing and dependency injection issues
 */
@Component
public class SdkInjectionHandler implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static LockReportService lockReportService;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        System.out.println("=== SDK INJECTION HANDLER INITIALIZED ===");

        // Immediately try to inject the service
        injectLockReportService();

        // Set up periodic injection attempts for SDK timing issues
        setupPeriodicInjection();
    }

    /**
     * Inject LockReportService into SDK components
     */
    public static void injectLockReportService() {
        if (applicationContext == null) {
            System.out.println("WARNING: ApplicationContext not yet available");
            return;
        }

        try {
            // Get the LockReportService bean
            lockReportService = applicationContext.getBean(LockReportService.class);
            System.out.println("SUCCESS: Retrieved LockReportService bean: " + lockReportService.getClass().getName());

            // Register in global registry
            SdkServiceRegistry.registerLockReportService(lockReportService);

            // Try to inject into SDK components using reflection
            injectIntoSdkComponents();

        } catch (Exception e) {
            System.err.println("ERROR: Failed to inject LockReportService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Use reflection to inject service into SDK components
     */
    private static void injectIntoSdkComponents() {
        try {
            System.out.println("=== ATTEMPTING SDK COMPONENT INJECTION ===");

            // Try to find and inject into common SDK handler classes
            String[] possibleHandlerClasses = {
                    "com.maxvision.edge.gateway.lock.netty.handler.a.l",
                    "com.maxvision.edge.gateway.lock.netty.handler.LockMessageHandler",
                    "com.maxvision.edge.gateway.lock.netty.handler.MessageHandler"
            };

            for (String className : possibleHandlerClasses) {
                try {
                    Class<?> handlerClass = Class.forName(className);
                    injectIntoClass(handlerClass);
                } catch (ClassNotFoundException e) {
                    System.out.println("INFO: Handler class not found: " + className);
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR: SDK component injection failed: " + e.getMessage());
        }
    }

    /**
     * Inject service into a specific class using reflection
     */
    private static void injectIntoClass(Class<?> targetClass) {
        try {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class)) {
                    field.setAccessible(true);

                    // Try to set the field on all instances
                    System.out.println("Found LockReportService field: " + field.getName() + " in " + targetClass.getName());

                    // This is a static injection approach - may need instance-specific injection
                    try {
                        field.set(null, lockReportService);
                        System.out.println("SUCCESS: Injected LockReportService into static field: " + field.getName());
                    } catch (Exception e) {
                        System.out.println("INFO: Static injection failed, field may be instance-based: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to inject into class " + targetClass.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Set up periodic injection attempts to handle SDK timing issues
     */
    private void setupPeriodicInjection() {
        Thread injectionThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(2000); // Wait 2 seconds
                    System.out.println("=== PERIODIC INJECTION ATTEMPT " + (i + 1) + " ===");
                    injectLockReportService();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        injectionThread.setDaemon(true);
        injectionThread.start();
    }

    /**
     * Get the current LockReportService instance
     */
    public static LockReportService getLockReportService() {
        return lockReportService;
    }
}
