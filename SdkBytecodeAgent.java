package com.maxvision.fleet.sdk;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Bytecode agent that patches the SDK handler to prevent null service calls
 */
public class SdkBytecodeAgent {
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("=== SDK BYTECODE AGENT LOADED ===");
        
        inst.addTransformer(new SdkClassTransformer());
    }
    
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("=== SDK BYTECODE AGENT ATTACHED ===");
        
        inst.addTransformer(new SdkClassTransformer(), true);
        
        // Try to retransform already loaded classes
        try {
            Class<?>[] loadedClasses = inst.getAllLoadedClasses();
            for (Class<?> clazz : loadedClasses) {
                if (clazz.getName().contains("com.maxvision.edge.gateway.lock.netty.handler")) {
                    System.out.println("Retransforming: " + clazz.getName());
                    inst.retransformClasses(clazz);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to retransform classes: " + e.getMessage());
        }
    }
    
    static class SdkClassTransformer implements ClassFileTransformer {
        
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                              ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
            
            if (className != null && className.contains("com/maxvision/edge/gateway/lock/netty/handler")) {
                System.out.println("=== TRANSFORMING SDK HANDLER: " + className + " ===");
                
                // Here we would use ASM or similar to patch the bytecode
                // For now, just log that we found the target class
                return null; // Return null to keep original bytecode
            }
            
            return null;
        }
    }
}
