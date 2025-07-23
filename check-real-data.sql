-- Database verification script - NO TEST DATA
-- Only checks existing real balise data and database structure

-- Check database connection
SELECT 'Database connection successful' as status, current_timestamp, current_database();

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

-- Count REAL data only (no test entries)
SELECT 
    'Real balises' as table_name, 
    COUNT(*) as count 
FROM balises 
WHERE imei NOT LIKE 'TEST%'
UNION ALL
SELECT 
    'Real events' as table_name, 
    COUNT(*) as count 
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
WHERE b.imei NOT LIKE 'TEST%';

-- Show recent real balise events (if any)
SELECT 
    b.name as balise_name,
    b.imei,
    be.event_type,
    be.event_time,
    LEFT(be.message_raw, 100) as message_preview
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
WHERE b.imei NOT LIKE 'TEST%'
ORDER BY be.event_time DESC
LIMIT 10;

-- Check for any data at all
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN 'NO DATA - Waiting for real balise transmissions'
        ELSE CONCAT(COUNT(*), ' real balise events found')
    END as data_status
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
WHERE b.imei NOT LIKE 'TEST%';
