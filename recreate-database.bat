@echo off
echo Recreating the balise database...

echo Connecting to PostgreSQL container...
docker exec -it balise-postgres psql -U adminbdb -c "DROP DATABASE IF EXISTS balisedb;"
docker exec -it balise-postgres psql -U adminbdb -c "CREATE DATABASE balisedb;"
echo Database recreated successfully!

echo Running schema creation script...
docker cp h:\ynnov\CVS\FleetApp\sdk\create-tcp-database.sql balise-postgres:/tmp/create-tcp-database.sql
docker exec -it balise-postgres psql -U adminbdb -d balisedb -f /tmp/create-tcp-database.sql
echo Schema applied successfully!

echo Done. Database is ready for balise transmissions.
