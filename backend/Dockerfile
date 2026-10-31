
# ============================================================
# STAGE 1: Build the Application
# ============================================================
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application JAR directly
RUN mvn clean package -DskipTests

# ============================================================
# STAGE 2: Run the Application
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the JAR from build stage (adjust jar name if yours differs slightly)
COPY --from=build /app/target/construction-management-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS=""

USER appuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
