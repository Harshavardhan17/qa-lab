# syntax=docker/dockerfile:1

# ---------- Build stage: compile the Spring Boot fat jar with the Gradle wrapper ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy only the build definition first so dependency downloads are cached as a layer.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon help > /dev/null 2>&1 || true

# Now copy sources and build (skip tests here; tests are run separately by the developer).
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Runtime stage: small JRE image ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Curl is only used by the container HEALTHCHECK below.
RUN apk add --no-cache curl

# Low-footprint JVM defaults (override with JAVA_OPTS in docker-compose.yml).
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC"

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=10 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
