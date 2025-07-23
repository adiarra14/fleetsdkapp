-- Database creation script for TCP server communication
-- This creates the exact tables needed for the Maxvision SDK TCP server

-- Enable PostGIS extension for geographic data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Main balises table - stores device information
CREATE TABLE IF NOT EXISTS balises (
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

-- Balise events table - stores all TCP server data transmissions
CREATE TABLE IF NOT EXISTS balise_events (
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

-- Containers table for asset management
CREATE TABLE IF NOT EXISTS containers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326)
);

-- Assets table for linking containers and balises
CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    container_id INTEGER REFERENCES containers(id),
    balise_id INTEGER REFERENCES balises(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance indexes for TCP server queries
CREATE INDEX IF NOT EXISTS idx_balises_imei ON balises(imei);
CREATE INDEX IF NOT EXISTS idx_balises_location ON balises USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_location ON balise_events USING GIST(location);

-- Set proper ownership for TCP server database user
ALTER TABLE balises OWNER TO adminbdb;
ALTER TABLE balise_events OWNER TO adminbdb;
ALTER TABLE containers OWNER TO adminbdb;
ALTER TABLE assets OWNER TO adminbdb;

-- Verify tables were created successfully
SELECT 'Database tables created successfully for TCP server communication' as status;
