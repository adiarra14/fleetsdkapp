package com.maxvision.fleet.sdk;

/**
 * Simple static logger to capture JSON data
 */
public class JsonLogger {
    
    public static void logJson(String jsonStr) {
        System.out.println("=== CAPTURED JSON DATA ===");
        System.out.println("Timestamp: " + java.time.Instant.now());
        System.out.println("JSON Length: " + (jsonStr != null ? jsonStr.length() : 0));
        System.out.println("JSON Content: " + jsonStr);
        System.out.println("=== END JSON DATA ===");
    }
    
    public static void logError(String message, Throwable error) {
        System.err.println("=== JSON CAPTURE ERROR ===");
        System.err.println("Message: " + message);
        if (error != null) {
            System.err.println("Error: " + error.getMessage());
            error.printStackTrace();
        }
        System.err.println("=== END ERROR ===");
    }
}
