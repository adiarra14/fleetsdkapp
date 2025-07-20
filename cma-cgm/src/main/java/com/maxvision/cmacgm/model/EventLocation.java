/**
 * Fleet Monitor - CMA-CGM Integration Module
 * EventLocation Data Model
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
public class EventLocation {
    
    @JsonProperty("facilityTypeCode")
    private String facilityTypeCode; // CLOC, DEPO, POTE, RAMP
    
    @JsonProperty("locationCode")
    private String locationCode;
    
    @JsonProperty("locationName")
    private String locationName;
    
    @JsonProperty("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    private Double longitude;
    
    @JsonProperty("address")
    private Address address;
    
    // Constructors
    public EventLocation() {}
    
    public EventLocation(String facilityTypeCode, String locationCode, 
                        Double latitude, Double longitude) {
        this.facilityTypeCode = facilityTypeCode;
        this.locationCode = locationCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getFacilityTypeCode() {
        return facilityTypeCode;
    }
    
    public void setFacilityTypeCode(String facilityTypeCode) {
        this.facilityTypeCode = facilityTypeCode;
    }
    
    public String getLocationCode() {
        return locationCode;
    }
    
    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
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
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
    
    @Override
    public String toString() {
        return "EventLocation{" +
                "facilityTypeCode='" + facilityTypeCode + '\'' +
                ", locationCode='" + locationCode + '\'' +
                ", locationName='" + locationName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
    
    // Address nested class
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("street")
        private String street;
        
        @JsonProperty("streetNumber")
        private String streetNumber;
        
        @JsonProperty("floor")
        private String floor;
        
        @JsonProperty("postCode")
        private String postCode;
        
        @JsonProperty("city")
        private String city;
        
        @JsonProperty("stateRegion")
        private String stateRegion;
        
        @JsonProperty("country")
        private String country;
        
        // Constructors
        public Address() {}
        
        public Address(String name, String street, String city, String country) {
            this.name = name;
            this.street = street;
            this.city = city;
            this.country = country;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getStreet() {
            return street;
        }
        
        public void setStreet(String street) {
            this.street = street;
        }
        
        public String getStreetNumber() {
            return streetNumber;
        }
        
        public void setStreetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
        }
        
        public String getFloor() {
            return floor;
        }
        
        public void setFloor(String floor) {
            this.floor = floor;
        }
        
        public String getPostCode() {
            return postCode;
        }
        
        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getStateRegion() {
            return stateRegion;
        }
        
        public void setStateRegion(String stateRegion) {
            this.stateRegion = stateRegion;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        @Override
        public String toString() {
            return "Address{" +
                    "name='" + name + '\'' +
                    ", street='" + street + '\'' +
                    ", city='" + city + '\'' +
                    ", country='" + country + '\'' +
                    '}';
        }
    }
}
