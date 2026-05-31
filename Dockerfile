# Step 1: Build stage using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Compile the code and grab all dependencies (Selenium, WebDriverManager, etc.)
RUN mvn -B clean package dependency:copy-dependencies -DskipTests

# Step 2: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install basic network utilities your code needs to pull the chrome package
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
    libxfixes3 \
    librandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Copy compiled files over to runtime container
COPY --from=build /app/target/classes /app/classes
COPY --from=build /app/target/dependency /app/dependency

# Execute using your exact file layout target
CMD ["sh", "-c", "java -cp \"/app/classes:/app/dependency/*\" ArenaBatchTwo"]
