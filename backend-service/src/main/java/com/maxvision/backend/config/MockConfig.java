package com.maxvision.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.maxvision.edge.gateway.lock.netty.LockChannelInitializer;
import com.maxvision.backend.dummy.GlobalExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuration class to provide mock beans for SDK dependencies
 * that are excluded from the classpath.
 */
@Configuration
public class MockConfig {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;
    
    /**
     * Create mock LockChannelInitializer bean
     */
    @Bean
    public LockChannelInitializer lockChannelInitializer() {
        return new LockChannelInitializer(globalExceptionHandler);
    }
}
