# Stage 1: Build & extract layers
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy gradle wrapper and configuration files first to leverage Docker layer caching
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties* ./
COPY gradle gradle
COPY payment-service/build.gradle.kts payment-service/

RUN ./gradlew :payment-service:dependencies --no-daemon || true

# Copy source code and build production jar
COPY payment-service/src payment-service/src
RUN ./gradlew :payment-service:bootJar -x test --no-daemon

# Extract application layers using Spring Boot jarmode tools
RUN java -Djarmode=tools -jar payment-service/build/libs/*.jar extract --layers --application-filename app.jar --destination payment-service/build/extracted

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /application

# Run as a dedicated non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy layers from least frequently to most frequently changing
COPY --from=builder /workspace/payment-service/build/extracted/dependencies/ ./
COPY --from=builder /workspace/payment-service/build/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/payment-service/build/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/payment-service/build/extracted/application/ ./

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
