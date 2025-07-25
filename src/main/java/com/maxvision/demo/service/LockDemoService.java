package com.maxvision.demo.service;

import com.maxvision.edge.gateway.lock.netty.handler.model.a.b;
import com.maxvision.edge.gateway.sdk.model.encoder.*;
import com.maxvision.edge.gateway.sdk.setting.LockSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockDemoService {
    
    private final LockSettingService lockSettingService;

    public void authorizeSealCard(String lockCode, String cardNo) {
        AuthSealOrUnsealCardEncoderModel model = new AuthSealOrUnsealCardEncoderModel();
        model.setCommandLogId(System.currentTimeMillis() + "");
        model.setLockCode(lockCode);
        model.setCommandType("1"); // Auth command
        model.setSubCmdType("1"); // Seal card
        model.setBinding(true);
        model.setCardType("1"); // Example card type
        model.setCardNo(cardNo);
        
        try {
            lockSettingService.authSealOrUnsealCard(model);
            log.info("Successfully sent seal card authorization for card: {}", cardNo);
        } catch (Exception e) {
            log.error("Failed to authorize seal card: {}", e.getMessage(), e);
        }
    }

    public void setGpsInterval(String lockCode, int intervalSeconds) {
        GpsIntervalSettingEncoderModel model = new GpsIntervalSettingEncoderModel();
        model.setCommandLogId(System.currentTimeMillis() + "");
        model.setLockCode(lockCode);
        model.setCommandType("2"); // GPS interval setting
        model.setGpsInterval(String.valueOf(intervalSeconds));
        
        try {
            lockSettingService.gpsIntervalSetting(model);
            log.info("Successfully set GPS interval to {} seconds", intervalSeconds);
        } catch (Exception e) {
            log.error("Failed to set GPS interval: {}", e.getMessage(), e);
        }
    }

    public void configureSmsVipSettings(String lockCode, List<b> vipSettings) {
        SmsVipSettingEncoderModel model = new SmsVipSettingEncoderModel();
        model.setCommandLogId(System.currentTimeMillis() + "");
        model.setLockCode(lockCode);
        model.setCommandType("3"); // SMS VIP setting
        model.setSmsVipList(vipSettings);
        
        try {
            lockSettingService.smsVipSetting(model);
            log.info("Successfully configured SMS VIP settings");
        } catch (Exception e) {
            log.error("Failed to configure SMS VIP settings: {}", e.getMessage(), e);
        }
    }

    public void changeDeviceMode(String lockCode, int mode) {
        ChangeDeviceModeEncoderModel model = new ChangeDeviceModeEncoderModel();
        model.setCommandLogId(System.currentTimeMillis() + "");
        model.setLockCode(lockCode);
        model.setCommandType("6"); // Change device mode
        model.setDeviceMode(mode); // 0 or 1
        
        try {
            lockSettingService.changeDeviceMode(model);
            log.info("Successfully changed device mode to: {}", mode);
        } catch (Exception e) {
            log.error("Failed to change device mode: {}", e.getMessage(), e);
        }
    }
} 