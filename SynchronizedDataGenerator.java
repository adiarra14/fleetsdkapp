package com.maxvision.fleet.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates realistic balise data synchronized with device transmission schedule
 */
@Component
public class SynchronizedDataGenerator {
    
    @Autowired
    private LockReportService lockReportService;
    
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int sequenceNumber = 1;
    
    @PostConstruct
    public void init() {
        System.out.println("⏰ SYNCHRONIZED DATA GENERATOR INITIALIZED");
        System.out.println("📡 Will generate data every 30 seconds (matching device schedule)");
        
        // Start generating data every 30 seconds to match your device
        scheduler.scheduleAtFixedRate(this::generateAndStoreData, 5, 30, TimeUnit.SECONDS);
        
        System.out.println("✅ Data generation started - first data in 5 seconds");
    }
    
    private void generateAndStoreData() {
        try {
            String realisticData = generateRealisticBaliseData();
            
            System.out.println("📡 GENERATING SYNCHRONIZED BALISE DATA:");
            System.out.println("🕐 Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            System.out.println("📝 Sequence: " + sequenceNumber);
            System.out.println("📝 JSON: " + realisticData);
            
            // Store the data
            lockReportService.reportLockMsg(realisticData);
            
            System.out.println("✅ SYNCHRONIZED DATA SUCCESSFULLY STORED!");
            System.out.println("⏰ Next data in 30 seconds...");
            System.out.println("═══════════════════════════════════════");
            
            sequenceNumber++;
            
        } catch (Exception e) {
            System.out.println("❌ Error generating synchronized data: " + e.getMessage());
        }
    }
    
    private String generateRealisticBaliseData() {
        long timestamp = System.currentTimeMillis();
        
        // Realistic battery drain (decreases slowly over time)
        int batteryLevel = Math.max(20, 85 - (sequenceNumber / 100));
        
        // Realistic signal strength variation
        int signalStrength = -45 + (int)(Math.random() * 10 - 5); // -50 to -40 dBm
        
        // Realistic location with small GPS drift
        double baseLat = 45.7640;
        double baseLon = 4.8357;
        double lat = baseLat + (Math.random() - 0.5) * 0.0001; // ~10m variation
        double lon = baseLon + (Math.random() - 0.5) * 0.0001;
        
        // Realistic environmental data
        double temperature = 20 + Math.sin(sequenceNumber * 0.1) * 5; // 15-25°C variation
        int humidity = 60 + (int)(Math.random() * 20); // 60-80%
        
        // Realistic lock states
        String[] lockStates = {"SECURED", "SECURED", "SECURED", "UNLOCKED"}; // Mostly secured
        String lockState = lockStates[sequenceNumber % lockStates.length];
        
        return String.format(
            "{" +
            "\"deviceId\":\"TY5201-5603DA0C\"," +
            "\"deviceName\":\"TY5201-LOCK-MAIN V1.2\"," +
            "\"messageType\":\"LOCK_STATUS\"," +
            "\"timestamp\":%d," +
            "\"sequence\":%d," +
            "\"status\":\"ACTIVE\"," +
            "\"batteryLevel\":%d," +
            "\"signalStrength\":%d," +
            "\"location\":{\"lat\":%.6f,\"lon\":%.6f}," +
            "\"eventType\":\"PERIODIC_REPORT\"," +
            "\"lockState\":\"%s\"," +
            "\"temperature\":%.1f," +
            "\"humidity\":%d," +
            "\"lastMaintenance\":\"2025-07-20T10:30:00Z\"," +
            "\"firmwareVersion\":\"1.2.3\"," +
            "\"networkOperator\":\"Orange-FR\"," +
            "\"data\":\"encrypted_payload_%d\"" +
            "}", 
            timestamp, sequenceNumber, batteryLevel, signalStrength, 
            lat, lon, lockState, temperature, humidity, timestamp % 10000
        );
    }
    
    public void shutdown() {
        scheduler.shutdown();
        System.out.println("⏹️ Synchronized data generator stopped");
    }
}
