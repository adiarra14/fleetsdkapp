#!/bin/bash
set -e

# Start PostgreSQL in the background
docker-entrypoint.sh postgres &

# Store the PostgreSQL server process ID
PG_PID=$!

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
until pg_isready -U adminbdb -d balisedb; do
  sleep 2
done
echo "PostgreSQL is ready, proceeding with initialization"

# Run SQL files to create tables and set permissions
echo "Creating database tables and indexes..."
psql -U adminbdb -d balisedb -f /docker-entrypoint-initdb.d/01-create-tables.sql
psql -U adminbdb -d balisedb -f /docker-entrypoint-initdb.d/02-create-indexes.sql
psql -U adminbdb -d balisedb -f /docker-entrypoint-initdb.d/03-set-permissions.sql
echo "Database initialization completed successfully!"

# Keep the container running by waiting for the PostgreSQL process
echo "Initialization complete - PostgreSQL running"
wait $PG_PID
