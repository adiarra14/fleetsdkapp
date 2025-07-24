#!/bin/bash
set -e

# This script runs on container startup to:
# 1. Set up MD5 authentication
# 2. Ensure tables are created

# Wait for PostgreSQL to be ready
until pg_isready -U adminbdb -d balisedb; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done
echo "PostgreSQL is ready, proceeding with initialization"

# Apply MD5 authentication
echo "Applying MD5 authentication configuration"
cp /tmp/pg_hba.conf.md5 /var/lib/postgresql/data/pg_hba.conf
pg_ctl reload

# Create database tables
echo "Creating database tables"
psql -U adminbdb -d balisedb << EOF
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

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_balise_device_id ON balises(device_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_balise_id ON balise_events(balise_id);
CREATE INDEX IF NOT EXISTS idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX IF NOT EXISTS idx_balise_events_latitude ON balise_events(latitude);
CREATE INDEX IF NOT EXISTS idx_balise_events_longitude ON balise_events(longitude);
CREATE INDEX IF NOT EXISTS idx_containers_container_number ON containers(container_number);

-- Set proper ownership for TCP server database user
ALTER TABLE balises OWNER TO adminbdb;
ALTER TABLE balise_events OWNER TO adminbdb;
ALTER TABLE containers OWNER TO adminbdb;
ALTER TABLE assets OWNER TO adminbdb;

-- Grant necessary permissions for TCP server data transmission
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adminbdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adminbdb;
EOF

echo "Database tables creation complete"
echo "PostgreSQL initialization complete"
