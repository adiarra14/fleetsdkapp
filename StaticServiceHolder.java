package com.maxvision.edge.gateway.sdk.report;

/**
 * Static holder for LockReportService to bypass Spring injection timing issues
 */
public class StaticServiceHolder {
    
    private static volatile LockReportService instance;
    
    public static void setInstance(LockReportService service) {
        instance = service;
        System.out.println("🎯 STATIC SERVICE HOLDER: Service registered - " + service.getClass().getName());
    }
    
    public static LockReportService getInstance() {
        return instance;
    }
    
    public static boolean isAvailable() {
        return instance != null;
    }
}
