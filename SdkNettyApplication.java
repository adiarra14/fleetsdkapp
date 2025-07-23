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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Value;
import javax.sql.DataSource;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import com.maxvision.fleet.sdk.LockReportServiceImpl;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {
    "com.maxvision.fleet.sdk",
    "com.maxvision.edge.gateway"
}, nameGenerator = SdkNettyApplication.CustomBeanNameGenerator.class)
public class SdkNettyApplication {

    // Configuration class to ensure JdbcTemplate and DataSource beans are created
    @Configuration
    public static class DatabaseConfiguration {
        
        @Value("${SPRING_DATASOURCE_URL:jdbc:postgresql://balise-postgres:5432/balisedb}")
        private String datasourceUrl;
        
        @Value("${SPRING_DATASOURCE_USERNAME:adminbdb}")
        private String datasourceUsername;
        
        @Value("${SPRING_DATASOURCE_PASSWORD:To7Z2UCeWTsriPxbADX8}")
        private String datasourcePassword;
        
        @Bean
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(datasourceUrl);
            dataSource.setUsername(datasourceUsername);
            dataSource.setPassword(datasourcePassword);
            return dataSource;
        }
        
        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
        
        @Bean
        public LockReportService lockReportService(JdbcTemplate jdbcTemplate) {
            LockReportServiceImpl service = new LockReportServiceImpl();
            // Manually inject JdbcTemplate since we're creating the bean manually
            service.setJdbcTemplate(jdbcTemplate);
            return service;
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
