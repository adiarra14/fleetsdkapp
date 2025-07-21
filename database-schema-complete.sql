-- PostgreSQL Database Creation Script for Balise Management System
-- Compatible with JPA model classes: Balise, Customer, BaliseCustomerAssignment
-- Run this script to create the complete database schema

-- Enable PostGIS extension for spatial data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (for clean redeployment)
DROP TABLE IF EXISTS balise_customer_assignments CASCADE;
DROP TABLE IF EXISTS balise_events CASCADE;
DROP TABLE IF EXISTS balises CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS containers CASCADE;

-- Create customers table
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create balises table
CREATE TABLE balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    serial_number VARCHAR(255),
    model VARCHAR(255),
    firmware_version VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    last_seen TIMESTAMP WITH TIME ZONE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    battery_level INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create balise_customer_assignments table (junction table for many-to-many relationship)
CREATE TABLE balise_customer_assignments (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER NOT NULL REFERENCES balises(id) ON DELETE CASCADE,
    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    equipment_reference VARCHAR(255), -- Customer-specific equipment ID
    container_number VARCHAR(255),    -- For CMA-CGM container assignments
    assignment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    unassignment_date TIMESTAMP WITH TIME ZONE,
    sync_enabled BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(balise_id, customer_id, assignment_date) -- Prevent duplicate active assignments
);

-- Create balise_events table for tracking balise data
CREATE TABLE balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    battery_level INTEGER,
    speed DECIMAL(10,2),
    heading DECIMAL(10,2),
    temperature DECIMAL(5,2),
    message_raw TEXT,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create containers table (for CMA-CGM integration)
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    container_number VARCHAR(255) UNIQUE NOT NULL,
    container_type VARCHAR(50),
    status VARCHAR(50),
    customer_id INTEGER REFERENCES customers(id),
    current_balise_id INTEGER REFERENCES balises(id),
    origin_port VARCHAR(100),
    destination_port VARCHAR(100),
    departure_date TIMESTAMP WITH TIME ZONE,
    arrival_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_balises_device_id ON balises(device_id);
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balises_last_seen ON balises(last_seen);
CREATE INDEX idx_customers_code ON customers(code);
CREATE INDEX idx_customers_active ON customers(is_active);
CREATE INDEX idx_assignments_balise ON balise_customer_assignments(balise_id);
CREATE INDEX idx_assignments_customer ON balise_customer_assignments(customer_id);
CREATE INDEX idx_assignments_sync ON balise_customer_assignments(sync_enabled);
CREATE INDEX idx_events_balise ON balise_events(balise_id);
CREATE INDEX idx_events_time ON balise_events(event_time);
CREATE INDEX idx_events_type ON balise_events(event_type);
CREATE INDEX idx_containers_number ON containers(container_number);

-- Insert default customers
INSERT INTO customers (name, code, contact_email, is_active) VALUES
('CMA CGM', 'CMACGM', 'integration@cmacgm.com', true),
('Default Customer', 'DEFAULT', 'admin@maxvision.com', true),
('Test Customer', 'TEST', 'test@maxvision.com', true);

-- Insert sample balises for testing
INSERT INTO balises (device_id, serial_number, model, status) VALUES
('BAL001', 'SN001', 'MaxVision-V1', 'ACTIVE'),
('BAL002', 'SN002', 'MaxVision-V1', 'ACTIVE'),
('BAL003', 'SN003', 'MaxVision-V2', 'MAINTENANCE'),
('BAL004', 'SN004', 'MaxVision-V2', 'ACTIVE'),
('BAL005', 'SN005', 'MaxVision-V1', 'ACTIVE');

-- Insert sample assignments
INSERT INTO balise_customer_assignments (balise_id, customer_id, equipment_reference, sync_enabled) VALUES
(1, 1, 'CMACGM-001', true),  -- BAL001 assigned to CMA CGM
(2, 2, 'DEFAULT-001', true), -- BAL002 assigned to Default Customer
(4, 1, 'CMACGM-002', true),  -- BAL004 assigned to CMA CGM
(5, 2, 'DEFAULT-002', true); -- BAL005 assigned to Default Customer

-- Insert sample containers for CMA-CGM
INSERT INTO containers (container_number, container_type, status, customer_id, current_balise_id) VALUES
('CMAU1234567', '20FT', 'IN_TRANSIT', 1, 1),
('CMAU7654321', '40FT', 'AT_PORT', 1, 4);

-- Create a view for easy querying of balise assignments
CREATE VIEW balise_assignment_view AS
SELECT 
    b.id as balise_id,
    b.device_id,
    b.serial_number,
    b.model,
    b.status as balise_status,
    b.last_seen,
    b.battery_level,
    c.id as customer_id,
    c.name as customer_name,
    c.code as customer_code,
    bca.equipment_reference,
    bca.container_number,
    bca.assignment_date,
    bca.sync_enabled
FROM balises b
LEFT JOIN balise_customer_assignments bca ON b.id = bca.balise_id AND bca.unassignment_date IS NULL
LEFT JOIN customers c ON bca.customer_id = c.id;

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_app_user;

-- Display summary
SELECT 'Database schema created successfully!' as status;
SELECT COUNT(*) as total_customers FROM customers;
SELECT COUNT(*) as total_balises FROM balises;
SELECT COUNT(*) as total_assignments FROM balise_customer_assignments;
SELECT COUNT(*) as total_containers FROM containers;
