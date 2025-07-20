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

/**
 * Assignment of balises to specific customers
 * 
 * This allows the system to:
 * - Assign specific balises to CMA-CGM
 * - Assign other balises to different customers
 * - Control which data goes where
 * - Enable/disable sync per assignment
 */
@Entity
@Table(name = "balise_customer_assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"balise_id", "customer_id"}))
public class BaliseCustomerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "balise_id", nullable = false)
    private Balise balise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "equipment_reference")
    private String equipmentReference; // Customer-specific equipment ID

    @Column(name = "sync_enabled")
    private Boolean syncEnabled = true;

    @Column(name = "sync_gps")
    private Boolean syncGps = true;

    @Column(name = "sync_events")
    private Boolean syncEvents = true;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "assigned_by")
    private String assignedBy; // Username who made the assignment

    @Column(name = "notes")
    private String notes;

    // Constructors
    public BaliseCustomerAssignment() {}

    public BaliseCustomerAssignment(Balise balise, Customer customer, String equipmentReference) {
        this.balise = balise;
        this.customer = customer;
        this.equipmentReference = equipmentReference;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Balise getBalise() { return balise; }
    public void setBalise(Balise balise) { this.balise = balise; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getEquipmentReference() { return equipmentReference; }
    public void setEquipmentReference(String equipmentReference) { this.equipmentReference = equipmentReference; }

    public Boolean getSyncEnabled() { return syncEnabled; }
    public void setSyncEnabled(Boolean syncEnabled) { this.syncEnabled = syncEnabled; }

    public Boolean getSyncGps() { return syncGps; }
    public void setSyncGps(Boolean syncGps) { this.syncGps = syncGps; }

    public Boolean getSyncEvents() { return syncEvents; }
    public void setSyncEvents(Boolean syncEvents) { this.syncEvents = syncEvents; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
