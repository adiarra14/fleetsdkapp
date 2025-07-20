/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Main Application Class
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This software and associated documentation files are the property
 * of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
 * 
 * Unauthorized copying, modification, distribution, or use of this software
 * is strictly prohibited without prior written consent from Ynnov.
 */

package com.maxvision.cmacgm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

import com.maxvision.cmacgm.config.CmaCgmConfig;

@SpringBootApplication
@EnableConfigurationProperties(CmaCgmConfig.class)
@EnableAsync
public class CmaCgmIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmaCgmIntegrationApplication.class, args);
    }
}
