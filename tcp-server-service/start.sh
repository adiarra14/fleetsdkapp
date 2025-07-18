#!/bin/sh

# Write directly to stdout/stderr for Portainer logging
exec 1>/dev/stdout 2>/dev/stderr

echo "==== PORTAINER-DEBUG: Fleet SDK TCP Server starting at $(date) ===="
echo "==== PORTAINER-DEBUG: Environment variables: ===="
env | grep -v PASSWORD

# Always keep the container running, regardless of application exit status
KEEP_RUNNING=true

echo "[DEBUG] Starting Fleet SDK TCP Server script"
echo "[DEBUG] Current directory: $(pwd)"
echo "[DEBUG] Listing /app directory:"
ls -la /app
echo "[DEBUG] Listing /app/lib directory:"
ls -la /app/lib

# Set Java options if not already set
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}
echo "[DEBUG] Java options: $JAVA_OPTS"

# Set the TCP server port from environment or use default
TCP_SERVER_PORT=${TCP_SERVER_PORT:-6060}
export TCP_SERVER_PORT
echo "[DEBUG] TCP server port: $TCP_SERVER_PORT"

# Set the classpath to include all JARs in the lib directory
CP=""
for jar in /app/lib/*.jar; do
  if [ -z "$CP" ]; then
    CP="$jar"
  else
    CP="$CP:$jar"
  fi
done

# Create a log file
touch /app/tcpserver.log
echo "[DEBUG] Created log file at /app/tcpserver.log"

# First, just run a simple Java version check to see if Java works
echo "[DEBUG] Java version check:"
java -version
JAVA_VERSION_RESULT=$?
echo "[DEBUG] Java version check exit code: $JAVA_VERSION_RESULT"

# Run the application with proper class path and main class
echo "[DEBUG] Running Java with classpath: $CP"
echo "[DEBUG] Running main class: com.maxvision.tcpserver.TcpServerApplication"

# Run Java application in background so script continues
java $JAVA_OPTS -cp "$CP" com.maxvision.tcpserver.TcpServerApplication > /app/tcpserver.log 2>&1 &
JAVA_PID=$!
echo "[DEBUG] Java process started with PID: $JAVA_PID"

# Wait a few seconds to see if it crashes immediately
sleep 5

# Check if process is still running
if ps -p $JAVA_PID > /dev/null; then
    echo "[DEBUG] Java process is still running after 5 seconds"
else
    echo "[ERROR] Java process exited within 5 seconds"
    echo "[DEBUG] Last 20 lines of log:"
    tail -n 20 /app/tcpserver.log
fi

# Keep the container running no matter what
echo "[DEBUG] Keeping container alive for logs and troubleshooting"
while true; do
    echo "[HEARTBEAT] Container still alive at $(date)" >> /app/tcpserver.log
    sleep 60
done
