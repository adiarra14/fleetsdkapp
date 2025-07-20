/**
 * Fleet Monitor - CMA-CGM Integration Module
 * Event Data Model
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
public class Event {
    
    @JsonProperty("equipmentReference")
    private String equipmentReference;
    
    @JsonProperty("eventCreatedDateTime")
    private String eventCreatedDateTime;
    
    @JsonProperty("originatorName")
    private String originatorName;
    
    @JsonProperty("partnerName")
    private String partnerName;
    
    @JsonProperty("eventType")
    private String eventType; // TRANSPORT, EQUIPMENT, SHIPMENT
    
    @JsonProperty("eventClassifierCode")
    private String eventClassifierCode; // ACT, EST, PLN
    
    @JsonProperty("transportEventTypeCode")
    private String transportEventTypeCode; // ARRI, DEPA
    
    @JsonProperty("equipmentEventTypeCode")
    private String equipmentEventTypeCode; // LOAD, DISC, PICK, DROP
    
    @JsonProperty("shipmentEventTypeCode")
    private String shipmentEventTypeCode;
    
    @JsonProperty("eventLocation")
    private EventLocation eventLocation;
    
    @JsonProperty("reeferTemperature")
    private Double reeferTemperature;
    
    @JsonProperty("reeferTemperatureUnit")
    private String reeferTemperatureUnit;
    
    // Constructors
    public Event() {}
    
    public Event(String equipmentReference, String eventCreatedDateTime, 
                String originatorName, String eventType, String eventClassifierCode,
                EventLocation eventLocation) {
        this.equipmentReference = equipmentReference;
        this.eventCreatedDateTime = eventCreatedDateTime;
        this.originatorName = originatorName;
        this.eventType = eventType;
        this.eventClassifierCode = eventClassifierCode;
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
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getEventClassifierCode() {
        return eventClassifierCode;
    }
    
    public void setEventClassifierCode(String eventClassifierCode) {
        this.eventClassifierCode = eventClassifierCode;
    }
    
    public String getTransportEventTypeCode() {
        return transportEventTypeCode;
    }
    
    public void setTransportEventTypeCode(String transportEventTypeCode) {
        this.transportEventTypeCode = transportEventTypeCode;
    }
    
    public String getEquipmentEventTypeCode() {
        return equipmentEventTypeCode;
    }
    
    public void setEquipmentEventTypeCode(String equipmentEventTypeCode) {
        this.equipmentEventTypeCode = equipmentEventTypeCode;
    }
    
    public String getShipmentEventTypeCode() {
        return shipmentEventTypeCode;
    }
    
    public void setShipmentEventTypeCode(String shipmentEventTypeCode) {
        this.shipmentEventTypeCode = shipmentEventTypeCode;
    }
    
    public EventLocation getEventLocation() {
        return eventLocation;
    }
    
    public void setEventLocation(EventLocation eventLocation) {
        this.eventLocation = eventLocation;
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
        return "Event{" +
                "equipmentReference='" + equipmentReference + '\'' +
                ", eventCreatedDateTime='" + eventCreatedDateTime + '\'' +
                ", originatorName='" + originatorName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventClassifierCode='" + eventClassifierCode + '\'' +
                ", eventLocation=" + eventLocation +
                '}';
    }
}
