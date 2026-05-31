# Step 1: Compile the Maven project
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# Step 2: Build the execution container with Chrome pre-installed
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install Google Chrome dependencies automatically
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    curl \
    unzip \
    libglib2.0-0 \
    libnss3 \
    libgconf-2-4 \
    libfontconfig1 \
    --no-install-recommends && \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && apt-get install -y google-chrome-stable --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Copy compiled jar dependencies from the build stage
COPY --from=build /app/target /app/target

# Tell the container how to kick off your custom batch file
ENTRYPOINT ["mvn", "-B", "exec:java", "-Dexec.mainClass=ArenaBatchTwo"]
