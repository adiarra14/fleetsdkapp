package com.maxvision.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standalone minimal backend application that excludes all SDK components.
 * This provides a basic REST API without any dependencies on the SDK library.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.maxvision.backend"})
@RestController
public class StandaloneBackendApplication {
    
    @GetMapping("/health")
    public String health() {
        return "Fleet SDK Backend Service is running - " + java.time.LocalDateTime.now();
    }
    
    @GetMapping("/")
    public String home() {
        return "Fleet SDK Backend Service - Ready and Operational";
    }
    
    public static void main(String[] args) {
        System.out.println("=== Starting Fleet SDK Backend Service (Standalone Mode) ===");
        SpringApplication.run(StandaloneBackendApplication.class, args);
        System.out.println("=== Fleet SDK Backend Service Started (Standalone Mode) ===");
    }
}
