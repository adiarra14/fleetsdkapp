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
