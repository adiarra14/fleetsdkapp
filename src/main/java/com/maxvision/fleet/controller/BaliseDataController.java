package com.maxvision.fleet.controller;

import com.maxvision.fleet.entity.DeviceReport;
import com.maxvision.fleet.service.DeviceReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for balise data access and management
 */
@RestController
@RequestMapping("/api/balise")
@Slf4j
public class BaliseDataController {

    @Autowired
    private DeviceReportService deviceReportService;

    /**
     * Get recent reports for all balises
     */
    @GetMapping("/reports")
    public ResponseEntity<List<DeviceReport>> getRecentReports() {
        log.info("Request received for recent balise reports");
        return ResponseEntity.ok(deviceReportService.getRecentReports(100));
    }

    /**
     * Get reports for a specific balise
     */
    @GetMapping("/reports/{deviceId}")
    public ResponseEntity<List<DeviceReport>> getDeviceReports(@PathVariable String deviceId) {
        log.info("Request received for balise reports for device: {}", deviceId);
        return ResponseEntity.ok(deviceReportService.getReportsByDevice(deviceId));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Balise Data Service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
