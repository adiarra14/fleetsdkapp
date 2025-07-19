package com.maxvision.tcpserver.config;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Global exception handler for Netty channels used by the Maxvision SDK.
 * This component is required by the SDK's LockChannelInitializer.
 */
@Component
@Slf4j
public class GlobalExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught in Netty channel {}: {}", ctx.channel().id(), cause.getMessage(), cause);
        
        // Log the remote address if available
        if (ctx.channel().remoteAddress() != null) {
            log.error("Remote address: {}", ctx.channel().remoteAddress());
        }
        
        // Close the channel on exception to prevent resource leaks
        if (ctx.channel().isActive()) {
            log.info("Closing channel {} due to exception", ctx.channel().id());
            ctx.close();
        }
        
        // Don't call super.exceptionCaught() to prevent further propagation
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel {} became inactive", ctx.channel().id());
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel {} became active, remote address: {}", 
                ctx.channel().id(), ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
}
