/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Configuration Class
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

package com.maxvision.cmacgm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cmacgm")
public class CmaCgmConfig {
    
    private Auth auth = new Auth();
    private Api api = new Api();
    private Integration integration = new Integration();
    
    // Getters and Setters
    public Auth getAuth() {
        return auth;
    }
    
    public void setAuth(Auth auth) {
        this.auth = auth;
    }
    
    public Api getApi() {
        return api;
    }
    
    public void setApi(Api api) {
        this.api = api;
    }
    
    public Integration getIntegration() {
        return integration;
    }
    
    public void setIntegration(Integration integration) {
        this.integration = integration;
    }
    
    // Nested configuration classes
    public static class Auth {
        private String url = "https://auth-pre.cma-cgm.com/as/token.oauth2";
        private String clientId = "beapp-sinigroup";
        private String clientSecret;
        private String scope = "tracking:write:be";
        
        // Getters and Setters
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getClientId() {
            return clientId;
        }
        
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        
        public String getClientSecret() {
            return clientSecret;
        }
        
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
        
        public String getScope() {
            return scope;
        }
        
        public void setScope(String scope) {
            this.scope = scope;
        }
    }
    
    public static class Api {
        private String baseUrl = "https://apis-uat.cma-cgm.net/technical/generic/eagle/v1";
        private int timeout = 30;
        private int retryAttempts = 3;
        private int retryDelaySeconds = 5;
        
        // Getters and Setters
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
        
        public int getRetryAttempts() {
            return retryAttempts;
        }
        
        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }
        
        public int getRetryDelaySeconds() {
            return retryDelaySeconds;
        }
        
        public void setRetryDelaySeconds(int retryDelaySeconds) {
            this.retryDelaySeconds = retryDelaySeconds;
        }
    }
    
    public static class Integration {
        private boolean enabled = true;
        private String originatorName = "SINIGROUP";
        private String partnerName = "SINIGROUP";
        private String defaultModeOfTransport = "TRUCK";
        private String defaultFuelType = "FUEL";
        private String defaultMileageUnit = "KM";
        private String defaultTemperatureUnit = "CEL";
        private int batchSize = 100;
        private int processingIntervalSeconds = 60;
        
        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getOriginatorName() {
            return originatorName;
        }
        
        public void setOriginatorName(String originatorName) {
            this.originatorName = originatorName;
        }
        
        public String getPartnerName() {
            return partnerName;
        }
        
        public void setPartnerName(String partnerName) {
            this.partnerName = partnerName;
        }
        
        public String getDefaultModeOfTransport() {
            return defaultModeOfTransport;
        }
        
        public void setDefaultModeOfTransport(String defaultModeOfTransport) {
            this.defaultModeOfTransport = defaultModeOfTransport;
        }
        
        public String getDefaultFuelType() {
            return defaultFuelType;
        }
        
        public void setDefaultFuelType(String defaultFuelType) {
            this.defaultFuelType = defaultFuelType;
        }
        
        public String getDefaultMileageUnit() {
            return defaultMileageUnit;
        }
        
        public void setDefaultMileageUnit(String defaultMileageUnit) {
            this.defaultMileageUnit = defaultMileageUnit;
        }
        
        public String getDefaultTemperatureUnit() {
            return defaultTemperatureUnit;
        }
        
        public void setDefaultTemperatureUnit(String defaultTemperatureUnit) {
            this.defaultTemperatureUnit = defaultTemperatureUnit;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public int getProcessingIntervalSeconds() {
            return processingIntervalSeconds;
        }
        
        public void setProcessingIntervalSeconds(int processingIntervalSeconds) {
            this.processingIntervalSeconds = processingIntervalSeconds;
        }
    }
}
