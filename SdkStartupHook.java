package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.report.LockReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Startup hook that injects LockReportService into SDK components after Spring is ready
 */
@Component
public class SdkStartupHook implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private LockReportService lockReportService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("=== SDK STARTUP HOOK TRIGGERED ===");
        System.out.println("LockReportService available: " + (lockReportService != null));
        
        if (lockReportService != null) {
            // Start periodic injection attempts
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                try {
                    injectIntoSdkHandler();
                } catch (Exception e) {
                    System.out.println("Injection attempt: " + e.getMessage());
                }
            }, 1, 10, TimeUnit.SECONDS);
        }
    }

    private void injectIntoSdkHandler() throws Exception {
        // Try to find and inject into the SDK handler
        Class<?> handlerClass = Class.forName("com.maxvision.edge.gateway.lock.netty.handler.a.l");
        
        Field[] fields = handlerClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(LockReportService.class)) {
                field.setAccessible(true);
                
                // Try static injection first
                try {
                    field.set(null, lockReportService);
                    System.out.println("SUCCESS: Injected LockReportService into SDK handler");
                    return;
                } catch (Exception e) {
                    // Field is not static, need instance injection
                    System.out.println("Field is instance-based, static injection failed");
                }
            }
        }
    }
}
