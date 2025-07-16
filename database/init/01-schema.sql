-- PostgreSQL initialization script with PostGIS extension
-- Creates the schema for the Balise Management System

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create tables
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

CREATE TABLE IF NOT EXISTS containers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326)
);

CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    container_id INTEGER REFERENCES containers(id),
    balise_id INTEGER REFERENCES balises(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_balises_imei ON balises(imei);
CREATE INDEX IF NOT EXISTS idx_balises_location ON balises USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_location ON balise_events USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_containers_location ON containers USING GIST(location);

-- Add some initial data (optional)
INSERT INTO containers (name, type, status, location) 
VALUES 
('Container A', 'Standard', 'Active', ST_GeogFromText('POINT(2.3488 48.8534)'))
ON CONFLICT DO NOTHING;

INSERT INTO containers (name, type, status, location) 
VALUES 
('Container B', 'Refrigerated', 'Active', ST_GeogFromText('POINT(2.2945 48.8582)'))
ON CONFLICT DO NOTHING;

-- Grant permissions
ALTER TABLE balises OWNER TO adminbdb;
ALTER TABLE balise_events OWNER TO adminbdb;
ALTER TABLE containers OWNER TO adminbdb;
ALTER TABLE assets OWNER TO adminbdb;
