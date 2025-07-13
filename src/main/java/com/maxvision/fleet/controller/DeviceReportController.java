package com.maxvision.fleet.controller;

import com.maxvision.fleet.entity.DeviceReport;
import com.maxvision.fleet.repository.DeviceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DeviceReportController {
    private final DeviceReportRepository repository;

    @GetMapping
    public List<DeviceReport> getAllReports() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public DeviceReport getReport(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }
}
