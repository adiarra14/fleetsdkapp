@echo off
echo Checking balise data in PostgreSQL database...

echo.
echo Listing database tables:
docker exec -it balise-postgres psql -U adminbdb -d balisedb -c "SELECT table_name FROM information_schema.tables WHERE table_schema='public';"

echo.
echo Checking balises table:
docker exec -it balise-postgres psql -U adminbdb -d balisedb -c "SELECT * FROM balises LIMIT 10;"

echo.
echo Checking recent balise events:
docker exec -it balise-postgres psql -U adminbdb -d balisedb -c "SELECT device_id, event_time, event_type, latitude, longitude, battery_level FROM balise_events ORDER BY event_time DESC LIMIT 10;"

echo.
echo Counting events in the last hour:
docker exec -it balise-postgres psql -U adminbdb -d balisedb -c "SELECT COUNT(*) FROM balise_events WHERE event_time > NOW() - INTERVAL '1 hour';"

echo.
echo Counting events by device:
docker exec -it balise-postgres psql -U adminbdb -d balisedb -c "SELECT device_id, COUNT(*) as event_count FROM balise_events GROUP BY device_id ORDER BY event_count DESC;"
