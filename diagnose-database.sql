-- Database Diagnostic Script
-- Run this to check if tables exist and database is properly initialized

-- Check if we can connect to the database
SELECT 'Database connection successful!' as status;

-- Check if PostGIS extension is installed
SELECT 'PostGIS extension status: ' || CASE WHEN EXISTS (
    SELECT 1 FROM pg_extension WHERE extname = 'postgis'
) THEN 'INSTALLED' ELSE 'NOT INSTALLED' END as postgis_status;

-- Check if balises table exists
SELECT 'balises table status: ' || CASE WHEN EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'balises'
) THEN 'EXISTS' ELSE 'MISSING' END as balises_table;

-- Check if balise_events table exists
SELECT 'balise_events table status: ' || CASE WHEN EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'balise_events'
) THEN 'EXISTS' ELSE 'MISSING' END as balise_events_table;

-- Check if containers table exists
SELECT 'containers table status: ' || CASE WHEN EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'containers'
) THEN 'EXISTS' ELSE 'MISSING' END as containers_table;

-- List all tables in the database
SELECT 'All tables in database:' as info;
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Check database name and user
SELECT current_database() as current_db, current_user as current_user;
