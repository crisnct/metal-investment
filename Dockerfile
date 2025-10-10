#FROM openjdk:8-jdk-alpine
#VOLUME /tmp
#COPY /target/metal-investment-0.0.1.jar app.jar
#ENTRYPOINT ["java", "-jar", "/app.jar"]
#
# Multi-stage build for Spring Boot application
FROM maven:3.8.6-openjdk-8 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:8-jre-slim

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN useradd -r -u 1001 -g root appuser
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=30s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.port=${PORT:8080}", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
  "-jar", \
  "app.jar"]