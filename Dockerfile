# ===== BUILD STAGE =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package dependency:copy-dependencies -DskipTests

# ===== RUNTIME STAGE =====
FROM ubuntu:22.04
WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive

# Base dependencies
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    wget curl unzip gnupg \
    fonts-liberation \
    libnss3 libgbm1 libasound2 libx11-xcb1 \
    libatk-bridge2.0-0 libgtk-3-0 libxdamage1 libxrandr2 \
    libu2f-udev libvulkan1 \
    --no-install-recommends

# Install Google Chrome (stable fix)
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb

# Copy build output
COPY --from=build /app/target /app/target

# Run app
CMD ["sh", "-c", "java -cp \"target/classes:target/dependency/*\" ArenaBatchTwo"]
