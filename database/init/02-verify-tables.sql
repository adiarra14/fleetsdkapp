-- Verification and debugging script for database initialization
-- This script will help verify that tables are created properly

-- Show current database and user
SELECT current_database(), current_user;

-- List all tables in the current database
SELECT table_name, table_schema 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Show PostGIS extension status
SELECT name, default_version, installed_version 
FROM pg_available_extensions 
WHERE name = 'postgis';

-- Create sample balise data for testing
INSERT INTO balises (name, imei, type, status, battery_level, location) VALUES
('BALISE001', '123456789012345', 'GPS_TRACKER', 'ACTIVE', 85.5, ST_GeogFromText('POINT(2.3522 48.8566)')),
('BALISE002', '123456789012346', 'GPS_TRACKER', 'ACTIVE', 92.3, ST_GeogFromText('POINT(2.2945 48.8584)')),
('BALISE003', '123456789012347', 'GPS_TRACKER', 'INACTIVE', 45.8, ST_GeogFromText('POINT(2.3488 48.8534)'))
ON CONFLICT (imei) DO NOTHING;

-- Create sample container data
INSERT INTO containers (name, type, status, location) VALUES
('CONTAINER001', 'SHIPPING', 'IN_TRANSIT', ST_GeogFromText('POINT(2.3522 48.8566)')),
('CONTAINER002', 'STORAGE', 'PARKED', ST_GeogFromText('POINT(2.2945 48.8584)'))
ON CONFLICT DO NOTHING;

-- Create sample events
INSERT INTO balise_events (balise_id, event_type, location, battery_level, speed, heading, payload) VALUES
(1, 'GPS_UPDATE', ST_GeogFromText('POINT(2.3522 48.8566)'), 85.5, 45.2, 180.0, '{"signal_strength": -75, "satellites": 8}'),
(2, 'GPS_UPDATE', ST_GeogFromText('POINT(2.2945 48.8584)'), 92.3, 0.0, 0.0, '{"signal_strength": -68, "satellites": 12}'),
(3, 'LOW_BATTERY', ST_GeogFromText('POINT(2.3488 48.8534)'), 45.8, 0.0, 0.0, '{"battery_voltage": 3.2, "charging": false}');

-- Show table counts for verification
SELECT 
    'balises' as table_name, COUNT(*) as record_count FROM balises
UNION ALL
SELECT 
    'balise_events' as table_name, COUNT(*) as record_count FROM balise_events
UNION ALL
SELECT 
    'containers' as table_name, COUNT(*) as record_count FROM containers
UNION ALL
SELECT 
    'assets' as table_name, COUNT(*) as record_count FROM assets;

-- Grant permissions to ensure access
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO adminbdb;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO adminbdb;

-- Output success message
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully!';
    RAISE NOTICE 'Tables created and sample data inserted.';
END $$;
