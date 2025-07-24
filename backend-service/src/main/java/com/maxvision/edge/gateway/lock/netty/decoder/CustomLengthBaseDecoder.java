package com.maxvision.edge.gateway.lock.netty.decoder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Replacement for the SDK's default CustomLengthBaseDecoder, placed in backend-service module.
 * Increases the max frame size to 128 KB so large balise messages are accepted.
 */
public class CustomLengthBaseDecoder extends LengthFieldBasedFrameDecoder {

    private static final int MAX_FRAME_SIZE = 131_072; // 128 KB

    public CustomLengthBaseDecoder() {
        super(MAX_FRAME_SIZE, 0, 4, 0, 4);
        System.out.println("=== CUSTOM FRAME SIZE DECODER INITIALIZED ===");
        System.out.println("=== MAX FRAME SIZE: " + MAX_FRAME_SIZE + " BYTES ===");
    }
}
