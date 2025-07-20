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

# Multi-stage build for CMA-CGM Integration Service
FROM maven:3.8.6-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy POM file
COPY pom.xml .

# Download dependencies (for better caching)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S cmacgm && \
    adduser -S cmacgm -u 1001 -G cmacgm

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p logs && \
    chown -R cmacgm:cmacgm /app

# Switch to non-root user
USER cmacgm

# Expose port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
    CMD curl -f http://localhost:8090/cmacgm/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
