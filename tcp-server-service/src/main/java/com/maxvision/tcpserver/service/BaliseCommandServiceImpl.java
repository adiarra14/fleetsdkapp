package com.maxvision.tcpserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxvision.edge.gateway.exception.CustomException;
import com.maxvision.edge.gateway.model.AuthSealOrUnsealCardEncoderModel;
import com.maxvision.edge.gateway.model.GpsIntervalSettingEncoderModel;
import com.maxvision.edge.gateway.model.SmsVipSettingEncoderModel;
import com.maxvision.edge.gateway.model.MultiIpEncoderModel;
import com.maxvision.edge.gateway.model.OperateCommandEncoderModel;
import com.maxvision.edge.gateway.model.ChangeDeviceModeEncoderModel;
import com.maxvision.edge.gateway.service.LockSettingService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Implementation of LockSettingService to send commands to balise devices.
 * This service provides methods to configure and control balises according to the SDK protocol.
 */
@Service
@Slf4j
public class BaliseCommandServiceImpl implements LockSettingService {

    private static final String DB_URL = "jdbc:postgresql://balise-postgres:5432/balisedb";
    private static final String DB_USER = "adminbdb";
    private static final String DB_PASSWORD = "To7Z2UCeWTsriPxbADX8";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Authorize or unauthorize an RFID card for temporary seal/unseal operations on the balise
     */
    @Override
    public void authSealOrUnsealCard(AuthSealOrUnsealCardEncoderModel model) throws CustomException {
        try {
            log.info("Sending auth card command to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            if (model.getSubCmdType() == null || !model.getSubCmdType().equals(model.getCommandType())) {
                throw new CustomException("2001", "subCmdType is error");
            }
            
            if (model.getCardType() == null || 
                    (!model.getCardType().equals("universal") && !model.getCardType().equals("temporary"))) {
                throw new CustomException("2002", "cardType is error");
            }
            
            if (model.getCardNo() == null || !model.getCardNo().matches("[0-9A-F]{8}")) {
                throw new CustomException("2003", "cardNo is error");
            }
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), "authSealOrUnsealCard", 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            log.info("Auth card command sent to balise {}: card {} ({}) binding={}", 
                    model.getLockCode(), model.getCardNo(), model.getCardType(), model.getBinding());
            
        } catch (CustomException e) {
            log.error("Error sending auth card command: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error sending auth card command: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Configure the interval at which the balise reports its GPS location
     */
    @Override
    public void gpsIntervalSetting(GpsIntervalSettingEncoderModel model) throws CustomException {
        try {
            log.info("Sending GPS interval setting to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            // Validate GPS interval (must be 10-65535)
            int interval;
            try {
                interval = Integer.parseInt(model.getGpsInterval());
                if (interval < 10 || interval > 65535) {
                    throw new CustomException("3001", "gpsIntervalSetting is error: must be between 10-65535");
                }
            } catch (NumberFormatException e) {
                throw new CustomException("3001", "gpsIntervalSetting is error: not a valid number");
            }
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), "gpsIntervalSetting", 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            log.info("GPS interval set for balise {}: {} seconds", model.getLockCode(), interval);
            
        } catch (CustomException e) {
            log.error("Error setting GPS interval: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error setting GPS interval: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Configure the balise's SMS VIP list (phone numbers that receive SMS alerts)
     */
    @Override
    public void smsVipSetting(SmsVipSettingEncoderModel model) throws CustomException {
        try {
            log.info("Sending SMS VIP setting to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            // Validate SMS VIP list
            if (model.getSmsVipList() == null || model.getSmsVipList().isEmpty()) {
                throw new CustomException("4001", "smsVipList cannot be empty");
            }
            
            // Validate each SMS VIP entry
            model.getSmsVipList().forEach(vip -> {
                // Check index (1-5)
                try {
                    int index = Integer.parseInt(vip.getIndex());
                    if (index < 1 || index > 5) {
                        throw new CustomException("4002", "index is error: must be between 1-5");
                    }
                } catch (NumberFormatException e) {
                    throw new CustomException("4002", "index is error: not a valid number");
                }
                
                // Check phone number (4-20 chars)
                if (vip.getPhone() == null || vip.getPhone().length() < 4 || vip.getPhone().length() > 20) {
                    throw new CustomException("4003", "phone is error: length must be 4-20 characters");
                }
            });
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), "smsVipSetting", 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            log.info("SMS VIP list set for balise {}: {} entries", model.getLockCode(), model.getSmsVipList().size());
            
        } catch (CustomException e) {
            log.error("Error setting SMS VIP list: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error setting SMS VIP list: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Configure the device's primary and secondary server connection settings
     */
    @Override
    public void multiIpSetting(MultiIpEncoderModel model) throws CustomException {
        try {
            log.info("Sending multi-IP setting to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            // Validate primary server settings
            if (model.getPrimary() == null) {
                throw new CustomException("5001", "primary cannot be null");
            }
            
            // Validate IP/domain
            if (model.getPrimary().getIp() == null || model.getPrimary().getIp().isEmpty()) {
                throw new CustomException("5002", "ip is error");
            }
            
            // Validate TCP port
            try {
                int port = Integer.parseInt(model.getPrimary().getTcpPort());
                if (port < 1 || port > 65535) {
                    throw new CustomException("5003", "tcpPort is error: must be between 1-65535");
                }
            } catch (NumberFormatException e) {
                throw new CustomException("5003", "tcpPort is error: not a valid number");
            }
            
            // Validate APN settings if provided
            if (model.getPrimary().getApn() != null && model.getPrimary().getApn().length() > 50) {
                throw new CustomException("5004", "apn is error: max length is 50 characters");
            }
            
            if (model.getPrimary().getAccount() != null && model.getPrimary().getAccount().length() > 50) {
                throw new CustomException("5005", "account is error: max length is 50 characters");
            }
            
            if (model.getPrimary().getPassword() != null && model.getPrimary().getPassword().length() > 50) {
                throw new CustomException("5006", "password is error: max length is 50 characters");
            }
            
            // Validate secondary server settings if provided
            if (model.getSecondary() != null) {
                if (model.getSecondary().getIp() == null || model.getSecondary().getIp().isEmpty()) {
                    throw new CustomException("5002", "secondary ip is error");
                }
                
                try {
                    int port = Integer.parseInt(model.getSecondary().getTcpPort());
                    if (port < 1 || port > 65535) {
                        throw new CustomException("5003", "secondary tcpPort is error: must be between 1-65535");
                    }
                } catch (NumberFormatException e) {
                    throw new CustomException("5003", "secondary tcpPort is error: not a valid number");
                }
                
                if (model.getSecondary().getApn() != null && model.getSecondary().getApn().length() > 50) {
                    throw new CustomException("5004", "secondary apn is error: max length is 50 characters");
                }
            }
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), "multiIpSetting", 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            log.info("Multi-IP settings set for balise {}: primary={}, secondary={}", 
                    model.getLockCode(), model.getPrimary().getIp(), 
                    model.getSecondary() != null ? model.getSecondary().getIp() : "none");
            
        } catch (CustomException e) {
            log.error("Error setting multi-IP: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error setting multi-IP: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Send an operational command to seal (lock) or unseal (unlock) the balise
     */
    @Override
    public void operateCommand(OperateCommandEncoderModel model) throws CustomException {
        try {
            log.info("Sending operate command to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            // Validate command type
            if (model.getCmdType() == null || !model.getCmdType().equals(model.getCommandType())) {
                throw new CustomException("6001", "cmdType is error");
            }
            
            // Check that commandType is either "seal" or "unseal"
            if (!model.getCommandType().equals("seal") && !model.getCommandType().equals("unseal")) {
                throw new CustomException("1004", "commandType is error: must be 'seal' or 'unseal'");
            }
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), model.getCommandType(), 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            String operation = model.getCommandType().equals("seal") ? "sealed" : "unsealed";
            log.info("Balise {} has been commanded to be {}", model.getLockCode(), operation);
            
        } catch (CustomException e) {
            log.error("Error sending operate command: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error sending operate command: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Switch the device's operating mode between "trip activation" and "end of trip"
     */
    @Override
    public void changeDeviceMode(ChangeDeviceModeEncoderModel model) throws CustomException {
        try {
            log.info("Sending device mode change to balise {}: {}", model.getLockCode(), model);
            
            // Validate command parameters
            validateCommonParams(model.getCommandLogId(), model.getLockCode(), model.getCommandType());
            
            // Validate device mode (must be 0 or 1)
            if (model.getDeviceMode() != 0 && model.getDeviceMode() != 1) {
                throw new CustomException("7001", "deviceMode is error: must be 0 or 1");
            }
            
            // Log command in database for tracking
            logCommandToDb(model.getCommandLogId(), model.getLockCode(), "changeDeviceMode", 
                    objectMapper.writeValueAsString(model));
            
            // In a real implementation, this would send the command to the SDK
            // Here we just log it for demonstration
            String modeStr = model.getDeviceMode() == 1 ? "trip activation (in transit)" : "end of trip";
            log.info("Device mode for balise {} changed to {}", model.getLockCode(), modeStr);
            
        } catch (CustomException e) {
            log.error("Error changing device mode: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error changing device mode: {}", e.getMessage(), e);
            throw new CustomException("1000", "Internal error: " + e.getMessage());
        }
    }
    
    /**
     * Validate common parameters required for all commands
     */
    private void validateCommonParams(String commandLogId, String lockCode, String commandType) throws CustomException {
        if (commandLogId == null || commandLogId.isEmpty()) {
            throw new CustomException("1002", "commandLogId is error");
        }
        
        if (lockCode == null || lockCode.isEmpty() || !lockCode.matches("[A-Z]{4}\\d{10}")) {
            throw new CustomException("1003", "lockCode is error");
        }
        
        if (commandType == null || commandType.isEmpty()) {
            throw new CustomException("1004", "commandType is error");
        }
    }
    
    /**
     * Log command to the database for tracking and auditing
     */
    private void logCommandToDb(String commandId, String baliseId, String commandType, String commandData) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO balise_commands (command_id, balise_imei, command_type, command_data, created_at, status) " +
                         "VALUES (?, ?, ?, ?, NOW(), 'SENT')";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, commandId);
                stmt.setString(2, baliseId);
                stmt.setString(3, commandType);
                stmt.setString(4, commandData);
                
                int inserted = stmt.executeUpdate();
                log.debug("Logged command {} to database: {} rows inserted", commandId, inserted);
            }
        } catch (SQLException e) {
            log.error("Error logging command to database: {}", e.getMessage(), e);
        }
    }
}
