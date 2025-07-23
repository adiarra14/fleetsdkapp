#!/bin/bash

# Script to test database connection from TCP server container in Portainer
# Run this inside the balise-tcp-server container

echo "=== PORTAINER TCP SERVER DATABASE CONNECTION TEST ==="
echo "Container: balise-tcp-server"
echo "Target: balise-postgres:5432/balisedb"
echo "Timestamp: $(date)"
echo ""

# Check if PostgreSQL JAR is available
echo "🔍 Checking PostgreSQL driver..."
if [ -f "/app/lib/postgresql-42.7.4.jar" ]; then
    echo "✅ PostgreSQL driver found: /app/lib/postgresql-42.7.4.jar"
else
    echo "❌ PostgreSQL driver not found in /app/lib/"
    ls -la /app/lib/ | grep -i postgres || echo "No PostgreSQL JAR found"
fi

# Check network connectivity to PostgreSQL
echo ""
echo "🔍 Testing network connectivity to PostgreSQL..."
if command -v nc >/dev/null 2>&1; then
    if nc -z balise-postgres 5432; then
        echo "✅ Network connection to balise-postgres:5432 successful"
    else
        echo "❌ Cannot connect to balise-postgres:5432"
    fi
else
    echo "⚠️ netcat not available, skipping network test"
fi

# Check DNS resolution
echo ""
echo "🔍 Testing DNS resolution..."
if command -v nslookup >/dev/null 2>&1; then
    nslookup balise-postgres
elif command -v host >/dev/null 2>&1; then
    host balise-postgres
else
    echo "⚠️ DNS tools not available"
fi

# Compile and run the Java test
echo ""
echo "🔍 Compiling and running Java database test..."
cd /app

# Compile the test
javac -cp "/app/lib/*" TestDatabaseConnection.java

if [ $? -eq 0 ]; then
    echo "✅ Java compilation successful"
    
    # Run the test
    echo ""
    echo "🚀 Running database connection test..."
    java -cp "/app/lib/*:/app" TestDatabaseConnection
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "🎉 DATABASE CONNECTION TEST COMPLETED SUCCESSFULLY!"
    else
        echo ""
        echo "❌ DATABASE CONNECTION TEST FAILED!"
    fi
else
    echo "❌ Java compilation failed"
fi

echo ""
echo "=== TEST COMPLETED ==="
