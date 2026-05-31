# ===== BUILD STAGE =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package dependency:copy-dependencies -DskipTests

# ===== RUNTIME STAGE =====
FROM ubuntu:22.04
WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive

# Install Java + Chromium + Driver
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    chromium \
    chromium-driver \
    wget curl unzip gnupg \
    fonts-liberation \
    libnss3 libgbm1 libasound2 libx11-xcb1 \
    libatk-bridge2.0-0 libgtk-3-0 libxdamage1 libxrandr2 \
    libu2f-udev libvulkan1 \
    --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy build output
COPY --from=build /app/target /app/target

# Run
CMD ["sh", "-c", "java -cp \"target/classes:target/dependency/*\" ArenaBatchTwo"]
