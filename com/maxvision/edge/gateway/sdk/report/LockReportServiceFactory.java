package com.maxvision.edge.gateway.sdk.report;

/**
 * Factory for creating LockReportService instances within the SDK
 * This allows the SDK to create our service internally
 */
public class LockReportServiceFactory {
    
    private static volatile LockReportService instance;
    
    /**
     * Get or create the LockReportService instance
     * This method can be called by the SDK internally
     */
    public static LockReportService getInstance() {
        if (instance == null) {
            synchronized (LockReportServiceFactory.class) {
                if (instance == null) {
                    System.out.println("🏭 FACTORY: Creating internal LockReportService");
                    instance = new InternalLockReportService();
                    System.out.println("✅ FACTORY: Internal service created successfully");
                }
            }
        }
        return instance;
    }
    
    /**
     * Create a new instance (for testing or special cases)
     */
    public static LockReportService createNew() {
        System.out.println("🏭 FACTORY: Creating new LockReportService instance");
        return new InternalLockReportService();
    }
    
    /**
     * Reset the singleton (for testing)
     */
    public static void reset() {
        instance = null;
        System.out.println("🔄 FACTORY: Service instance reset");
    }
}
