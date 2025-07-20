@echo off
echo ===== Balise Database Setup =====
echo.
echo This script will create the required database tables for the Balise Management System.
echo.
echo Prerequisites:
echo - PostgreSQL container must be running (port 6063)
echo - Database 'balisedb' must exist
echo - User 'adminbdb' must have access
echo.
pause

echo Connecting to PostgreSQL and creating tables...
echo.

docker exec -i balise-postgres psql -U adminbdb -d balisedb < create-tables-manually.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===== SUCCESS =====
    echo Database tables created successfully!
    echo You can now connect with pgAdmin to see the tables:
    echo - balises
    echo - balise_events  
    echo - containers
    echo - assets
    echo.
) else (
    echo.
    echo ===== ERROR =====
    echo Failed to create database tables.
    echo Please check:
    echo 1. PostgreSQL container is running: docker ps
    echo 2. Database exists and is accessible
    echo 3. Run the SQL script manually in pgAdmin
    echo.
)

pause
