package com.maxvision.fleet.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.maxvision.fleet.sdk",
    "com.maxvision.edge.gateway"
}, nameGenerator = SdkNettyApplication.CustomBeanNameGenerator.class)
public class SdkNettyApplication {

    public static void main(String[] args) {
        System.out.println("=== STARTING REAL MAXVISION SDK NETTY SERVER ===");
        System.out.println("=== FULL SDK INTEGRATION - NO MOCK ===");
        SpringApplication.run(SdkNettyApplication.class, args);
        System.out.println("=== SDK NETTY SERVER STARTED SUCCESSFULLY ===");
    }

    // Custom bean name generator to avoid conflicts with SDK beans
    public static class CustomBeanNameGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            String className = definition.getBeanClassName();
            if (className != null) {
                return className.substring(className.lastIndexOf('.') + 1).toLowerCase();
            }
            return "unknownBean";
        }
    }
}
