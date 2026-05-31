# Step 1: Build stage using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Compile the code and grab all dependencies (Selenium, WebDriverManager, etc.)
RUN mvn -B clean package dependency:copy-dependencies -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Clean update, fix broken dependencies, and install the core browser layout libraries
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
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
    libpango-1.0-0 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy the target build directory over cleanly to preserve paths
COPY --from=build /app/target /app/target

# Execute the looped batch code targeting the accurate classpath paths
CMD ["sh", "-c", "java -cp \"target/classes:target/dependency/*\" ArenaBatchTwo"]
