-- Database initialization for Maxvision Lock SDK
-- Creates tables for balises and events

-- Create balises table
CREATE TABLE IF NOT EXISTS balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) UNIQUE NOT NULL,
    device_type VARCHAR(50) DEFAULT 'TY5201-LOCK',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Create balise_events table
CREATE TABLE IF NOT EXISTS balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER REFERENCES balises(id),
    event_type VARCHAR(50) NOT NULL,
    event_data TEXT,
    raw_json TEXT,
    data_source VARCHAR(50) DEFAULT 'MAXVISION_SDK',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_balises_device_id ON balises(device_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_created_at ON balise_events(created_at);

-- Insert initial balise for TY5201-5603DA0C
INSERT INTO balises (device_id, device_type, status, notes) 
VALUES ('5603DA0C', 'TY5201-LOCK', 'ACTIVE', 'Initial balise for live data capture')
ON CONFLICT (device_id) DO NOTHING;
