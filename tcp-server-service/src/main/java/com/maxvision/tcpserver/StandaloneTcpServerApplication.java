package com.maxvision.tcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standalone minimal TCP server application that excludes all SDK components.
 * This provides a basic HTTP health endpoint without any dependencies on the SDK library.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.maxvision.tcpserver"})
@RestController
public class StandaloneTcpServerApplication {
    
    @GetMapping("/health")
    public String health() {
        return "Fleet SDK TCP Server is running - " + java.time.LocalDateTime.now();
    }
    
    @GetMapping("/")
    public String home() {
        return "Fleet SDK TCP Server - Ready and Operational";
    }
    
    public static void main(String[] args) {
        System.out.println("=== Starting Fleet SDK TCP Server (Standalone Mode) ===");
        SpringApplication.run(StandaloneTcpServerApplication.class, args);
        System.out.println("=== Fleet SDK TCP Server Started (Standalone Mode) ===");
    }
}
