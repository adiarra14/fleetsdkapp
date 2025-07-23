package com.maxvision.edge.gateway.lock.netty.handler.model;

/**
 * Base Message interface for Maxvision Edge Protocol Gateway SDK.
 * All message types must implement this interface.
 */
public interface Message {
    /**
     * Get the lock code that identifies the device
     * @return The lock code as a String
     */
    String getLockCode();
    
    /**
     * Get the type of message (login, gps, keepalive, nfcInfo)
     * @return The message type as a String
     */
    String getMessageType();
}
