FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY backend/pom.xml pom.xml
COPY backend/.mvn .mvn
COPY backend/mvnw mvnw
COPY backend/src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre
# Run as an unprivileged, non-root user to limit container-escape blast radius.
RUN groupadd --system --gid 1001 contracthawk \
    && useradd --system --uid 1001 --gid contracthawk --home-dir /app --shell /usr/sbin/nologin contracthawk \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
# Pre-create the runtime storage dir with the correct owner so the named
# volume inherits it on first mount in the compose setup.
RUN mkdir -p /data/contracts \
    && chown -R contracthawk:contracthawk /app /data/contracts
USER contracthawk
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health || exit 1
# Size the heap to the container's memory limit instead of the host's.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]
