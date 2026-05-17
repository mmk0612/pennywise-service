# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Create resources directory if it doesn't exist
# Create application.yml with environment variable placeholders
RUN mkdir -p src/main/resources && \
    printf '%s\n' \
      'spring:' \
      '  application:' \
      '    name: pennywise-server' \
      '  datasource:' \
      '    url: ${DB_URL:${DATABASE_URL:}}' \
      '    username: ${DB_USERNAME}' \
      '    password: ${DB_PASSWORD}' \
      '    driver-class-name: org.postgresql.Driver' \
      '  jpa:' \
      '    hibernate:' \
      '      ddl-auto: update' \
      '    show-sql: false' \
      '    open-in-view: false' \
      '    properties:' \
      '      hibernate:' \
      '        format_sql: true' \
      '  mail:' \
      '    host: ${MAIL_HOST}' \
      '    port: ${MAIL_PORT}' \
      '    username: ${MAIL_USERNAME}' \
      '    password: ${MAIL_PASSWORD}' \
      '    properties:' \
      '      mail:' \
      '        smtp:' \
      '          auth: true' \
      '          starttls:' \
      '            enable: true' \
      '  rabbitmq:' \
      '    addresses: ${RABBITMQ_URL}' \
      'server:' \
      '  port: ${SERVER_PORT:8080}' \
      'app:' \
      '  cors:' \
      '    allowed-origins: ${APP_CORS_ALLOWED_ORIGINS}' \
      '  jwt:' \
      '    secret: ${JWT_SECRET}' \
      '    expiration: ${JWT_EXPIRATION:3600000}' \
      '    refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}' \
      'nvidia:' \
      '  api:' \
      '    key: ${NVIDIA_API_KEY}' \
      '    url: ${NVIDIA_API_URL:https://integrate.api.nvidia.com/v1/chat/completions}' \
      '    model: ${NVIDIA_API_MODEL:google/gemma-3n-e2b-it}' \
      'pennywise:' \
      '  s3:' \
      '    bucket: ${PENNYWISE_S3_BUCKET}' \
      '    region: ${PENNYWISE_S3_REGION}' \
      '    access-key: ${PENNYWISE_S3_ACCESS_KEY}' \
      '    secret-key: ${PENNYWISE_S3_SECRET_KEY}' \
      > src/main/resources/application.yml

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/server-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
