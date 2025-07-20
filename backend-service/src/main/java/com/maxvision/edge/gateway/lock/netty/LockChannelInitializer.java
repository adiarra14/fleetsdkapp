package com.maxvision.edge.gateway.lock.netty;

import org.springframework.stereotype.Component;
import com.maxvision.edge.gateway.lock.netty.handler.GlobalExceptionHandler;

/**
 * Mock implementation of LockChannelInitializer to satisfy Spring wiring.
 * This is a placeholder implementation since we don't have access to the real implementation.
 */
@Component
public class LockChannelInitializer {
    
    private final GlobalExceptionHandler exceptionHandler;
    
    /**
     * Constructor with simplified parameters for mock implementation.
     * Original constructor had parameter 3 requiring GlobalExceptionHandler.
     */
    public LockChannelInitializer(GlobalExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
