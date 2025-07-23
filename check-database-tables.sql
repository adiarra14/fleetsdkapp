-- Quick database verification script
-- Run this to check if tables exist and are accessible

-- Check database connection
SELECT 'Database connection successful!' as status;

-- List all tables
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Check balises table structure
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'public' AND table_name = 'balises'
ORDER BY ordinal_position;

-- Check balise_events table structure  
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'public' AND table_name = 'balise_events'
ORDER BY ordinal_position;

-- Count existing data
SELECT 'balises' as table_name, COUNT(*) as row_count FROM balises
UNION ALL
SELECT 'balise_events' as table_name, COUNT(*) as row_count FROM balise_events;

-- Test insert capability (this will be rolled back)
BEGIN;
INSERT INTO balises (name, imei, type, status) VALUES ('TEST', 'TEST123', 'TEST', 'TEST');
SELECT 'Insert test successful' as test_result;
ROLLBACK;
