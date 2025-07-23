package com.maxvision.edge.gateway.lock.service;

/**
 * Service interface for lock device reporting in Maxvision Edge Protocol Gateway SDK.
 * This interface defines methods for handling device reports.
 */
public interface LockReportService {
    /**
     * Report a lock message received from a device
     * @param jsonStr The JSON message string to report
     */
    void reportLockMsg(String jsonStr);
}
