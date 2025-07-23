-- Manual test script to insert balise data
-- Run this to verify database connectivity and table structure

-- First, create a test balise if it doesn't exist
INSERT INTO balises (name, imei, type, status, created_at) 
VALUES ('TEST-BALISE-001', 'TEST123456789', 'TY5201-LOCK', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (imei) DO NOTHING;

-- Get the balise ID for our test
-- You can run this to see the ID: SELECT id FROM balises WHERE imei = 'TEST123456789';

-- Insert test event data (replace balise_id with actual ID from above query)
INSERT INTO balise_events (
    balise_id, 
    event_type, 
    event_time, 
    message_raw, 
    payload
) VALUES (
    (SELECT id FROM balises WHERE imei = 'TEST123456789'),
    'MANUAL_TEST',
    CURRENT_TIMESTAMP,
    '{"deviceId":"TEST123456789","messageType":"GPS","latitude":48.8566,"longitude":2.3522}',
    '{"test": true, "source": "manual", "timestamp": "2025-07-23T20:54:00Z"}'::jsonb
);

-- Verify the insert worked
SELECT 
    b.name as balise_name,
    b.imei,
    be.event_type,
    be.event_time,
    be.message_raw,
    be.payload
FROM balise_events be
JOIN balises b ON be.balise_id = b.id
WHERE b.imei = 'TEST123456789'
ORDER BY be.event_time DESC
LIMIT 5;

-- Check total count
SELECT 
    'Total balises' as table_name, 
    COUNT(*) as count 
FROM balises
UNION ALL
SELECT 
    'Total events' as table_name, 
    COUNT(*) as count 
FROM balise_events;
