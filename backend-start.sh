#!/bin/sh
# Backend service startup script

# Set default values for environment variables if not provided
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-backend}
SERVER_PORT=${SERVER_PORT:-6060}
JAVA_OPTS=${JAVA_OPTS:-"-Xmx512m -Xms256m"}

echo "Starting Backend Service..."
echo "Using profile: ${SPRING_PROFILES_ACTIVE}"
echo "On port: ${SERVER_PORT}"
echo "With Java options: ${JAVA_OPTS}"

# Manually launch the JAR with specified main class
# Using com.maxvision.fleet.FleetGatewayApplication as the main class
exec java ${JAVA_OPTS} \
  -cp /app/app.jar \
  com.maxvision.fleet.FleetGatewayApplication \
  --spring.profiles.active=${SPRING_PROFILES_ACTIVE} \
  --server.port=${SERVER_PORT}
