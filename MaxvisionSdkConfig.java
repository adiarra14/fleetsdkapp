package com.maxvision.fleet.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Configuration class to inject LockReportService into SDK using Maxvision's recommended approach
 */
@Configuration
public class MaxvisionSdkConfig {
    
    @Autowired
    private LockReportService lockReportService;
    
    @PostConstruct
    public void injectServiceIntoSdk() {
        System.out.println("🔧 MAXVISION SDK CONFIG: Injecting service using recommended approach");
        System.out.println("✅ Service to inject: " + lockReportService.getClass().getName());
        
        // Try Option 2: Look for explicit setter method
        tryExplicitSetterMethod();
        
        // Try Option 1: Look for SpringContextHolder
        trySpringContextHolder();
        
        // Try to find and set the service in common SDK classes
        tryDirectHandlerInjection();
    }
    
    private void tryExplicitSetterMethod() {
        try {
            System.out.println("🔍 Trying explicit setter method approach...");
            
            // Look for common SDK handler classes that might have setters
            String[] possibleClasses = {
                "com.maxvision.edge.gateway.lock.netty.handler.LockHandler",
                "com.maxvision.edge.gateway.lock.netty.handler.a",
                "com.maxvision.edge.gateway.lock.netty.LockServer",
                "com.maxvision.edge.gateway.lock.service.LockServiceRegistry"
            };
            
            for (String className : possibleClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    
                    // Look for setter methods
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().toLowerCase().contains("setlockreportservice") ||
                            method.getName().toLowerCase().contains("setservice") ||
                            method.getName().toLowerCase().contains("injectservice")) {
                            
                            method.setAccessible(true);
                            method.invoke(null, lockReportService); // Try static call first
                            System.out.println("✅ SETTER SUCCESS: " + className + "." + method.getName());
                            return;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // Class doesn't exist, continue
                } catch (Exception e) {
                    System.out.println("⚠️ Setter attempt failed for " + className + ": " + e.getMessage());
                }
            }
            
            System.out.println("❌ No explicit setter methods found");
            
        } catch (Exception e) {
            System.out.println("❌ Explicit setter method approach failed: " + e.getMessage());
        }
    }
    
    private void trySpringContextHolder() {
        try {
            System.out.println("🔍 Trying SpringContextHolder approach...");
            
            // Look for SpringContextHolder or similar utilities
            String[] possibleHolders = {
                "com.maxvision.edge.gateway.SpringContextHolder",
                "com.maxvision.edge.gateway.util.SpringContextHolder",
                "com.maxvision.edge.gateway.context.SpringContextHolder"
            };
            
            for (String holderClass : possibleHolders) {
                try {
                    Class<?> clazz = Class.forName(holderClass);
                    
                    // Look for methods to register beans
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().toLowerCase().contains("setbean") ||
                            method.getName().toLowerCase().contains("register")) {
                            
                            method.setAccessible(true);
                            method.invoke(null, LockReportService.class, lockReportService);
                            System.out.println("✅ CONTEXT HOLDER SUCCESS: " + holderClass + "." + method.getName());
                            return;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // Class doesn't exist, continue
                } catch (Exception e) {
                    System.out.println("⚠️ Context holder attempt failed for " + holderClass + ": " + e.getMessage());
                }
            }
            
            System.out.println("❌ No SpringContextHolder found");
            
        } catch (Exception e) {
            System.out.println("❌ SpringContextHolder approach failed: " + e.getMessage());
        }
    }
    
    private void tryDirectHandlerInjection() {
        try {
            System.out.println("🔍 Trying direct handler injection...");
            
            // Try to find handler classes and inject directly into static fields
            String[] handlerClasses = {
                "com.maxvision.edge.gateway.lock.netty.handler.a",
                "com.maxvision.edge.gateway.lock.netty.handler.LockHandler"
            };
            
            for (String className : handlerClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    
                    // Look for static fields that could hold the service
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType().equals(LockReportService.class) &&
                            java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            
                            field.setAccessible(true);
                            field.set(null, lockReportService);
                            System.out.println("✅ DIRECT INJECTION SUCCESS: " + className + "." + field.getName());
                            return;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // Class doesn't exist, continue
                } catch (Exception e) {
                    System.out.println("⚠️ Direct injection attempt failed for " + className + ": " + e.getMessage());
                }
            }
            
            System.out.println("❌ No suitable static fields found for direct injection");
            
        } catch (Exception e) {
            System.out.println("❌ Direct handler injection failed: " + e.getMessage());
        }
    }
}
