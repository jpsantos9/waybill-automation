# ------------------------------------------------------------
# Builder stage: build the Spring Boot jar using Maven + JDK 17
# ------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy pom first to leverage Docker layer caching
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy source and build
COPY src ./src

# Build executable JAR (skip tests)
RUN mvn -B -DskipTests package

# ------------------------------------------------------------
# Runtime stage: JRE 17 + Chrome for Selenium
# ------------------------------------------------------------
FROM eclipse-temurin:17-jre

ENV APP_HOME=/app
ENV TZ=Asia/Manila
ENV DEBIAN_FRONTEND=noninteractive

WORKDIR ${APP_HOME}

# Install system dependencies + tzdata
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    ca-certificates \
    wget \
    gnupg \
    fonts-liberation \
    libnss3 \
    libxss1 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libc6 \
    libcairo2 \
    libgbm1 \
    libgtk-3-0 \
    libx11-6 \
    libx11-xcb1 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxrandr2 \
    xdg-utils \
    tzdata \
 && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
 && echo $TZ > /etc/timezone \
 && dpkg-reconfigure -f noninteractive tzdata \
 && rm -rf /var/lib/apt/lists/*

# Install Google Chrome
RUN wget -O /tmp/chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
 && apt-get update \
 && apt-get install -y --no-install-recommends /tmp/chrome.deb \
 && rm -rf /tmp/chrome.deb /var/lib/apt/lists/*

# Create non-root user
RUN addgroup --system appgroup \
 && adduser --system --ingroup appgroup --home /home/appuser appuser || true

# Ensure HOME and selenium cache exist and are writable
RUN mkdir -p /home/appuser/.cache/selenium \
 && chown -R appuser:appgroup /home/appuser \
 && chmod -R 750 /home/appuser

# Copy the built Spring Boot executable JAR
COPY --from=builder /workspace/target/*.jar app.jar

RUN echo "DEBUG: /app contents:" && ls -l /app

# Prepare logs directory
RUN mkdir -p /app/logs \
 && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/app.jar"]
