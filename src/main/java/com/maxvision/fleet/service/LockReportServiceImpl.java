package com.maxvision.fleet.service;

import com.maxvision.fleet.entity.DeviceReport;
import com.maxvision.fleet.repository.DeviceReportRepository;
import com.maxvision.edge.gateway.LockReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LockReportServiceImpl implements LockReportService {
    private final DeviceReportRepository repository;

    @Override
    public void reportLockMsg(String jsonStr) {
        // TODO: parse jsonStr for real fields if needed
        DeviceReport report = new DeviceReport();
        report.setReportData(jsonStr);
        report.setReceivedAt(LocalDateTime.now());
        repository.save(report);
    }
}
