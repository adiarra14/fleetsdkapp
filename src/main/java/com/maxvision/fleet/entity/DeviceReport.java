package com.maxvision.fleet.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class DeviceReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lockCode;
    private String commandType;
    private String reportData;
    private LocalDateTime receivedAt;
}
