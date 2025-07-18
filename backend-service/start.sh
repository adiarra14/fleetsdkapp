#!/bin/sh

echo "[DEBUG] Starting Fleet SDK Backend Service script"
echo "[DEBUG] Current directory: $(pwd)"
echo "[DEBUG] Listing /app directory:"
ls -la /app
echo "[DEBUG] Listing /app/lib directory:"
ls -la /app/lib

# Set Java options if not already set
JAVA_OPTS=${JAVA_OPTS:-"-Xms256m -Xmx512m"}
echo "[DEBUG] Java options: $JAVA_OPTS"

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
touch /app/backend.log
echo "[DEBUG] Created log file at /app/backend.log"

# Run the application with proper class path and main class
echo "[DEBUG] Running Java with classpath: $CP"
echo "[DEBUG] Running main class: com.maxvision.backend.BackendApplication"

# Redirect all output to both console and log file
java $JAVA_OPTS -cp "$CP" com.maxvision.backend.BackendApplication > >(tee -a /app/backend.log) 2>&1
RESULT=$?

# Log the result
echo "[DEBUG] Java process exited with code: $RESULT"

# Keep container running if the app crashes
if [ $RESULT -ne 0 ]; then
  echo "[ERROR] Backend service crashed with exit code $RESULT, keeping container alive for troubleshooting..."
  echo "[DEBUG] Last 20 lines of log:"
  tail -n 20 /app/backend.log
  # Keep container running
  tail -f /app/backend.log
fi
