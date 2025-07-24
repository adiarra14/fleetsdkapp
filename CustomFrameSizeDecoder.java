package com.maxvision.fleet.sdk;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * Custom decoder that extends LengthFieldBasedFrameDecoder with a much larger frame size limit.
 * This is designed to replace the default decoder that has a 4096-byte limit.
 */
@Component
public class CustomFrameSizeDecoder extends LengthFieldBasedFrameDecoder {
    
    private static final int MAX_FRAME_SIZE = 131072; // Increased to 128KB from 4KB
    
    // Constructor with typical LengthFieldBasedFrameDecoder parameters
    public CustomFrameSizeDecoder() {
        // Use the same parameters as the original decoder but with increased frame size
        super(MAX_FRAME_SIZE, 0, 4, 0, 4);
        System.out.println("=== CUSTOM FRAME SIZE DECODER INITIALIZED ===");
        System.out.println("=== MAX FRAME SIZE: " + MAX_FRAME_SIZE + " BYTES ===");
    }
    
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        try {
            return super.decode(ctx, in);
        } catch (Exception e) {
            System.err.println("DECODER ERROR: " + e.getMessage());
            if (e.getMessage().contains("Adjusted frame length exceeds")) {
                System.err.println("FRAME SIZE TOO SMALL - INCREASE MAX_FRAME_SIZE IN CustomFrameSizeDecoder");
            }
            throw e;
        }
    }
}
