/**
 * Fleet Monitor - Balise Management System
 * TCP Server Application Main Class
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

package com.maxvision.tcpserver;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Main application class for the Balise TCP Server.
 * Includes Maxvision Protocol Gateway SDK integration for balise communication.
 */
@SpringBootApplication
@ComponentScan({"com.maxvision.tcpserver", "com.maxvision.edge.gateway"})
@RestController
@Slf4j
public class TcpServerApplication {
    
    @GetMapping("/health")
    public String health() {
        return "Fleet SDK TCP Server is running - " + java.time.LocalDateTime.now();
    }
    
    @GetMapping("/")
    public String home() {
        return "Fleet SDK TCP Server - Ready and Operational";
    }
    
    /**
     * Custom BeanNameGenerator to avoid bean name conflicts with SDK components.
     * This ensures each bean's name is set to its fully qualified class name.
     */
    public static class CustomGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            return definition.getBeanClassName();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Starting Fleet SDK TCP Server ===");
        
        // Use SpringApplicationBuilder with custom BeanNameGenerator to avoid SDK bean conflicts
        new SpringApplicationBuilder(TcpServerApplication.class)
            .beanNameGenerator(new CustomGenerator())
            .run(args);
        
        log.info("=== Fleet SDK TCP Server Started ===");
        log.info("SDK Protocol Gateway integration activated");
    }
}
