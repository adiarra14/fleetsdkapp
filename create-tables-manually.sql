-- Manual Database Setup Script for Balise Management System
-- Run this script in pgAdmin or via psql to create the required tables

-- Connect to the balisedb database first, then run this script

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (to ensure clean setup)
DROP TABLE IF EXISTS balise_events CASCADE;
DROP TABLE IF EXISTS assets CASCADE;
DROP TABLE IF EXISTS balises CASCADE;
DROP TABLE IF EXISTS containers CASCADE;

-- Create balises table
CREATE TABLE balises (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    imei VARCHAR(255) UNIQUE NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    last_seen TIMESTAMP WITH TIME ZONE,
    battery_level DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326),
    container_id INTEGER
);

-- Create balise_events table
CREATE TABLE balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326),
    battery_level DECIMAL(5,2),
    speed DECIMAL(10,2),
    heading DECIMAL(10,2),
    message_raw TEXT,
    payload JSONB
);

-- Create containers table
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326)
);

-- Create assets table
CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    container_id INTEGER REFERENCES containers(id),
    balise_id INTEGER REFERENCES balises(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_balises_imei ON balises(imei);
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX idx_balise_events_event_type ON balise_events(event_type);
CREATE INDEX idx_balise_events_event_time ON balise_events(event_time);

-- Grant permissions to the database user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adminbdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adminbdb;

-- Verify tables were created
SELECT table_name, table_schema 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Show PostGIS extension status
SELECT name, default_version, installed_version 
FROM pg_available_extensions 
WHERE name = 'postgis';

-- Success message
DO $$
BEGIN
    RAISE NOTICE '=== DATABASE SETUP COMPLETE ===';
    RAISE NOTICE 'Tables created: balises, balise_events, containers, assets';
    RAISE NOTICE 'PostGIS extension enabled for location data';
    RAISE NOTICE 'Indexes created for performance';
    RAISE NOTICE 'Permissions granted to adminbdb user';
    RAISE NOTICE 'Ready for balise data processing!';
END $$;
