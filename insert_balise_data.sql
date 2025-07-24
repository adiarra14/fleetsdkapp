-- Manual insertion of the decoded balise data
-- Run this when NullPointerException occurs to store the data

-- First, ensure the device exists
INSERT INTO balises (device_id, name, status, battery_level, last_seen, created_at, updated_at)
VALUES (
    'TY5201-5603DA0C',
    'TY5201-LOCK-MAIN V1.2',
    'ONLINE',
    48,
    '2025-07-24 23:00:37',
    NOW(),
    NOW()
)
ON CONFLICT (device_id) DO UPDATE SET
    status = EXCLUDED.status,
    battery_level = EXCLUDED.battery_level,
    last_seen = EXCLUDED.last_seen,
    updated_at = NOW();

-- Insert the event data
INSERT INTO balise_events (
    balise_id,
    event_type,
    event_data,
    raw_data,
    created_at
)
VALUES (
    (SELECT id FROM balises WHERE device_id = 'TY5201-5603DA0C'),
    'STATUS_REPORT',
    '{
        "deviceId": "TY5201-5603DA0C",
        "messageType": "STATUS_REPORT",
        "timestamp": "2025-07-24T23:00:37Z",
        "batteryLevel": 48,
        "deviceInfo": {
            "model": "TY5201-LOCK-MAIN",
            "firmwareVersion": "V1.2",
            "buildDate": "20250304",
            "softwareVersion": "V3.0.4_Alpha"
        },
        "status": "ONLINE",
        "alarms": [],
        "rawData": "FE434E4D5603DA0C200701004F907715...",
        "source": "manual_insertion"
    }',
    'FE434E4D5603DA0C200701004F90771530000000000000000000000800000000000000005A4037000A2B5459353230312D4C4F434B2D4D41494E5F56312E325F32303235303330345F56332E302E345F416C7068610000ECFF',
    '2025-07-24 23:00:37'
);

-- Verify the insertion
SELECT 
    b.device_id,
    b.name,
    b.status,
    b.battery_level,
    b.last_seen,
    be.event_type,
    be.event_data,
    be.created_at
FROM balises b
JOIN balise_events be ON b.id = be.balise_id
WHERE b.device_id = 'TY5201-5603DA0C'
ORDER BY be.created_at DESC
LIMIT 1;
