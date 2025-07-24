-- Database creation script for TCP server communication
-- This creates the exact tables needed for the Maxvision SDK TCP server

-- Note: PostGIS extension not available in container, using standard PostgreSQL types
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- Main balises table - stores device information
CREATE TABLE IF NOT EXISTS balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    serial_number VARCHAR(255),
    imei VARCHAR(255),
    model VARCHAR(50),
    type VARCHAR(50),
    firmware_version VARCHAR(50),
    status VARCHAR(50),
    last_ip VARCHAR(45),
    last_seen TIMESTAMP WITH TIME ZONE,
    battery_level DECIMAL(5,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    container_id INTEGER
);

-- Balise events table - stores all TCP server data transmissions
CREATE TABLE IF NOT EXISTS balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    battery_level DECIMAL(5,2),
    speed DECIMAL(10,2),
    heading DECIMAL(10,2),
    message_raw TEXT,
    payload JSONB
);

-- Containers table for asset management
CREATE TABLE IF NOT EXISTS containers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);

-- Assets table for linking containers and balises
CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    container_id INTEGER,
    balise_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes for TCP server queries
CREATE INDEX IF NOT EXISTS idx_balises_imei ON balises(imei);
CREATE INDEX IF NOT EXISTS idx_balises_latitude ON balises(latitude);
CREATE INDEX IF NOT EXISTS idx_balises_longitude ON balises(longitude);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_latitude ON balise_events(latitude);
CREATE INDEX IF NOT EXISTS idx_balise_events_longitude ON balise_events(longitude);

-- Set proper ownership for TCP server database user
ALTER TABLE balises OWNER TO adminbdb;
ALTER TABLE balise_events OWNER TO adminbdb;
ALTER TABLE containers OWNER TO adminbdb;
ALTER TABLE assets OWNER TO adminbdb;

-- Verify tables were created successfully
SELECT 'Database tables created successfully for TCP server communication' as status;
