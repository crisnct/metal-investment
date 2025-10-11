#FROM openjdk:9-jdk-slim
#VOLUME /tmp
#COPY /target/metal-investment-0.0.1.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]
#
# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN useradd -r -u 1001 -g root appuser
USER appuser

# Expose port
EXPOSE 8080

# Health check (temporarily disabled for debugging)
# HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#   CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Create startup script
RUN echo '#!/bin/bash' > /app/start.sh && \
    echo 'echo "Starting application..."' >> /app/start.sh && \
    echo 'echo "PORT: ${PORT:-8080}"' >> /app/start.sh && \
    echo 'echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}"' >> /app/start.sh && \
    echo 'echo "DATABASE_URL: ${DATABASE_URL:-not set}"' >> /app/start.sh && \
    echo 'echo "DB_USERNAME: ${DB_USERNAME:-not set}"' >> /app/start.sh && \
    echo 'echo "DB_PASSWORD: ${DB_PASSWORD:-not set}"' >> /app/start.sh && \
    echo 'java -Djava.security.egd=file:/dev/./urandom \' >> /app/start.sh && \
    echo '  -Dserver.port=${PORT:8080} \' >> /app/start.sh && \
    echo '  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod} \' >> /app/start.sh && \
    echo '  -Xmx512m \' >> /app/start.sh && \
    echo '  -Xms256m \' >> /app/start.sh && \
    echo '  -XX:+UseG1GC \' >> /app/start.sh && \
    echo '  -XX:MaxGCPauseMillis=200 \' >> /app/start.sh && \
    echo '  -Dspring.datasource.url=${DATABASE_URL} \' >> /app/start.sh && \
    echo '  -Dspring.datasource.username=${DB_USERNAME} \' >> /app/start.sh && \
    echo '  -Dspring.datasource.password=${DB_PASSWORD} \' >> /app/start.sh && \
    echo '  -Dlogging.level.com.investment.metal=DEBUG \' >> /app/start.sh && \
    echo '  -Dlogging.level.org.springframework.boot=INFO \' >> /app/start.sh && \
    echo '  -Dlogging.level.org.springframework.web=INFO \' >> /app/start.sh && \
    echo '  -Dlogging.level.org.springframework.security=INFO \' >> /app/start.sh && \
    echo '  -jar app.jar' >> /app/start.sh && \
    chmod +x /app/start.sh

# Run the application
ENTRYPOINT ["/app/start.sh"]
