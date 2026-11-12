
# ============================================================
# STAGE 1: Build the Application
# ============================================================
FROM docker.io/library/maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application JAR directly. Tests run in CI, not in the image build.
RUN mvn -B clean package -DskipTests

# ============================================================
# STAGE 2: Run the Application
# ============================================================
FROM docker.io/library/eclipse-temurin:21-jre-alpine

# curl is used by the container health check; run as an unprivileged user.
# /app/uploads is created in the image so a named volume inherits the appuser ownership.
RUN apk add --no-cache curl \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads \
    && chown -R appuser:appgroup /app/uploads

WORKDIR /app

# Copy the JAR from the build stage.
COPY --from=build /app/target/construction-management-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Container-aware JVM defaults; override JAVA_OPTS to tune further.
ENV SPRING_PROFILES_ACTIVE=default \
    UPLOAD_DIR=/app/uploads \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

USER appuser

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/api/health || exit 1

# exec keeps the JVM as PID 1 so Docker can forward SIGTERM cleanly.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
