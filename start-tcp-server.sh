#!/bin/bash

echo "===== Starting Maxvision Simple TCP Server v3.0 ====="

# Show directory contents for debugging
echo "==== Directory contents ====="
ls -la
echo "==== Lib directory contents ====="
ls -la lib 2>/dev/null || echo "No lib directory"

# Set JAVA_HOME and PATH for Java
export JAVA_HOME=/opt/java/openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Compile the server
echo "Compiling Maxvision Simple TCP Server..."
javac -cp .:postgresql.jar MaxvisionSimpleTcpServer.java

# Setup tables if they don't exist
echo "Setting up database tables (if needed)..."
sleep 5 # Wait for database to be ready
PGPASSWORD=To7Z2UCeWTsriPxbADX8 psql -h balise-postgres -U adminbdb -d balisedb -f create-tables.sql || echo "Could not initialize tables, will rely on auto-creation"

# Run the compiled server with JVM parameters
echo "Starting Maxvision Simple TCP Server..."
java -cp .:postgresql.jar MaxvisionSimpleTcpServer &
SERVER_PID=$!
echo "Maxvision Simple TCP Server started with PID: $SERVER_PID"

# Monitor the application
echo "Keeping container alive for monitoring"
while true; do
    if ! ps -p $SERVER_PID > /dev/null; then
        echo "Server process died, restarting..."
        java -cp .:postgresql.jar MaxvisionSimpleTcpServer &
        SERVER_PID=$!
    fi
    sleep 10
done
