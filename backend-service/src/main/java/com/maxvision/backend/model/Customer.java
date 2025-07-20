/*
 * Fleet Management System with Maxvision SDK Integration
 * 
 * Project: Fleet Management System with Multi-Customer Support
 * Client: SiniTechnologie / Sinigroupe
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This code is proprietary until transfer to client.
 */

package com.maxvision.backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Customer entity for multi-customer balise management
 * 
 * Represents different customers (CMA-CGM, DHL, etc.) that receive
 * data from specific assigned balises
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code; // e.g., "CMACGM", "DHL", "MAERSK"

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "api_endpoint")
    private String apiEndpoint;

    @Column(name = "api_credentials")
    private String apiCredentials; // Encrypted

    @Column(name = "sync_enabled")
    private Boolean syncEnabled = true;

    @Column(name = "sync_interval_minutes")
    private Integer syncIntervalMinutes = 5;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relationship to balises assigned to this customer
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private Set<BaliseCustomerAssignment> baliseAssignments;

    // Constructors
    public Customer() {}

    public Customer(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }

    public String getApiCredentials() { return apiCredentials; }
    public void setApiCredentials(String apiCredentials) { this.apiCredentials = apiCredentials; }

    public Boolean getSyncEnabled() { return syncEnabled; }
    public void setSyncEnabled(Boolean syncEnabled) { this.syncEnabled = syncEnabled; }

    public Integer getSyncIntervalMinutes() { return syncIntervalMinutes; }
    public void setSyncIntervalMinutes(Integer syncIntervalMinutes) { this.syncIntervalMinutes = syncIntervalMinutes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<BaliseCustomerAssignment> getBaliseAssignments() { return baliseAssignments; }
    public void setBaliseAssignments(Set<BaliseCustomerAssignment> baliseAssignments) { this.baliseAssignments = baliseAssignments; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
