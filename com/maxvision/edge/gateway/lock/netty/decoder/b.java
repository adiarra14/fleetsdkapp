package com.maxvision.edge.gateway.lock.netty.decoder;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Replacement for the SDK's obfuscated decoder class (simple name 'b').
 * Increases max frame size from 4096 bytes to 128 KB to allow full
 * balise messages.
 */
public class b extends LengthFieldBasedFrameDecoder {

    private static final int MAX_FRAME_SIZE = 131_072; // 128 KB

    public b() {
        super(MAX_FRAME_SIZE, 0, 4, 0, 4);
        System.out.println("=== CUSTOM FRAME SIZE DECODER (class b) INITIALIZED ===");
        System.out.println("=== MAX FRAME SIZE: " + MAX_FRAME_SIZE + " BYTES ===");
    }
}
