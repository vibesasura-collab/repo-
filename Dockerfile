# Step 1: Build the Java project using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# Step 2: Create the runtime execution box
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install dependencies and Chrome via official Google repository setup
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    gnupg \
    unzip \
    --no-install-recommends && \
    # Set up Google's modern, secure signing keys properly
    install -d /etc/apt/keyrings && \
    curl -fsSL https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /etc/apt/keyrings/google-chrome.gpg && \
    echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
    # Now update and install chrome along with its dependencies automatically
    apt-get update && apt-get install -y \
    google-chrome-stable \
    --no-install-recommends && \
    # Clean up to keep the container small
    rm -rf /var/lib/apt/lists/*

# Copy the compiled target files from the build stage
COPY --from=build /app/target /app/target

# Dynamic entrypoint tracking variable matching your specified logic
CMD ["sh", "-c", "java -cp target/*:target/dependency/* Main --mainClass=${BOT_CLASS}"]
