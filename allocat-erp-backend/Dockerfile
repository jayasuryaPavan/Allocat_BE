# Multi-stage build for Allocat ERP Backend
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom files first for better layer caching
COPY pom.xml .
COPY allocat-common/pom.xml allocat-common/
COPY allocat-security/pom.xml allocat-security/
COPY allocat-core/pom.xml allocat-core/
COPY allocat-gateway/pom.xml allocat-gateway/
COPY allocat-api/pom.xml allocat-api/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from builder stage
# For Phase 1, we deploy the API service directly
# (Gateway can be added as a separate service later if needed)
COPY --from=builder /app/allocat-api/target/*.jar app.jar

# Expose the API port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
