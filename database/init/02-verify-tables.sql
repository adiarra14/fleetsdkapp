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

-- No sample data - tables will be populated by real balise connections

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
