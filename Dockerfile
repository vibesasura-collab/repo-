# Step 1: Build stage using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Compile code and grab all dependencies (Selenium, WebDriverManager, etc.)
RUN mvn -B clean package dependency:copy-dependencies -DskipTests

# Step 2: Runtime stage using a stable Ubuntu base with working package trees
FROM ubuntu:22.04
WORKDIR /app

# Install Java 17 JRE along with basic utilities your script needs to grab Chrome
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    wget \
    curl \
    unzip \
    ca-certificates \
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
    libxfixes3 \
    librandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    --no-install-recommends && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the target build directory over cleanly
COPY --from=build /app/target /app/target

# Execute the looped batch code targeting the accurate classpath paths
CMD ["sh", "-c", "java -cp \"target/classes:target/dependency/*\" ArenaBatchTwo"]
