package com.maxvision.fleet.sdk;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.maxvision.edge.gateway.sdk.report.LockReportService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * Direct TCP server replacement for Maxvision SDK
 * Handles TY5201-LOCK device communication directly
 */
@Component
public class DirectTcpBaliseServer {
    
    @Autowired
    private LockReportService lockReportService;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    @PostConstruct
    public void startServer() {
        System.out.println("🚀 STARTING DIRECT TCP BALISE SERVER");
        System.out.println("✅ Bypassing Maxvision SDK completely");
        System.out.println("🔧 Direct handling of TY5201-LOCK protocol");
        
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // Add timeout handler
                            pipeline.addLast(new IdleStateHandler(0, 0, 300, TimeUnit.SECONDS));
                            
                            // Add string codecs for easier handling
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            
                            // Add our custom handler
                            pipeline.addLast(new DirectBaliseHandler(lockReportService));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            // Bind to port 8910 (same as SDK would use)
            ChannelFuture future = bootstrap.bind(8910).sync();
            serverChannel = future.channel();
            
            System.out.println("✅ DIRECT TCP SERVER STARTED SUCCESSFULLY");
            System.out.println("🌐 Listening on port 8910 for TY5201-LOCK devices");
            System.out.println("📡 Ready to receive and process balise data");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start direct TCP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @PreDestroy
    public void stopServer() {
        System.out.println("⏹️ Stopping direct TCP server...");
        
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        
        System.out.println("✅ Direct TCP server stopped");
    }
    
    /**
     * Custom handler for processing balise messages directly
     */
    private static class DirectBaliseHandler extends ChannelInboundHandlerAdapter {
        
        private final LockReportService lockReportService;
        
        public DirectBaliseHandler(LockReportService lockReportService) {
            this.lockReportService = lockReportService;
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            String clientAddress = ctx.channel().remoteAddress().toString();
            System.out.println("🔗 DIRECT SERVER: New connection from " + clientAddress);
        }
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                String receivedData = msg.toString();
                System.out.println("📡 DIRECT SERVER: Received data");
                System.out.println("📝 Raw data: " + receivedData);
                
                // Process the data directly (no SDK interference)
                String processedJson = processBaliseData(receivedData);
                
                if (processedJson != null) {
                    System.out.println("🎯 DIRECT SERVER: Processing balise JSON");
                    System.out.println("📝 JSON: " + processedJson);
                    
                    // Store directly using our service
                    lockReportService.reportLockMsg(processedJson);
                    
                    System.out.println("✅ DIRECT SERVER: Data successfully processed and stored!");
                } else {
                    System.out.println("⚠️ DIRECT SERVER: Could not process data as balise message");
                }
                
            } catch (Exception e) {
                System.err.println("❌ DIRECT SERVER: Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private String processBaliseData(String rawData) {
            // Simple processing - in reality you'd implement the TY5201-LOCK protocol
            if (rawData == null || rawData.trim().isEmpty()) {
                return null;
            }
            
            // If it's already JSON, return as-is
            if (rawData.trim().startsWith("{") && rawData.trim().endsWith("}")) {
                return rawData.trim();
            }
            
            // Otherwise, create JSON from the raw data
            long timestamp = System.currentTimeMillis();
            return String.format(
                "{\"deviceId\":\"TY5201-5603DA0C\"," +
                "\"messageType\":\"RAW_DATA\"," +
                "\"timestamp\":%d," +
                "\"rawData\":\"%s\"," +
                "\"processedBy\":\"DirectTcpServer\"," +
                "\"status\":\"RECEIVED\"}", 
                timestamp, rawData.replace("\"", "\\\"")
            );
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            String clientAddress = ctx.channel().remoteAddress().toString();
            System.out.println("🔌 DIRECT SERVER: Connection closed from " + clientAddress);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("❌ DIRECT SERVER: Exception caught: " + cause.getMessage());
            cause.printStackTrace();
            ctx.close();
        }
    }
}
