# Multi-stage Backend Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Production Runtime Image
FROM eclipse-temurin:17-jre-jammy

# Install curl for container health check
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root application user
RUN groupadd -r fieldops && \
    useradd -r -g fieldops -u 10001 -m -d /app fieldops

WORKDIR /app

# Copy application binary with non-root ownership
COPY --from=build --chown=fieldops:fieldops /app/target/*.jar /app/app.jar

USER fieldops:fieldops

ENV PORT=8082 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8082}/actuator/health || exit 1

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
