package com.maxvision.fleet.sdk;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class WorkingServicePatcher {

    @Autowired
    private ApplicationContext applicationContext;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private LockReportService lockReportService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("=== WORKING SERVICE PATCHER STARTED ===");
        
        // Get our service - handle multiple beans
        try {
            // Try to get the correct implementation by name
            try {
                lockReportService = (LockReportService) applicationContext.getBean("com_maxvision_edge_gateway_sdk_report_lockreportserviceimpl");
                System.out.println("✅ Found correct LockReportService: " + lockReportService.getClass().getName());
            } catch (Exception e1) {
                try {
                    lockReportService = (LockReportService) applicationContext.getBean("lockReportService");
                    System.out.println("✅ Found fallback LockReportService: " + lockReportService.getClass().getName());
                } catch (Exception e2) {
                    // Get any available implementation by trying each bean name individually
                    try {
                        String[] beanNames = applicationContext.getBeanNamesForType(LockReportService.class);
                        System.out.println("Found " + beanNames.length + " LockReportService beans");
                        
                        // Try each bean individually
                        for (String beanName : beanNames) {
                            try {
                                lockReportService = (LockReportService) applicationContext.getBean(beanName);
                                System.out.println("✅ Successfully got bean: " + beanName + " -> " + lockReportService.getClass().getName());
                                break;
                            } catch (Exception beanEx) {
                                System.out.println("⚠️ Failed to get bean: " + beanName + " - " + beanEx.getMessage());
                            }
                        }
                        
                        if (lockReportService == null) {
                            throw new RuntimeException("Could not get any of the " + beanNames.length + " available beans");
                        }
                    } catch (Exception getBeanEx) {
                        throw new RuntimeException("Error getting bean names: " + getBeanEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Could not find any LockReportService: " + e.getMessage());
            return;
        }

        // Start aggressive patching
        scheduler.scheduleAtFixedRate(this::patchAllHandlers, 1, 5, TimeUnit.SECONDS);
        System.out.println("✅ Started aggressive service injection every 5 seconds");
    }

    private void patchAllHandlers() {
        try {
            // Get all threads
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);

            for (Thread thread : threads) {
                if (thread != null && thread.getName().contains("pool")) {
                    patchThreadStackObjects(thread);
                }
            }
        } catch (Exception e) {
            // Silent - don't spam logs
        }
    }

    private void patchThreadStackObjects(Thread thread) {
        try {
            // Get thread's stack trace
            StackTraceElement[] stack = thread.getStackTrace();
            
            for (StackTraceElement element : stack) {
                if (element.getClassName().contains("maxvision.edge.gateway.lock.netty.handler")) {
                    // Found a handler in the stack - try to patch it
                    patchHandlerClass(element.getClassName());
                }
            }
        } catch (Exception e) {
            // Silent
        }
    }

    private void patchHandlerClass(String className) {
        try {
            Class<?> handlerClass = Class.forName(className);
            
            // Get all instances of this class from all threads
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            
            for (Thread thread : threads) {
                patchThreadLocals(thread, handlerClass);
            }
            
        } catch (Exception e) {
            // Silent
        }
    }

    private void patchThreadLocals(Thread thread, Class<?> targetClass) {
        try {
            // Use reflection to access thread locals and stack objects
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocals = threadLocalsField.get(thread);
            
            if (threadLocals != null) {
                // Try to find and patch handler objects
                patchObjectFields(threadLocals, targetClass);
            }
        } catch (Exception e) {
            // Silent
        }
    }

    private void patchObjectFields(Object obj, Class<?> targetClass) {
        if (obj == null) return;
        
        try {
            Class<?> objClass = obj.getClass();
            
            // Check if this is a handler object
            if (targetClass.isAssignableFrom(objClass)) {
                injectServiceIntoHandler(obj);
                return;
            }
            
            // Recursively check all fields
            Field[] fields = objClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                
                if (fieldValue != null) {
                    if (targetClass.isAssignableFrom(fieldValue.getClass())) {
                        injectServiceIntoHandler(fieldValue);
                    } else {
                        patchObjectFields(fieldValue, targetClass);
                    }
                }
            }
        } catch (Exception e) {
            // Silent
        }
    }

    private void injectServiceIntoHandler(Object handler) {
        try {
            Class<?> handlerClass = handler.getClass();
            
            // Look for lockReportService field
            Field[] fields = handlerClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(LockReportService.class) || 
                    field.getName().contains("lockReportService")) {
                    
                    field.setAccessible(true);
                    Object currentValue = field.get(handler);
                    
                    if (currentValue == null) {
                        field.set(handler, lockReportService);
                        System.out.println("🎯 SUCCESSFULLY INJECTED SERVICE INTO: " + handlerClass.getName());
                        return;
                    }
                }
            }
            
            // Also check superclass fields
            Class<?> superClass = handlerClass.getSuperclass();
            while (superClass != null) {
                Field[] superFields = superClass.getDeclaredFields();
                for (Field field : superFields) {
                    if (field.getType().equals(LockReportService.class) || 
                        field.getName().contains("lockReportService")) {
                        
                        field.setAccessible(true);
                        Object currentValue = field.get(handler);
                        
                        if (currentValue == null) {
                            field.set(handler, lockReportService);
                            System.out.println("🎯 SUCCESSFULLY INJECTED SERVICE INTO SUPERCLASS: " + superClass.getName());
                            return;
                        }
                    }
                }
                superClass = superClass.getSuperclass();
            }
            
        } catch (Exception e) {
            // Silent
        }
    }
}
