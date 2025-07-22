package com.maxvision.edge.gateway.lock.netty.handler.model;

public interface MessageHandler {
    void handleMessage(Object message);
    void handleException(Throwable throwable);
}
