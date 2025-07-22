package com.maxvision.fleet.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.maxvision.fleet.sdk",
    "com.maxvision.edge.gateway"
}, nameGenerator = SdkNettyApplication.CustomBeanNameGenerator.class)
public class SdkNettyApplication {

    // Configuration class to ensure JdbcTemplate bean is created
    @Configuration
    public static class DatabaseConfiguration {
        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

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
                // Use full class path to avoid conflicts with obfuscated SDK classes
                return className.replace('.', '_').toLowerCase();
            }
            return "unknownBean_" + System.currentTimeMillis();
        }
    }
}
