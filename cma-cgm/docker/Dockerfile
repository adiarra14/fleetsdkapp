#
# Fleet Monitor - CMA-CGM Integration Module
# Docker Configuration
#
# Project: Fleet Management System with Maxvision SDK Integration
# Client: SiniTechnologie / Sinigroupe (for CMA-CGM customer)
# Project Chief: Antoine Diarra
#
# Copyright (c) 2025 Ynnov
# All rights reserved. This software and associated documentation files are the property
# of Ynnov until transfer to the client SiniTechnologie/Sinigroupe.
#
# Unauthorized copying, modification, distribution, or use of this software
# is strictly prohibited without prior written consent from Ynnov.
#

# Simple Java runtime for CMA-CGM Integration Service
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Install required packages
RUN apk add --no-cache curl

# Copy source files and compile simple Java server
COPY src/main/java/com/cmacgm/integration/SimpleCmaCgmServer.java .
RUN javac -cp "." SimpleCmaCgmServer.java

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run the simple Java server
CMD ["java", "SimpleCmaCgmServer"]
