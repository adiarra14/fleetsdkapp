# PostgreSQL connection parameters
$PGHOST = "localhost"
$PGPORT = "6063"  # Port mapped to PostgreSQL container
$PGUSER = "adminbdb"
$PGPASSWORD = "To7Z2UCeWTsriPxbADX8"
$PGDATABASE = "balisedb"

# Function to run a PostgreSQL query
function Run-PgQuery {
    param (
        [string]$query
    )
    
    # Use environment variables for connection parameters
    $env:PGPASSWORD = $PGPASSWORD
    
    # Run the query using psql
    $result = psql -h $PGHOST -p $PGPORT -U $PGUSER -d $PGDATABASE -c "$query"
    
    return $result
}

# Check if tables exist
Write-Host "Checking database tables..."
$tableQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"
Run-PgQuery -query $tableQuery

# Check balises table
Write-Host "`nChecking balise devices..."
$balisesQuery = "SELECT * FROM balises LIMIT 10;"
Run-PgQuery -query $balisesQuery

# Check balise_events table (most recent events)
Write-Host "`nChecking recent balise events..."
$eventsQuery = "SELECT device_id, event_time, event_type, latitude, longitude, battery_level FROM balise_events ORDER BY event_time DESC LIMIT 10;"
Run-PgQuery -query $eventsQuery

# Count events in the last hour
Write-Host "`nCounting events in the last hour..."
$countQuery = "SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '1 hour';"
Run-PgQuery -query $countQuery

# Count events by device
Write-Host "`nCounting events by device..."
$deviceCountQuery = "SELECT device_id, COUNT(*) as event_count FROM balise_events GROUP BY device_id ORDER BY event_count DESC;"
Run-PgQuery -query $deviceCountQuery
