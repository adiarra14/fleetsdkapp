-- =====================================================
-- MAXVISION SDK BALISE MANAGEMENT DATABASE SETUP
-- Complete production-ready database script
-- Compatible with PostgreSQL + PostGIS
-- =====================================================

-- Enable PostGIS extension for geographic data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Drop existing tables if they exist (clean slate)
DROP TABLE IF EXISTS balise_events CASCADE;
DROP TABLE IF EXISTS containers CASCADE;
DROP TABLE IF EXISTS balises CASCADE;

-- =====================================================
-- MAIN BALISES TABLE
-- Stores all balise devices in the system
-- =====================================================
CREATE TABLE balises (
    device_id VARCHAR(50) PRIMARY KEY,
    device_name VARCHAR(100),
    device_type VARCHAR(50) DEFAULT 'TY5201-LOCK',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW(),
    last_ip_address INET,
    firmware_version VARCHAR(20),
    battery_level INTEGER,
    location GEOMETRY(POINT, 4326), -- PostGIS point for GPS coordinates
    notes TEXT
);

-- =====================================================
-- BALISE EVENTS TABLE
-- Stores all events/messages from balise devices
-- =====================================================
CREATE TABLE balise_events (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL REFERENCES balises(device_id) ON DELETE CASCADE,
    event_time TIMESTAMP DEFAULT NOW(),
    event_type VARCHAR(50) NOT NULL,
    message_type VARCHAR(50),
    command_type VARCHAR(50),
    sub_command_type VARCHAR(50),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    altitude DECIMAL(8, 2),
    speed DECIMAL(6, 2),
    heading DECIMAL(5, 2),
    battery_level INTEGER,
    signal_strength INTEGER,
    temperature DECIMAL(5, 2),
    humidity DECIMAL(5, 2),
    lock_status VARCHAR(20),
    seal_status VARCHAR(20),
    tamper_alert BOOLEAN DEFAULT FALSE,
    emergency_alert BOOLEAN DEFAULT FALSE,
    raw_data TEXT,
    parsed_data JSONB,
    processed BOOLEAN DEFAULT FALSE,
    error_code VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- CONTAINERS TABLE
-- For CMA-CGM and other shipping line integrations
-- =====================================================
CREATE TABLE containers (
    id SERIAL PRIMARY KEY,
    container_id VARCHAR(50) UNIQUE NOT NULL,
    container_type VARCHAR(20),
    shipping_line VARCHAR(100) DEFAULT 'CMA-CGM',
    origin VARCHAR(100),
    destination VARCHAR(100),
    contents VARCHAR(255),
    weight_kg DECIMAL(10, 2),
    value_usd DECIMAL(12, 2),
    device_id VARCHAR(50) REFERENCES balises(device_id) ON DELETE SET NULL,
    assigned_at TIMESTAMP DEFAULT NOW(),
    unassigned_at TIMESTAMP,
    voyage_number VARCHAR(50),
    booking_reference VARCHAR(50),
    seal_number VARCHAR(50),
    status VARCHAR(20) DEFAULT 'IN_TRANSIT',
    priority VARCHAR(10) DEFAULT 'NORMAL',
    special_instructions TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- USERS TABLE (for web/mobile app access)
-- =====================================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'OPERATOR',
    company VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- SYSTEM LOGS TABLE
-- =====================================================
CREATE TABLE system_logs (
    id SERIAL PRIMARY KEY,
    log_level VARCHAR(10) NOT NULL,
    component VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    details JSONB,
    user_id INTEGER REFERENCES users(id),
    device_id VARCHAR(50) REFERENCES balises(device_id),
    ip_address INET,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- INDEXES for Performance
-- =====================================================

-- Balises indexes
CREATE INDEX idx_balises_status ON balises(status);
CREATE INDEX idx_balises_last_seen ON balises(last_seen);
CREATE INDEX idx_balises_device_type ON balises(device_type);
CREATE INDEX idx_balises_location ON balises USING GIST(location);

-- Balise events indexes
CREATE INDEX idx_balise_events_device_id ON balise_events(device_id);
CREATE INDEX idx_balise_events_event_time ON balise_events(event_time);
CREATE INDEX idx_balise_events_event_type ON balise_events(event_type);
CREATE INDEX idx_balise_events_message_type ON balise_events(message_type);
CREATE INDEX idx_balise_events_processed ON balise_events(processed);
CREATE INDEX idx_balise_events_emergency ON balise_events(emergency_alert) WHERE emergency_alert = TRUE;
CREATE INDEX idx_balise_events_tamper ON balise_events(tamper_alert) WHERE tamper_alert = TRUE;

-- Containers indexes
CREATE INDEX idx_containers_device_id ON containers(device_id);
CREATE INDEX idx_containers_shipping_line ON containers(shipping_line);
CREATE INDEX idx_containers_status ON containers(status);
CREATE INDEX idx_containers_assigned_at ON containers(assigned_at);

-- Users indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);

-- System logs indexes
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_log_level ON system_logs(log_level);
CREATE INDEX idx_system_logs_component ON system_logs(component);

-- =====================================================
-- TRIGGERS for automatic timestamp updates
-- =====================================================

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers
CREATE TRIGGER update_balises_updated_at BEFORE UPDATE ON balises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_containers_updated_at BEFORE UPDATE ON containers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- VIEWS for common queries
-- =====================================================

-- Active balises with latest event
CREATE VIEW active_balises_with_latest_event AS
SELECT 
    b.device_id,
    b.device_name,
    b.device_type,
    b.status,
    b.last_seen,
    b.battery_level,
    ST_Y(b.location) as latitude,
    ST_X(b.location) as longitude,
    be.event_type as last_event_type,
    be.event_time as last_event_time,
    c.container_id,
    c.shipping_line,
    c.destination
FROM balises b
LEFT JOIN containers c ON b.device_id = c.device_id
LEFT JOIN LATERAL (
    SELECT event_type, event_time 
    FROM balise_events 
    WHERE device_id = b.device_id 
    ORDER BY event_time DESC 
    LIMIT 1
) be ON TRUE
WHERE b.status = 'ACTIVE';

-- Emergency alerts view
CREATE VIEW emergency_alerts AS
SELECT 
    be.id,
    be.device_id,
    b.device_name,
    be.event_time,
    be.event_type,
    be.latitude,
    be.longitude,
    be.emergency_alert,
    be.tamper_alert,
    be.raw_data,
    c.container_id,
    c.shipping_line
FROM balise_events be
JOIN balises b ON be.device_id = b.device_id
LEFT JOIN containers c ON b.device_id = c.device_id
WHERE be.emergency_alert = TRUE OR be.tamper_alert = TRUE
ORDER BY be.event_time DESC;

-- =====================================================
-- SAMPLE DATA (Optional - remove for production)
-- =====================================================

-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password_hash, role, company) VALUES
('admin', 'admin@maxvision.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye', 'ADMIN', 'Maxvision Fleet Management');

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Verify table creation
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('balises', 'balise_events', 'containers', 'users', 'system_logs')
ORDER BY tablename;

-- Verify indexes
SELECT 
    indexname,
    tablename,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'public' 
AND tablename IN ('balises', 'balise_events', 'containers', 'users', 'system_logs')
ORDER BY tablename, indexname;

-- Verify PostGIS extension
SELECT name, default_version, installed_version 
FROM pg_available_extensions 
WHERE name = 'postgis';

-- Show table row counts
SELECT 'balises' as table_name, COUNT(*) as row_count FROM balises
UNION ALL
SELECT 'balise_events' as table_name, COUNT(*) as row_count FROM balise_events
UNION ALL
SELECT 'containers' as table_name, COUNT(*) as row_count FROM containers
UNION ALL
SELECT 'users' as table_name, COUNT(*) as row_count FROM users
UNION ALL
SELECT 'system_logs' as table_name, COUNT(*) as row_count FROM system_logs;

-- Success message
SELECT 'Maxvision SDK Database setup completed successfully!' as status,
       'All tables, indexes, triggers, and views created.' as details,
       NOW() as completed_at;
