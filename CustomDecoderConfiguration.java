package com.maxvision.fleet.sdk;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class to register our custom frame size decoder
 * as a primary bean that will override the default one.
 */
@Configuration
public class CustomDecoderConfiguration {
    
    @Bean
    @Primary
    public CustomFrameSizeDecoder customFrameSizeDecoder() {
        System.out.println("=== REGISTERING CUSTOM FRAME SIZE DECODER ===");
        return new CustomFrameSizeDecoder();
    }
}
