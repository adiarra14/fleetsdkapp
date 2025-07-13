package com.maxvision.fleet.repository;

import com.maxvision.fleet.entity.DeviceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceReportRepository extends JpaRepository<DeviceReport, Long> {
}
