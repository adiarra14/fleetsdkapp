package com.maxvision.edge.gateway.lock.netty.handler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Login message model for Maxvision Edge Protocol Gateway SDK.
 * Represents the initial connection message from a lock device.
 */
public class LoginMessage implements Message {
    private String lockCode;
    private String gpsUploadInterval;
    private String version;
    private String deviceMode;
    private Map<String, Object> lockGpsResModel = new HashMap<>();
    private String ipAddress;
    
    public LoginMessage(String lockCode, String ipAddress) {
        this.lockCode = lockCode;
        this.ipAddress = ipAddress;
        this.gpsUploadInterval = "60";  // Default 60 seconds
        this.version = "v1.0";
        this.deviceMode = "0";  // End-of-trip mode by default
        
        // Default GPS model data
        Map<String, String> gps = new HashMap<>();
        gps.put("lng", "0.0");
        gps.put("lat", "0.0");
        
        this.lockGpsResModel.put("terminalStatusList", new ArrayList<String>());
        this.lockGpsResModel.put("warningMessageList", new ArrayList<String>());
        this.lockGpsResModel.put("gpsTime", System.currentTimeMillis());
        this.lockGpsResModel.put("gps", gps);
        this.lockGpsResModel.put("gpsValid", false);
        this.lockGpsResModel.put("speed", "0");
        this.lockGpsResModel.put("voltage", "50");  // 50% battery level
        this.lockGpsResModel.put("satelliteNum", "0");
        this.lockGpsResModel.put("satelliteSignalValueList", new ArrayList<Integer>());
        this.lockGpsResModel.put("lockStatus", "unseal");
        this.lockGpsResModel.put("direction", "0");
    }
    
    @Override
    public String getLockCode() { return lockCode; }
    
    @Override
    public String getMessageType() { return "login"; }
    
    public String getIpAddress() { return ipAddress; }
    
    // Additional getters
    public String getGpsUploadInterval() { return gpsUploadInterval; }
    public String getVersion() { return version; }
    public String getDeviceMode() { return deviceMode; }
    public Map<String, Object> getLockGpsResModel() { return lockGpsResModel; }
    
    // Setters for test customization
    public void setGpsUploadInterval(String interval) { this.gpsUploadInterval = interval; }
    public void setVersion(String version) { this.version = version; }
    public void setDeviceMode(String mode) { this.deviceMode = mode; }
    
    // Add status flag
    public void addTerminalStatus(String status) {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>)this.lockGpsResModel.get("terminalStatusList");
        list.add(status);
    }
    
    // Add warning/alarm
    public void addWarningMessage(String warning) {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>)this.lockGpsResModel.get("warningMessageList");
        list.add(warning);
    }
    
    // Set location
    public void setLocation(double longitude, double latitude, boolean valid) {
        Map<String, String> gps = new HashMap<>();
        gps.put("lng", String.valueOf(longitude));
        gps.put("lat", String.valueOf(latitude));
        this.lockGpsResModel.put("gps", gps);
        this.lockGpsResModel.put("gpsValid", valid);
    }
    
    // Set battery level
    public void setBatteryLevel(String level) {
        this.lockGpsResModel.put("voltage", level);
    }
    
    // Set lock status
    public void setLockStatus(String status) {
        this.lockGpsResModel.put("lockStatus", status);
    }
}
