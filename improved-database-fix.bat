@echo off
echo Fixing PostgreSQL authentication issue...

REM Create a simple SQL script to modify authentication method
echo CREATE OR REPLACE FUNCTION pg_temp.set_auth_md5() RETURNS void AS $$ > auth_fix.sql
echo BEGIN >> auth_fix.sql
echo     EXECUTE 'ALTER SYSTEM SET password_encryption = ''md5'''; >> auth_fix.sql
echo     PERFORM pg_reload_conf(); >> auth_fix.sql
echo END; >> auth_fix.sql
echo $$ LANGUAGE plpgsql; >> auth_fix.sql
echo SELECT pg_temp.set_auth_md5(); >> auth_fix.sql

REM Copy the SQL script to the container
docker cp auth_fix.sql balise-postgres:/tmp/

REM Run the SQL script as postgres user
docker exec -it balise-postgres psql -U adminbdb -d balisedb -f /tmp/auth_fix.sql

REM Add an MD5 entry to pg_hba.conf using postgres user
docker exec -it balise-postgres bash -c "echo 'host all all all md5' >> /var/lib/postgresql/data/pg_hba.conf"

REM Restart PostgreSQL container to apply changes
docker restart balise-postgres

echo Waiting for PostgreSQL to restart...
timeout /t 5

echo Done! PostgreSQL authentication should now accept MD5 password authentication.
echo The TCP server should now be able to connect to the database.

REM Clean up temporary file
del auth_fix.sql

pause
