/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Coordinate Data Model
 * 
 * Project: Fleet Management System with Maxvision SDK Integration
 * Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
 * Project Chief: Antoine Diarra
 * 
 * Copyright (c) 2025 Ynnov
 * All rights reserved. This software and associated documentation files are the property
 * of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
 * 
 * Unauthorized copying, modification, distribution, or use of this software
 * is strictly prohibited without prior written consent from Ynnov.
 */

package com.maxvision.cmacgm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Coordinate {
    
    @JsonProperty("equipmentReference")
    private String equipmentReference;
    
    @JsonProperty("eventCreatedDateTime")
    private String eventCreatedDateTime;
    
    @JsonProperty("originatorName")
    private String originatorName;
    
    @JsonProperty("partnerName")
    private String partnerName;
    
    @JsonProperty("carrierBookingReference")
    private String carrierBookingReference;
    
    @JsonProperty("modeOfTransport")
    private String modeOfTransport;
    
    @JsonProperty("transportOrder")
    private String transportOrder;
    
    @JsonProperty("eventLocation")
    private Location eventLocation;
    
    @JsonProperty("fuelType")
    private String fuelType;
    
    @JsonProperty("truckMileage")
    private Double truckMileage;
    
    @JsonProperty("truckMileageUnit")
    private String truckMileageUnit;
    
    @JsonProperty("reeferTemperature")
    private Double reeferTemperature;
    
    @JsonProperty("reeferTemperatureUnit")
    private String reeferTemperatureUnit;
    
    // Constructors
    public Coordinate() {}
    
    public Coordinate(String equipmentReference, String eventCreatedDateTime, 
                     String originatorName, Location eventLocation) {
        this.equipmentReference = equipmentReference;
        this.eventCreatedDateTime = eventCreatedDateTime;
        this.originatorName = originatorName;
        this.eventLocation = eventLocation;
    }
    
    // Getters and Setters
    public String getEquipmentReference() {
        return equipmentReference;
    }
    
    public void setEquipmentReference(String equipmentReference) {
        this.equipmentReference = equipmentReference;
    }
    
    public String getEventCreatedDateTime() {
        return eventCreatedDateTime;
    }
    
    public void setEventCreatedDateTime(String eventCreatedDateTime) {
        this.eventCreatedDateTime = eventCreatedDateTime;
    }
    
    public String getOriginatorName() {
        return originatorName;
    }
    
    public void setOriginatorName(String originatorName) {
        this.originatorName = originatorName;
    }
    
    public String getPartnerName() {
        return partnerName;
    }
    
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }
    
    public String getCarrierBookingReference() {
        return carrierBookingReference;
    }
    
    public void setCarrierBookingReference(String carrierBookingReference) {
        this.carrierBookingReference = carrierBookingReference;
    }
    
    public String getModeOfTransport() {
        return modeOfTransport;
    }
    
    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }
    
    public String getTransportOrder() {
        return transportOrder;
    }
    
    public void setTransportOrder(String transportOrder) {
        this.transportOrder = transportOrder;
    }
    
    public Location getEventLocation() {
        return eventLocation;
    }
    
    public void setEventLocation(Location eventLocation) {
        this.eventLocation = eventLocation;
    }
    
    public String getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
    
    public Double getTruckMileage() {
        return truckMileage;
    }
    
    public void setTruckMileage(Double truckMileage) {
        this.truckMileage = truckMileage;
    }
    
    public String getTruckMileageUnit() {
        return truckMileageUnit;
    }
    
    public void setTruckMileageUnit(String truckMileageUnit) {
        this.truckMileageUnit = truckMileageUnit;
    }
    
    public Double getReeferTemperature() {
        return reeferTemperature;
    }
    
    public void setReeferTemperature(Double reeferTemperature) {
        this.reeferTemperature = reeferTemperature;
    }
    
    public String getReeferTemperatureUnit() {
        return reeferTemperatureUnit;
    }
    
    public void setReeferTemperatureUnit(String reeferTemperatureUnit) {
        this.reeferTemperatureUnit = reeferTemperatureUnit;
    }
    
    @Override
    public String toString() {
        return "Coordinate{" +
                "equipmentReference='" + equipmentReference + '\'' +
                ", eventCreatedDateTime='" + eventCreatedDateTime + '\'' +
                ", originatorName='" + originatorName + '\'' +
                ", eventLocation=" + eventLocation +
                '}';
    }
}
