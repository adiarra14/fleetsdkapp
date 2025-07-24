#!/bin/bash

# This script fixes the PostgreSQL authentication issue by updating the pg_hba.conf file
# to use md5 authentication instead of scram-sha-256

echo "Fixing PostgreSQL authentication issue..."

# Copy the pg_hba.conf template to the container
docker exec -it balise-postgres bash -c "echo 'host all all all md5' > /tmp/pg_hba_update.conf"

# Append the line to the pg_hba.conf file
docker exec -it balise-postgres bash -c "cat /tmp/pg_hba_update.conf >> /var/lib/postgresql/data/pg_hba.conf"

# Reload PostgreSQL configuration
docker exec -it balise-postgres bash -c "pg_ctl reload -D /var/lib/postgresql/data"

echo "PostgreSQL authentication method updated to md5. Restarting PostgreSQL..."

# Restart the PostgreSQL container
docker restart balise-postgres

echo "Done! PostgreSQL authentication method has been updated to use md5 instead of scram-sha-256."
echo "The TCP server should now be able to connect to the database."
