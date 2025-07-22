package com.maxvision.edge.gateway.lock.netty;

import com.maxvision.edge.gateway.lock.netty.handler.model.MessageHandler;

public class LockProtocolServer {
    private final int port;
    private final MessageHandler handler;
    
    public LockProtocolServer(int port, MessageHandler handler) {
        this.port = port;
        this.handler = handler;
    }
    
    public void start() throws Exception {
        System.out.println("Starting LockProtocolServer on port " + port);
    }
}
