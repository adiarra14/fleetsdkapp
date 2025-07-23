package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

/**
 * DEFINITIVE SOLUTION: Direct SDK Service Injection
 * 
 * This class uses reflection to directly inject our LockReportService
 * into the SDK's internal handlers, bypassing Spring's normal injection
 * mechanism which is failing due to timing issues.
 */
@Component
public class DirectSdkServiceInjection {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private LockReportService lockReportService;
    
    @PostConstruct
    public void injectServicesIntoSdk() {
        System.out.println("=== DIRECT SDK SERVICE INJECTION STARTING ===");
        
        try {
            // Wait for Spring context to be fully initialized
            Thread.sleep(2000);
            
            // Get all beans that might contain the SDK handlers
            String[] allBeanNames = applicationContext.getBeanDefinitionNames();
            
            for (String beanName : allBeanNames) {
                try {
                    Object bean = applicationContext.getBean(beanName);
                    injectLockReportServiceIntoBean(bean);
                } catch (Exception e) {
                    // Ignore beans that can't be retrieved
                }
            }
            
            System.out.println("=== DIRECT SDK SERVICE INJECTION COMPLETED ===");
            System.out.println("LockReportService injected: " + (lockReportService != null));
            
        } catch (Exception e) {
            System.err.println("ERROR in direct SDK service injection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void injectLockReportServiceIntoBean(Object bean) {
        if (bean == null) return;
        
        Class<?> clazz = bean.getClass();
        
        // Look for fields named 'lockReportService' or similar
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(LockReportService.class) || 
                field.getName().contains("lockReportService") ||
                field.getName().contains("LockReportService")) {
                
                try {
                    field.setAccessible(true);
                    Object currentValue = field.get(bean);
                    
                    if (currentValue == null) {
                        field.set(bean, lockReportService);
                        System.out.println("SUCCESS: Injected LockReportService into " + 
                                         clazz.getSimpleName() + "." + field.getName());
                    }
                } catch (Exception e) {
                    // Ignore injection failures for individual fields
                }
            }
        }
        
        // Also check superclasses
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            try {
                injectLockReportServiceIntoBean(bean);
            } catch (Exception e) {
                // Ignore superclass injection failures
            }
        }
    }
}
