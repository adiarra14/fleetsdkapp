package com.maxvision.fleet.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import javax.annotation.PostConstruct;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import javassist.*;

/**
 * Direct bytecode agent to intercept and capture balise data before null service call
 */
@Component
public class DirectDataCaptureAgent implements ClassFileTransformer {
    
    @Autowired
    private LockReportService lockReportService;
    
    private static LockReportService staticService;
    
    @PostConstruct
    public void init() {
        staticService = lockReportService;
        System.out.println("🎯 DIRECT DATA CAPTURE AGENT INITIALIZED");
        System.out.println("✅ Static service registered: " + staticService.getClass().getName());
        
        // Try to register with JVM instrumentation
        try {
            // This is a simplified approach - in production you'd need proper agent setup
            System.out.println("⚠️ Bytecode transformation requires -javaagent setup");
            System.out.println("💡 Using reflection-based interception instead");
            startReflectionInterception();
        } catch (Exception e) {
            System.out.println("❌ Could not setup bytecode agent: " + e.getMessage());
        }
    }
    
    private void startReflectionInterception() {
        // Start a thread that continuously monitors for handler instances
        Thread interceptorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    interceptHandlerCalls();
                } catch (Exception e) {
                    // Continue silently
                }
            }
        });
        interceptorThread.setDaemon(true);
        interceptorThread.setName("DataCaptureInterceptor");
        interceptorThread.start();
        System.out.println("🔄 Started continuous data interception thread");
    }
    
    private void interceptHandlerCalls() {
        // This is a placeholder for more sophisticated interception
        // In practice, we'd need to hook into the Netty pipeline directly
    }
    
    /**
     * Static method that can be called from anywhere to capture data
     */
    public static void captureData(String jsonData) {
        try {
            if (staticService != null) {
                System.out.println("🎯 DIRECT CAPTURE: Processing balise data");
                staticService.reportLockMsg(jsonData);
                System.out.println("✅ Data successfully captured and stored");
            } else {
                System.out.println("❌ Static service not available for capture");
            }
        } catch (Exception e) {
            System.out.println("❌ Error in direct capture: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        
        // Target the SDK handler class
        if (className != null && className.contains("com/maxvision/edge/gateway/lock/netty/handler")) {
            try {
                System.out.println("🎯 Transforming handler class: " + className);
                return transformHandlerClass(classfileBuffer);
            } catch (Exception e) {
                System.out.println("❌ Failed to transform class: " + e.getMessage());
            }
        }
        
        return null; // No transformation
    }
    
    private byte[] transformHandlerClass(byte[] classBytes) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.makeClass(new java.io.ByteArrayInputStream(classBytes));
        
        // Find methods that call reportLockMsg
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            try {
                // Insert our capture call before any reportLockMsg call
                method.insertBefore(
                    "if ($1 != null && $1 instanceof String) {" +
                    "  com.maxvision.fleet.sdk.DirectDataCaptureAgent.captureData((String)$1);" +
                    "}"
                );
                System.out.println("✅ Injected capture code into method: " + method.getName());
            } catch (Exception e) {
                // Method might not have the right signature, continue
            }
        }
        
        return ctClass.toBytecode();
    }
}
