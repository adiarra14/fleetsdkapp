package com.maxvision.fleet.sdk;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

/**
 * Intercepts calls to LockReportService.reportLockMsg() to capture JSON
 * even when the service is null
 */
public class JsonCaptureInterceptor {
    
    public static void installGlobalHandler() {
        // Install uncaught exception handler to capture JSON from stack traces
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            if (exception instanceof NullPointerException && 
                exception.getMessage() != null && 
                exception.getMessage().contains("LockReportService.reportLockMsg")) {
                
                System.out.println("=== JSON CAPTURE INTERCEPTOR ===");
                System.out.println("Caught NullPointerException for LockReportService");
                System.out.println("Thread: " + thread.getName());
                
                // Try to extract JSON from method parameters using reflection
                try {
                    StackTraceElement[] stack = exception.getStackTrace();
                    for (StackTraceElement element : stack) {
                        if (element.getClassName().contains("handler.a.l")) {
                            System.out.println("Found SDK handler: " + element.getClassName());
                            System.out.println("Method: " + element.getMethodName());
                            System.out.println("Line: " + element.getLineNumber());
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in JSON capture: " + e.getMessage());
                }
            }
            
            // Print the original exception
            System.err.println("Uncaught exception in thread " + thread.getName());
            exception.printStackTrace();
        });
        
        System.out.println("=== JSON CAPTURE INTERCEPTOR INSTALLED ===");
    }
    
    /**
     * Create a proxy that logs all method calls
     */
    public static Object createLoggingProxy(Object target, Class<?> interfaceClass) {
        return Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new LoggingInvocationHandler(target)
        );
    }
    
    static class LoggingInvocationHandler implements InvocationHandler {
        private final Object target;
        
        public LoggingInvocationHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("=== PROXY CALL INTERCEPTED ===");
            System.out.println("Method: " + method.getName());
            System.out.println("Args count: " + (args != null ? args.length : 0));
            
            if (args != null && args.length > 0 && args[0] instanceof String) {
                String jsonStr = (String) args[0];
                System.out.println("=== CAPTURED JSON ===");
                System.out.println("JSON Length: " + jsonStr.length());
                System.out.println("JSON Content: " + jsonStr);
                System.out.println("=== END JSON ===");
            }
            
            if (target != null) {
                return method.invoke(target, args);
            } else {
                System.out.println("Target is null, method call intercepted successfully");
                return null;
            }
        }
    }
}
