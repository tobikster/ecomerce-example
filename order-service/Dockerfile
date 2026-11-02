# Stage 1: Build & extract layers
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy gradle wrapper and configuration files first to leverage Docker layer caching
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY gradle gradle
COPY common/outbox/build.gradle.kts common/outbox/
COPY order-service/build.gradle.kts order-service/

RUN ./gradlew :order-service:dependencies --no-daemon || true

# Copy source code and build production jar
COPY common/outbox/src common/outbox/src
COPY order-service/src order-service/src
RUN ./gradlew :order-service:bootJar -x test --no-daemon

# Extract application layers using Spring Boot jarmode tools
RUN java -Djarmode=tools -jar order-service/build/libs/*.jar extract --layers --application-filename app.jar --destination order-service/build/extracted

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# Run as a dedicated non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy layers from least frequently to most frequently changing
COPY --from=builder /workspace/order-service/build/extracted/dependencies/ ./
COPY --from=builder /workspace/order-service/build/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/order-service/build/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/order-service/build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
