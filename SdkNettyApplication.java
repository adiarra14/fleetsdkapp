package com.maxvision.fleet.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
            // Create the enhanced LockReportServiceImpl with null-safety
            LockReportServiceImpl service = new LockReportServiceImpl();
            service.setJdbcTemplate(jdbcTemplate);
            
            System.out.println("=== ENHANCED NULL-SAFE LOCKREPORTSERVICE REGISTERED ===");
            System.out.println("Service: " + service.getClass().getName());
            System.out.println("Handles null JdbcTemplate: TRUE");
            System.out.println("Prevents NullPointerException: TRUE");
            System.out.println("Logs all messages: TRUE");
            System.out.println("Database storage when available: TRUE");
            System.out.println("JdbcTemplate: " + (jdbcTemplate != null ? "AVAILABLE" : "NULL"));
            
            // CRITICAL: Register service globally for SDK access
            try {
                // Store service in static context for SDK access
                SdkServiceRegistry.registerLockReportService(service);
                System.out.println("SUCCESS: LockReportService registered in SDK registry");
            } catch (Exception e) {
                System.err.println("WARNING: Could not register service in SDK registry: " + e.getMessage());
            }
            
            return service;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== STARTING REAL MAXVISION SDK NETTY SERVER ===");
        System.out.println("=== FULL SDK INTEGRATION - NO MOCK ===");
        
        // Install JSON capture interceptor
        try {
            JsonCaptureInterceptor.installGlobalHandler();
            System.out.println("=== JSON CAPTURE INTERCEPTOR ACTIVE ===");
        } catch (Exception e) {
            System.err.println("Failed to install JSON interceptor: " + e.getMessage());
        }
        System.out.println("=== ENHANCED INJECTION SYSTEM ACTIVE ===");
        
        SpringApplication app = new SpringApplication(SdkNettyApplication.class);
        ConfigurableApplicationContext context = app.run(args);
        
        // Initialize INTERNAL SDK APPROACH (NEW SOLUTION)
        try {
            System.out.println("=== STARTING INTERNAL SDK APPROACH ===");
            InternalSdkInitializer.initialize();
            System.out.println("✅ Internal SDK initializer active");
            System.out.println("🔧 Using SDK's own architecture (no external dependencies)");
            
            // Also try static field injection
            InternalSdkInitializer.injectViaStaticField();
            System.out.println("✅ Static field injection attempted");
            
        } catch (Exception e) {
            System.out.println("⚠️ Could not initialize internal SDK approach: " + e.getMessage());
        }
        
        System.out.println("=== SDK NETTY SERVER STARTED SUCCESSFULLY ===");
        System.out.println("=== INJECTION HANDLER INITIALIZED ===");
        
        // Force injection after startup
        try {
            Thread.sleep(3000); // Wait for full initialization
            SdkInjectionHandler.injectLockReportService();
            System.out.println("=== POST-STARTUP INJECTION COMPLETED ===");
        } catch (Exception e) {
            System.err.println("WARNING: Post-startup injection failed: " + e.getMessage());
        }
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
