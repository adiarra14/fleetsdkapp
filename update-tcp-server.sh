#!/bin/bash
# Script to update the TCP server with the fixed LockReportServiceImpl

echo "=== UPDATING TCP SERVER WITH DATABASE FIXES ==="
echo "Timestamp: $(date)"

# Compile the updated class
echo "Compiling LockReportServiceImpl.java..."
javac -cp "/app/lib/*:/usr/local/openjdk/lib/*" LockReportServiceImpl.java

if [ $? -ne 0 ]; then
  echo "ERROR: Compilation failed!"
  exit 1
fi

# Create deployment directory
mkdir -p deploy/com/maxvision/fleet/sdk

# Copy the compiled class to deployment directory
cp LockReportServiceImpl.class deploy/com/maxvision/fleet/sdk/

# Create JAR file with the updated class
echo "Creating JAR with updated classes..."
cd deploy
jar cf tcp-server-update.jar com/maxvision/fleet/sdk/LockReportServiceImpl.class
cd ..

echo "Update JAR created: deploy/tcp-server-update.jar"

echo "=== DEPLOYMENT INSTRUCTIONS ==="
echo "To deploy this update to Portainer:"
echo "1. Copy the JAR file to the TCP server container:"
echo "   docker cp deploy/tcp-server-update.jar tcp-server-container:/app/lib/"
echo ""
echo "2. Restart the TCP server container:"
echo "   docker restart tcp-server-container"
echo ""
echo "3. Check logs to verify the update:"
echo "   docker logs -f tcp-server-container"
