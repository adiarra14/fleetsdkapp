/**
 * Fleet Monitor - Balise Management System
 * Backend Application Main Class
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This software and associated documentation files are the property
 * of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
 * 
 * Unauthorized copying, modification, distribution, or use of this software
 * is strictly prohibited without prior written consent from Ynnov.
 */

package com.maxvision.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.maxvision.backend", "com.maxvision.backend.controller"})
@EnableAutoConfiguration
@RestController
public class BackendApplication {
    
    @GetMapping("/health")
    public String health() {
        return "Fleet SDK Backend Service is running - " + java.time.LocalDateTime.now();
    }
    
    @GetMapping("/")
    public String home() {
        return "Fleet SDK Backend Service - Ready and Operational";
    }
    
    public static void main(String[] args) {
        System.out.println("=== Starting Fleet SDK Backend Service ===");
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("=== Fleet SDK Backend Service Started ===");
    }
}
