package com.cmacgm.integration;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Simple CMA-CGM Integration Server
 * 
 * This is a lightweight HTTP server that provides CMA-CGM integration endpoints
 * without requiring Maven build in Docker/Portainer environment.
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 */
public class SimpleCmaCgmServer {
    
    private static final int PORT = 8081;
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Health check endpoint
        server.createContext("/health", new HealthHandler());
        
        // CMA-CGM API endpoints
        server.createContext("/api/cmacgm/sync", new SyncHandler());
        server.createContext("/api/cmacgm/status", new StatusHandler());
        server.createContext("/api/cmacgm/coordinates", new CoordinatesHandler());
        server.createContext("/api/cmacgm/events", new EventsHandler());
        
        // Set executor
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Start server
        server.start();
        
        System.out.println("CMA-CGM Integration Server started on port " + PORT);
        System.out.println("Health check: http://localhost:" + PORT + "/health");
        System.out.println("CMA-CGM API: http://localhost:" + PORT + "/api/cmacgm/");
        
        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down CMA-CGM Integration Server...");
            server.stop(0);
        }));
    }
    
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"status\":\"UP\",\"service\":\"CMA-CGM Integration\",\"timestamp\":\"" + 
                            java.time.Instant.now() + "\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    static class SyncHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\":\"CMA-CGM sync triggered\",\"status\":\"processing\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"sync_status\":\"active\",\"last_sync\":\"" + 
                            java.time.Instant.now() + "\",\"records_synced\":0}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    static class CoordinatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\":\"Coordinates endpoint ready\",\"default_location\":\"Bamako, Mali\",\"partner\":\"SINIgroupe\",\"country\":\"Mali\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    
    static class EventsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"message\":\"Events endpoint ready\",\"supported_events\":[\"GPS\",\"LOCK\",\"UNLOCK\"],\"default_location\":\"Bamako, Mali\",\"partner\":\"SINIgroupe\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
