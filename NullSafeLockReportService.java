package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;

/**
 * Simple null-safe wrapper for LockReportService that handles the SDK timing issue
 * by providing a safe fallback when the service is not yet available.
 */
public class NullSafeLockReportService implements LockReportService {
    
    @Override
    public void reportLockMsg(String jsonStr) {
        System.out.println("=== NULL-SAFE SDK MESSAGE RECEIVED ===");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("JSON Length: " + (jsonStr != null ? jsonStr.length() : 0));
        System.out.println("JSON: " + jsonStr);
        
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            System.err.println("ERROR: Empty or null JSON message received");
            return;
        }
        
        // For now, just log the message - this prevents the NullPointerException
        // and allows us to see that the SDK is working and receiving data
        System.out.println("SUCCESS: NULL-SAFE SDK: Message received and logged");
        System.out.println("Device data contains: " + (jsonStr.contains("TY5201-LOCK") ? "TY5201-LOCK device" : "Unknown device"));
        
        // TODO: Add database storage once the timing issue is fully resolved
        // For now, this prevents the crash and proves the SDK integration works
    }
}
