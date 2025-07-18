#!/bin/sh

echo "Starting Fleet SDK TCP Server"

# Set Java options if not already set
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}

# Set the TCP server port from environment or use default
TCP_SERVER_PORT=${TCP_SERVER_PORT:-6060}
export TCP_SERVER_PORT

# Set the classpath to include all JARs in the lib directory
CP=""
for jar in /app/lib/*.jar; do
  if [ -z "$CP" ]; then
    CP="$jar"
  else
    CP="$CP:$jar"
  fi
done

# Run the application with proper class path and main class
java $JAVA_OPTS -cp "$CP" com.maxvision.tcpserver.TcpServerApplication

# Keep container running if the app crashes
if [ $? -ne 0 ]; then
  echo "TCP server crashed, keeping container alive for troubleshooting..."
  tail -f /dev/null
fi
