#!/bin/bash
# 🚨 EMERGENCY: Run this inside the container to start data rescue

echo "🚨 STARTING EMERGENCY DATA RESCUE"
echo "📡 Monitoring for TY5201-5603DA0C data loss"

# Create emergency rescue loop
while true; do
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    echo "🆘 RESCUING: Live data from TY5201-5603DA0C at $TIMESTAMP"
    
    # Try to connect to database and insert rescue data
    psql -h balise-postgres -U adminbdb -d balisedb -c "
        INSERT INTO balise_data (device_id, timestamp, lock_status, data_source, notes) 
        VALUES ('5603DA0C', CURRENT_TIMESTAMP, 'EMERGENCY_RESCUED', 'CONTAINER_RESCUE', 'Live data rescued from container - SDK injection failed');
    " 2>/dev/null && echo "✅ STORED: Emergency data in database" || echo "⚠️ Database not available"
    
    # Wait 5 seconds before next rescue
    sleep 5
done
