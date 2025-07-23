package com.maxvision.edge.gateway.lock.netty.handler.model;

/**
 * MessageHandler interface for Maxvision Edge Protocol Gateway SDK.
 * This interface defines methods for handling messages and exceptions.
 */
public interface MessageHandler {
    /**
     * Handle a message received from a device
     * @param message The message object to handle
     */
    void handleMessage(Object message);
    
    /**
     * Handle an exception that occurred during message processing
     * @param throwable The exception to handle
     */
    void handleException(Throwable throwable);
}
