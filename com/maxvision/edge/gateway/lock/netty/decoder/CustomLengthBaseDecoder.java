package com.maxvision.edge.gateway.lock.netty.decoder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Replacement for the SDK's default CustomLengthBaseDecoder.
 * Increases the max frame size from 4 KB to 128 KB so that
 * ~30 KB balise messages are accepted instead of discarded.
 */
public class CustomLengthBaseDecoder extends LengthFieldBasedFrameDecoder {

    private static final int MAX_FRAME_SIZE = 131_072; // 128 KB

    public CustomLengthBaseDecoder() {
        /*
         * original parameters in SDK constructor:
         *   maxFrameLength = 4096
         *   lengthFieldOffset = 0
         *   lengthFieldLength = 4
         *   lengthAdjustment = 0
         *   initialBytesToStrip = 4
         */
        super(MAX_FRAME_SIZE, 0, 4, 0, 4);
        System.out.println("=== CUSTOM FRAME SIZE DECODER INITIALIZED ===");
        System.out.println("=== MAX FRAME SIZE: " + MAX_FRAME_SIZE + " BYTES ===");
    }
}
