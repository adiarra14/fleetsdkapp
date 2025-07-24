#!/bin/bash
set -e

# This script runs on container startup to:
# 1. Set up MD5 authentication
# 2. Ensure tables are created

# Wait for PostgreSQL to be ready
until pg_isready -U adminbdb -d balisedb; do
  echo "Waiting for PostgreSQL to start..."
  sleep 2
done

# Copy MD5 authentication configuration
echo "Configuring MD5 authentication..."
if [ -f "/tmp/pg_hba.conf.md5" ]; then
  cat /tmp/pg_hba.conf.md5 > /var/lib/postgresql/data/pg_hba.conf
  # Reload configuration
  pg_ctl reload -D /var/lib/postgresql/data
  echo "PostgreSQL authentication updated to MD5"
else
  echo "WARNING: MD5 configuration file not found"
fi

# Ensure tables are created
echo "Creating database tables..."
if [ -f "/docker-entrypoint-initdb.d/create-tcp-database.sql" ]; then
  psql -U adminbdb -d balisedb -f /docker-entrypoint-initdb.d/create-tcp-database.sql
  echo "Database tables created successfully"
else
  echo "WARNING: Table creation script not found"
fi

echo "PostgreSQL initialization complete"
