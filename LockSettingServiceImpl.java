package com.maxvision.fleet.sdk;

import com.maxvision.edge.gateway.sdk.setting.LockSettingService;
import org.springframework.stereotype.Service;

@Service
public class LockSettingServiceImpl implements LockSettingService {

    @Override
    public void authSealOrUnsealCard(String deviceId, String command) {
        System.out.println("=== SDK AUTH SEAL/UNSEAL COMMAND ===");
        System.out.println("Device: " + deviceId + ", Command: " + command);
        // Implementation for auth seal/unseal commands
    }

    @Override
    public void gpsIntervalSetting(String deviceId, int interval) {
        System.out.println("=== SDK GPS INTERVAL SETTING ===");
        System.out.println("Device: " + deviceId + ", Interval: " + interval);
        // Implementation for GPS interval settings
    }

    @Override
    public void smsVipSetting(String deviceId, String phoneNumber) {
        System.out.println("=== SDK SMS VIP SETTING ===");
        System.out.println("Device: " + deviceId + ", Phone: " + phoneNumber);
        // Implementation for SMS VIP settings
    }

    @Override
    public void multiIpSetting(String deviceId, String ipAddresses) {
        System.out.println("=== SDK MULTI IP SETTING ===");
        System.out.println("Device: " + deviceId + ", IPs: " + ipAddresses);
        // Implementation for multi IP settings
    }

    @Override
    public void operateCommand(String deviceId, String operation) {
        System.out.println("=== SDK OPERATE COMMAND ===");
        System.out.println("Device: " + deviceId + ", Operation: " + operation);
        // Implementation for operate commands
    }

    @Override
    public void changeDeviceMode(String deviceId, String mode) {
        System.out.println("=== SDK CHANGE DEVICE MODE ===");
        System.out.println("Device: " + deviceId + ", Mode: " + mode);
        // Implementation for device mode changes
    }
}
