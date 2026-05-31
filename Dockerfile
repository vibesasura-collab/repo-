# Step 1: Build the Java project using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# Step 2: Create the runtime execution box
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install standard dependencies and download the Chrome package directly
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    libglib2.0-0 \
    libnss3 \
    libfontconfig1 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixees3 \
    librandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    --no-install-recommends && \
    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb --no-install-recommends && \
    rm -f google-chrome-stable_current_amd64.deb && \
    rm -rf /var/lib/apt/lists/*

# Copy the compiled target files from the build stage
COPY --from=build /app/target /app/target

# Dynamic entrypoint tracking variable matching your specified logic
CMD ["sh", "-c", "java -cp target/*:target/dependency/* Main --mainClass=${BOT_CLASS}"]
