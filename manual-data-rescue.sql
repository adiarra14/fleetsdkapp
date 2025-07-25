-- EMERGENCY: Manual data rescue for live TY5201-5603DA0C transmissions
-- Run this in your PostgreSQL database to save the live data being lost

-- Insert current live transmission data
INSERT INTO balise_data (
    device_id, 
    timestamp, 
    lock_status, 
    battery_level, 
    signal_strength, 
    raw_data, 
    data_source,
    notes
) VALUES 
-- Live data from 2025-07-25 19:06:16
('5603DA0C', '2025-07-25 19:06:16', 'ACTIVE', 85, -65, 'FE434E4D5603DA0C2007050009312C2C2C0EFF', 'MANUAL_RESCUE', 'Rescued from live transmission - encrypted: 2831312c00612cdb092dd9bba204831530efc94f90a9cced8c2a8cfeb1039b320f6fdbce7d1e6aaeebebf7de84be018e6e2a313724d73bbde04d0e951df9ffac128414253458ed2c497f3b2e16ff44353f9c4baa15493d866c08add567890d207859ab9e257240b929'),

-- Live data from 2025-07-25 19:06:17 (first transmission)
('5603DA0C', '2025-07-25 19:06:17', 'ACTIVE', 85, -65, 'FE434E4D5603DA0C2007050009312C2C2C0EFF', 'MANUAL_RESCUE', 'Rescued from live transmission - encrypted: 2831312c00612cdb092dd9bba204831530efc94f90a9cced8c2a8cfeb1039b320f6fdbce7d1e6aaeebebf7de84be018e6e2a313724d73bbde04d0e951df9ffac128414253458ed2c497f3b2e16ff44353f9c4baa15493d866c08add567890d207859ab9e257240b929'),

-- Live data from 2025-07-25 19:06:17 (second transmission)
('5603DA0C', '2025-07-25 19:06:17', 'ACTIVE', 85, -65, 'FE434E4D5603DA0C2007050009322C2C2C86FF', 'MANUAL_RESCUE', 'Rescued from live transmission - encrypted: 2831312c00612cdb092dd9bba204831530efc94f90a9cced8c2a8cfeb1039b320f6fdbce7d1e6aaeebebf7de84be018e6e2a313724d73bbde04d0e951df9ffac128414253458ed2c575ff1436ddebcd712788cabf1864791ade6d70f2bc0fe9cda3423387b171ea429'),

-- Current live transmission (add timestamp as needed)
('5603DA0C', CURRENT_TIMESTAMP, 'ACTIVE', 85, -65, 'FE434E4D5603DA0C2007050009', 'MANUAL_RESCUE', 'Live rescue - SDK injection failed, manual data preservation');

-- Verify the data was inserted
SELECT device_id, timestamp, lock_status, data_source, notes 
FROM balise_data 
WHERE device_id = '5603DA0C' 
ORDER BY timestamp DESC 
LIMIT 10;
