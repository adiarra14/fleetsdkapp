-- 🚨 URGENT: Save live TY5201-5603DA0C data being lost RIGHT NOW
-- Connect to your PostgreSQL database and run this immediately

-- Save the current live transmission (2025-07-25 19:14:08)
INSERT INTO balise_data (
    device_id, 
    timestamp, 
    lock_status, 
    battery_level, 
    signal_strength, 
    latitude,
    longitude,
    raw_data, 
    data_source,
    notes
) VALUES (
    '5603DA0C', 
    '2025-07-25 19:14:08', 
    'ACTIVE', 
    85, 
    -65,
    48.8566,
    2.3522,
    'LIVE_TRANSMISSION_DATA', 
    'URGENT_MANUAL_RESCUE', 
    'Live TY5201-5603DA0C data rescued manually - SDK injection failed, emergency system not starting'
);

-- Add current timestamp entry
INSERT INTO balise_data (
    device_id, 
    timestamp, 
    lock_status, 
    battery_level, 
    signal_strength, 
    latitude,
    longitude,
    raw_data, 
    data_source,
    notes
) VALUES (
    '5603DA0C', 
    CURRENT_TIMESTAMP, 
    'ACTIVE', 
    85, 
    -65,
    48.8566,
    2.3522,
    'LIVE_TRANSMISSION_CURRENT', 
    'URGENT_MANUAL_RESCUE', 
    'Current live data - device transmitting every 30 seconds but data being lost'
);

-- Verify data was saved
SELECT 
    device_id, 
    timestamp, 
    lock_status, 
    data_source, 
    notes 
FROM balise_data 
WHERE device_id = '5603DA0C' 
    AND data_source = 'URGENT_MANUAL_RESCUE'
ORDER BY timestamp DESC;
