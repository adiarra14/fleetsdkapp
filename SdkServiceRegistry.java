package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global registry for SDK services to ensure proper injection
 * This solves the SDK dependency injection timing issue
 */
public class SdkServiceRegistry {
    
    private static final AtomicReference<LockReportService> lockReportServiceRef = new AtomicReference<>();
    
    /**
     * Register the LockReportService for global SDK access
     */
    public static void registerLockReportService(LockReportService service) {
        lockReportServiceRef.set(service);
        System.out.println("=== SDK SERVICE REGISTRY ===");
        System.out.println("LockReportService registered: " + (service != null));
        System.out.println("Service class: " + (service != null ? service.getClass().getName() : "NULL"));
        System.out.println("Registry ready for SDK access");
    }
    
    /**
     * Get the registered LockReportService for SDK use
     */
    public static LockReportService getLockReportService() {
        LockReportService service = lockReportServiceRef.get();
        if (service == null) {
            System.err.println("WARNING: LockReportService not yet registered in SDK registry");
        }
        return service;
    }
    
    /**
     * Check if LockReportService is available
     */
    public static boolean isLockReportServiceAvailable() {
        return lockReportServiceRef.get() != null;
    }
    
    /**
     * Create a proxy service that handles null cases gracefully
     */
    public static LockReportService createProxyService() {
        return new LockReportService() {
            @Override
            public void reportLockMsg(String jsonStr) {
                System.out.println("=== SDK PROXY SERVICE CALLED ===");
                System.out.println("Message received: " + (jsonStr != null ? jsonStr.length() + " chars" : "NULL"));
                
                LockReportService actualService = getLockReportService();
                if (actualService != null) {
                    System.out.println("Delegating to actual service: " + actualService.getClass().getName());
                    actualService.reportLockMsg(jsonStr);
                } else {
                    System.out.println("FALLBACK: No actual service available, logging message");
                    System.out.println("JSON: " + jsonStr);
                    System.out.println("SUCCESS: Message logged via proxy service");
                }
            }
        };
    }
}
