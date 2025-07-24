-- Create balises table
CREATE TABLE IF NOT EXISTS balises (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100),
    status VARCHAR(50),
    battery_level INTEGER,
    signal_strength INTEGER,
    last_seen TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    container_id INTEGER,
    locked BOOLEAN,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create balise_events table
CREATE TABLE IF NOT EXISTS balise_events (
    id SERIAL PRIMARY KEY,
    balise_id INTEGER NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    raw_data TEXT,
    parsed_data JSONB,
    CONSTRAINT fk_balise
        FOREIGN KEY(balise_id)
        REFERENCES balises(id)
);

-- Create containers table
CREATE TABLE IF NOT EXISTS containers (
    id SERIAL PRIMARY KEY,
    container_number VARCHAR(50) UNIQUE,
    shipping_line VARCHAR(100),
    booking_reference VARCHAR(100),
    container_type VARCHAR(50),
    origin VARCHAR(100),
    destination VARCHAR(100),
    status VARCHAR(50),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create assets table
CREATE TABLE IF NOT EXISTS assets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    status VARCHAR(50),
    customer VARCHAR(100),
    notes TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
