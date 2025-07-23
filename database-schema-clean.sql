-- Clean Database Setup Script for Balise Management System
-- This script creates the schema without any example data

-- Enable PostGIS extension for geographical data support
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS balise_events CASCADE;
DROP TABLE IF EXISTS balise_customer_assignments CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS balises CASCADE;
DROP TABLE IF EXISTS containers CASCADE;

-- Create balises table - core device information
CREATE TABLE balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE,
    serial_number VARCHAR(255) UNIQUE,
    model VARCHAR(50) DEFAULT 'TY5201-LOCK',
    firmware_version VARCHAR(50),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    creation_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    battery_level INTEGER,
    last_ip VARCHAR(50)
);

-- Create customers table - for multi-customer support
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    contact_name VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE'
);

-- Create containers table - for CMA-CGM and other shipping containers
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    container_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id INTEGER REFERENCES customers(id),
    type VARCHAR(50),
    size VARCHAR(10),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create balise_customer_assignments - for tracking device assignments
CREATE TABLE balise_customer_assignments (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id) ON DELETE CASCADE,
    customer_id INTEGER REFERENCES customers(id),
    container_id INTEGER REFERENCES containers(id),
    assignment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    unassignment_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    assignment_notes TEXT,
    assigned_by VARCHAR(255)
);

-- Create balise_events table - for storing device events and telemetry
CREATE TABLE balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    battery_level INTEGER,
    signal_strength INTEGER,
    message_raw TEXT,
    payload JSONB
);

-- Create indexes for better query performance
CREATE INDEX idx_balises_device_id ON balises(device_id);
CREATE INDEX idx_balises_serial_number ON balises(serial_number);
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX idx_balise_events_event_type ON balise_events(event_type);
CREATE INDEX idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX idx_assignments_balise_id ON balise_customer_assignments(balise_id);
CREATE INDEX idx_assignments_customer_id ON balise_customer_assignments(customer_id);
CREATE INDEX idx_assignments_container_id ON balise_customer_assignments(container_id);
CREATE INDEX idx_containers_customer_id ON containers(customer_id);

-- Grant permissions to the database user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adminbdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adminbdb;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO adminbdb;

-- Verify tables were created successfully
SELECT table_name, table_schema 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '=== BALISE MANAGEMENT DATABASE SETUP COMPLETE ===';
    RAISE NOTICE 'Tables created: balises, customers, containers, balise_customer_assignments, balise_events';
    RAISE NOTICE 'PostGIS extension enabled for location data';
    RAISE NOTICE 'Indexes created for optimized query performance';
    RAISE NOTICE 'Permissions granted to adminbdb user';
    RAISE NOTICE 'Database is ready for balise data processing!';
END $$;
