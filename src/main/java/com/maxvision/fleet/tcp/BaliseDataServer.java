package com.maxvision.fleet.tcp;

import com.maxvision.fleet.entity.DeviceReport;
import com.maxvision.fleet.service.DeviceReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TCP Server for receiving balise data transmissions
 */
// DISABLED: Spring TCP server is not allowed with SDK integration
// @Slf4j
// @Component
// public class BaliseDataServer {

    @Value("${balise.tcp.port:6060}")
    private int port;

    @Value("${balise.tcp.threads:10}")
    private int threadPoolSize;

    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private volatile boolean running = false;

    @Autowired
    private DeviceReportService deviceReportService;

    @PostConstruct
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            running = true;

            log.info("Balise TCP Server started on port {}", port);

            // Start a background thread to accept connections
            new Thread(this::acceptConnections).start();
        } catch (IOException e) {
            log.error("Failed to start Balise TCP Server on port {}", port, e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Error closing server socket", e);
            }
        }

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("Balise TCP Server stopped");
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                log.info("New balise connection from: {}", clientSocket.getInetAddress());
                executorService.execute(() -> handleClientConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    log.error("Error accepting client connection", e);
                }
            }
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                log.info("Received balise data: {}", line);
                processBaliseData(line, clientSocket.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            log.error("Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing client socket", e);
            }
        }
    }

    private void processBaliseData(String data, String sourceIp) {
        try {
            // Extract device ID from the data (implementation depends on your protocol)
            String deviceId = extractDeviceId(data);
            
            // Parse command type (status update, GPS position, etc.)
            String commandType = parseCommandType(data);
            
            // Create and save device report
            DeviceReport report = new DeviceReport();
            report.setLockCode(deviceId);
            report.setCommandType(commandType);
            report.setReportData(data);
            report.setReceivedAt(LocalDateTime.now());
            
            deviceReportService.saveReport(report);
            
            log.info("Processed balise data from device: {}, command: {}", deviceId, commandType);
        } catch (Exception e) {
            log.error("Error processing balise data: {}", data, e);
        }
    }
    
    /**
     * Extract device ID from the raw data
     * Implementation depends on the specific balise protocol format
     */
    private String extractDeviceId(String data) {
        // Example implementation - adjust based on your protocol
        // Assumes data format starts with device ID followed by command and data
        // Format: <DEVICE_ID>:<COMMAND>:<DATA>
        if (data != null && data.contains(":")) {
            return data.split(":", 2)[0];
        }
        return "unknown";
    }
    
    /**
     * Parse command type from the raw data
     * Implementation depends on the specific balise protocol format
     */
    private String parseCommandType(String data) {
        // Example implementation - adjust based on your protocol
        // Assumes format: <DEVICE_ID>:<COMMAND>:<DATA>
        if (data != null && data.contains(":")) {
            String[] parts = data.split(":", 3);
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return "unknown";
    }
}
