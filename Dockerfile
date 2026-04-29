# syntax=docker/dockerfile:1.6

# ─── Stage 1: build (Java 21 + Node via frontend-maven-plugin) ────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./

COPY frontend/package.json frontend/pnpm-lock.yaml frontend/
RUN mvn -B -q -DskipTests -pl . dependency:go-offline || true

COPY frontend frontend
COPY src src

RUN mvn -B -DskipTests clean package

# ─── Stage 2: runtime (slim JRE) ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system delivra && useradd --system --gid delivra delivra

COPY --from=build /build/target/service-*.jar /app/app.jar

RUN mkdir -p /app/uploads && chown -R delivra:delivra /app
USER delivra

EXPOSE 8189
ENV JAVA_OPTS="" \
    UPLOAD_DIR=/app/uploads

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8189/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
