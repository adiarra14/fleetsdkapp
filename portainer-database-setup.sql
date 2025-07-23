-- =====================================================
-- PORTAINER DATABASE SETUP SCRIPT
-- Execute this in pgAdmin connected to Portainer PostgreSQL
-- Database: balisedb
-- User: adminbdb
-- =====================================================

-- Enable PostGIS extension for geographic data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (clean slate)
DROP TABLE IF EXISTS balise_events CASCADE;
DROP TABLE IF EXISTS containers CASCADE;
DROP TABLE IF EXISTS balises CASCADE;

-- Create the main balises table
CREATE TABLE IF NOT EXISTS balises (
    device_id VARCHAR(50) PRIMARY KEY,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW()
);

-- Create the events table with device_id column
CREATE TABLE IF NOT EXISTS balise_events (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL REFERENCES balises(device_id),
    event_time TIMESTAMP DEFAULT NOW(),
    event_type VARCHAR(50),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    battery_level INTEGER,
    raw_data TEXT,
    processed BOOLEAN DEFAULT FALSE
);

-- Create containers table for CMA-CGM integration
CREATE TABLE IF NOT EXISTS containers (
    id SERIAL PRIMARY KEY,
    container_id VARCHAR(50) UNIQUE NOT NULL,
    shipping_line VARCHAR(100),
    destination VARCHAR(100),
    contents VARCHAR(255),
    device_id VARCHAR(50) REFERENCES balises(device_id),
    assigned_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_balise_events_device_id ON balise_events(device_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_type ON balise_events(event_type);
CREATE INDEX IF NOT EXISTS idx_balises_last_seen ON balises(last_seen);

-- Verify table creation
SELECT 'balises' as table_name, COUNT(*) as row_count FROM balises
UNION ALL
SELECT 'balise_events' as table_name, COUNT(*) as row_count FROM balise_events
UNION ALL
SELECT 'containers' as table_name, COUNT(*) as row_count FROM containers;

-- Show table structures
\d balises;
\d balise_events;
\d containers;

-- Success message
SELECT 'Database setup completed successfully! Tables created with proper foreign key constraints.' as status;
