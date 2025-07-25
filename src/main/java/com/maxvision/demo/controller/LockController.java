package com.maxvision.demo.controller;

import com.maxvision.demo.service.LockDemoService;
import com.maxvision.edge.gateway.lock.netty.handler.model.a.b;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lock")
@RequiredArgsConstructor
public class LockController {

    private final LockDemoService lockDemoService;

    @PostMapping("/auth-seal-card")
    public ResponseEntity<String> authorizeSealCard(
            @RequestParam String lockCode,
            @RequestParam String cardNo) {
        try {
            lockDemoService.authorizeSealCard(lockCode, cardNo);
            return ResponseEntity.ok("Card authorization request sent successfully");
        } catch (Exception e) {
            log.error("Failed to authorize seal card", e);
            return ResponseEntity.internalServerError().body("Failed to authorize seal card: " + e.getMessage());
        }
    }

    @PostMapping("/gps-interval")
    public ResponseEntity<String> setGpsInterval(
            @RequestParam String lockCode,
            @RequestParam int intervalSeconds) {
        try {
            lockDemoService.setGpsInterval(lockCode, intervalSeconds);
            return ResponseEntity.ok("GPS interval setting request sent successfully");
        } catch (Exception e) {
            log.error("Failed to set GPS interval", e);
            return ResponseEntity.internalServerError().body("Failed to set GPS interval: " + e.getMessage());
        }
    }

    @PostMapping("/sms-vip")
    public ResponseEntity<String> configureSmsVip(
            @RequestParam String lockCode,
            @RequestBody List<b> vipSettings) {
        try {
            lockDemoService.configureSmsVipSettings(lockCode, vipSettings);
            return ResponseEntity.ok("SMS VIP settings request sent successfully");
        } catch (Exception e) {
            log.error("Failed to configure SMS VIP settings", e);
            return ResponseEntity.internalServerError().body("Failed to configure SMS VIP settings: " + e.getMessage());
        }
    }

    @PostMapping("/device-mode")
    public ResponseEntity<String> changeDeviceMode(
            @RequestParam String lockCode,
            @RequestParam int mode) {
        if (mode != 0 && mode != 1) {
            return ResponseEntity.badRequest().body("Device mode must be 0 or 1");
        }
        try {
            lockDemoService.changeDeviceMode(lockCode, mode);
            return ResponseEntity.ok("Device mode change request sent successfully");
        } catch (Exception e) {
            log.error("Failed to change device mode", e);
            return ResponseEntity.internalServerError().body("Failed to change device mode: " + e.getMessage());
        }
    }
} 