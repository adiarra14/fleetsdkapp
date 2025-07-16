package com.maxvision.fleet.repository;

import com.maxvision.fleet.entity.DeviceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {
    
    /**
     * Find reports for a specific device ordered by received time (newest first)
     */
    List<DeviceReport> findByLockCodeOrderByReceivedAtDesc(String lockCode);
    
    /**
     * Find most recent reports across all devices
     */
    List<DeviceReport> findTop100ByOrderByReceivedAtDesc();
}
