package com.maxvision.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.maxvision.edge.gateway.lock.netty.handler.GlobalExceptionHandler;
import com.maxvision.edge.gateway.lock.netty.LockChannelInitializer;

/**
 * Configuration class to provide mock beans for SDK dependencies
 * that are excluded from the classpath.
 */
@Configuration
public class MockConfig {

    /**
     * Create mock GlobalExceptionHandler bean
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
    
    /**
     * Create mock LockChannelInitializer bean
     */
    @Bean
    public LockChannelInitializer lockChannelInitializer() {
        return new LockChannelInitializer(globalExceptionHandler());
    }
}
