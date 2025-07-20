#!/bin/sh
# TCP Server startup script

# Set default values for environment variables if not provided
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-tcp-server}
SERVER_PORT=${SERVER_PORT:-6060}
TCP_SERVER_PORT=${TCP_SERVER_PORT:-6060}
JAVA_OPTS=${JAVA_OPTS:-"-Xmx256m -Xms128m"}

echo "Starting TCP Server..."
echo "Using profile: ${SPRING_PROFILES_ACTIVE}"
echo "On port: ${SERVER_PORT} / TCP port: ${TCP_SERVER_PORT}"
echo "With Java options: ${JAVA_OPTS}"

# Manually launch the JAR with specified main class
# Using com.maxvision.fleet.FleetGatewayApplication as the main class with tcp-server profile
exec java ${JAVA_OPTS} \
  -cp /app/app.jar \
  com.maxvision.fleet.FleetGatewayApplication \
  --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  --server.port=${SERVER_PORT} \
  --tcp.server.port=${TCP_SERVER_PORT}
