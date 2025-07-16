package com.maxvision.fleet.service;

import com.maxvision.fleet.entity.DeviceReport;
import com.maxvision.fleet.repository.DeviceReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling balise device reports
 */
@Service
@Slf4j
public class DeviceReportService {

    @Autowired
    private DeviceReportRepository reportRepository;

    /**
     * Save a new device report
     * @param report the report to save
     * @return the saved report
     */
    public DeviceReport saveReport(DeviceReport report) {
        if (report.getReceivedAt() == null) {
            report.setReceivedAt(LocalDateTime.now());
        }
        log.info("Saving device report: {}", report);
        return reportRepository.save(report);
    }

    /**
     * Get all reports for a specific device
     * @param lockCode the device ID/lock code
     * @return list of reports for the device
     */
    public List<DeviceReport> getReportsByDevice(String lockCode) {
        return reportRepository.findByLockCodeOrderByReceivedAtDesc(lockCode);
    }

    /**
     * Get recent reports for all devices
     * @param limit max number of reports to return
     * @return list of recent reports
     */
    public List<DeviceReport> getRecentReports(int limit) {
        return reportRepository.findTop100ByOrderByReceivedAtDesc();
    }
}
