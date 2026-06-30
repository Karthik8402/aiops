# ── Stage 1: build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven

# Copy pom.xml and resolve dependencies using a persistent cache mount
COPY pom.xml .
COPY spotbugs-exclude.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B -Dmaven.wagon.http.retryHandler.count=5

# Copy src and build package using the same persistent cache mount
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -Dmaven.wagon.http.retryHandler.count=5

RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user and install curl for health checks
RUN apk add --no-cache curl && \
    addgroup -S aiops && adduser -S aiops -G aiops

# Copy the extracted layers directly (this merges them into /app)
COPY --from=builder --chown=aiops:aiops /build/extracted/dependencies/ ./
COPY --from=builder --chown=aiops:aiops /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=aiops:aiops /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=aiops:aiops /build/extracted/application/ ./

USER aiops

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
