-- Ensure tables are created with the correct structure
CREATE TABLE IF NOT EXISTS balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100),
    status VARCHAR(20),
    last_seen TIMESTAMP,
    battery_level INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
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

-- Insert some sample balises to ensure the database has data
INSERT INTO balises (device_id, name, status, battery_level)
VALUES 
('BALISE001', 'Test Balise 1', 'ACTIVE', 95),
('BALISE002', 'Test Balise 2', 'ACTIVE', 87)
ON CONFLICT (device_id) DO NOTHING;
