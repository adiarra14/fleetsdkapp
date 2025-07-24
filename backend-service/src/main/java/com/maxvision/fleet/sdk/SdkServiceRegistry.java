package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global registry for SDK services to ensure proper injection
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
}
