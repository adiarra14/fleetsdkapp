#!/bin/sh

echo "Starting Fleet SDK Backend Service"

# Set Java options if not already set
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}

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
java $JAVA_OPTS -cp "$CP" com.maxvision.backend.BackendApplication

# Keep container running if the app crashes
if [ $? -ne 0 ]; then
  echo "Backend service crashed, keeping container alive for troubleshooting..."
  tail -f /dev/null
fi
