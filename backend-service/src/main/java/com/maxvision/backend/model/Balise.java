package com.maxvision.backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "balises")
public class Balise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId; // Unique balise identifier
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Column(name = "model")
    private String model;
    
    @Column(name = "firmware_version")
    private String firmwareVersion;
    
    @Column(name = "status")
    private String status; // ACTIVE, INACTIVE, MAINTENANCE, etc.
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "battery_level")
    private Integer batteryLevel;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationship with customer assignments
    @OneToMany(mappedBy = "balise", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BaliseCustomerAssignment> customerAssignments;
    
    // Constructors
    public Balise() {}
    
    public Balise(String deviceId, String serialNumber, String model) {
        this.deviceId = deviceId;
        this.serialNumber = serialNumber;
        this.model = model;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Integer getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<BaliseCustomerAssignment> getCustomerAssignments() {
        return customerAssignments;
    }
    
    public void setCustomerAssignments(List<BaliseCustomerAssignment> customerAssignments) {
        this.customerAssignments = customerAssignments;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Balise{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", model='" + model + '\'' +
                ", status='" + status + '\'' +
                ", lastSeen=" + lastSeen +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
