/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Token Management Service
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

package com.maxvision.cmacgm.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class TokenManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String currentToken;
    private Instant tokenExpiresAt;
    
    /**
     * Store a new access token with its expiration time
     */
    public void storeToken(String token, Instant expiresAt) {
        lock.writeLock().lock();
        try {
            this.currentToken = token;
            this.tokenExpiresAt = expiresAt;
            logger.debug("Token stored, expires at: {}", expiresAt);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get the current access token
     */
    public String getCurrentToken() {
        lock.readLock().lock();
        try {
            return currentToken;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if the current token is valid (not null and not expired)
     */
    public boolean isTokenValid() {
        lock.readLock().lock();
        try {
            if (currentToken == null || tokenExpiresAt == null) {
                return false;
            }
            
            boolean isValid = Instant.now().isBefore(tokenExpiresAt);
            if (!isValid) {
                logger.debug("Token has expired at: {}", tokenExpiresAt);
            }
            return isValid;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Clear the stored token (useful for logout or error scenarios)
     */
    public void clearToken() {
        lock.writeLock().lock();
        try {
            this.currentToken = null;
            this.tokenExpiresAt = null;
            logger.debug("Token cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get the token expiration time
     */
    public Instant getTokenExpiresAt() {
        lock.readLock().lock();
        try {
            return tokenExpiresAt;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get remaining token validity time in seconds
     */
    public long getRemainingValiditySeconds() {
        lock.readLock().lock();
        try {
            if (tokenExpiresAt == null) {
                return 0;
            }
            
            long remaining = tokenExpiresAt.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(0, remaining);
        } finally {
            lock.readLock().unlock();
        }
    }
}
