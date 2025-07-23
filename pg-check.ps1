# PowerShell script to check PostgreSQL database via Docker

# Function to run a query in the PostgreSQL container
function Query-Postgres {
    param (
        [string]$query,
        [string]$description = ""
    )
    
    if ($description) {
        Write-Host "`n==== $description ====" -ForegroundColor Cyan
    }
    
    try {
        # Use docker exec to run psql inside the container
        $result = docker exec balise-postgres psql -U adminbdb -d balisedb -c $query
        $result | Out-Host
        return $result
    }
    catch {
        Write-Host "Error executing query: $_" -ForegroundColor Red
    }
}

# Check database tables
Query-Postgres -query "SELECT table_name FROM information_schema.tables WHERE table_schema='public';" -description "Database Tables"

# Check balises table structure
Query-Postgres -query "\d balises" -description "Balises Table Structure"

# Check balise_events table structure
Query-Postgres -query "\d balise_events" -description "Balise Events Table Structure"

# Check for balises
Query-Postgres -query "SELECT * FROM balises LIMIT 10;" -description "Balise Devices"

# Check for recent events
Query-Postgres -query "SELECT device_id, event_time, event_type, latitude, longitude, battery_level FROM balise_events ORDER BY event_time DESC LIMIT 10;" -description "Recent Balise Events"

# Count events in the last hour
Query-Postgres -query "SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '1 hour';" -description "Events in the Last Hour"

# Count events by device
Query-Postgres -query "SELECT device_id, COUNT(*) as event_count FROM balise_events GROUP BY device_id ORDER BY event_count DESC;" -description "Events by Device"

# Check if the tables exist but are empty
Query-Postgres -query "SELECT 
(SELECT COUNT(*) FROM balises) AS balise_count,
(SELECT COUNT(*) FROM balise_events) AS event_count;" -description "Record Counts"
