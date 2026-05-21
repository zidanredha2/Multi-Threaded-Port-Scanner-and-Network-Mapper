# Multi-Threaded TCP Port Scanner & Network Mapper

A high-performance, asynchronous full-stack network utility built in Java 17 using native socket architecture. This project bypasses high-level wrappers to interact directly with the OS TCP stack, utilizing a coordinated worker thread pool to eliminate blocking IO bottlenecks.

## Architectural Highlights
*   **Concurrency Model:** Replaced loose background tasks with a structured `Callable` batch processing queue managed via `ExecutorService`.
*   **Synchronization Safety:** Employs a thread-safe `Collections.synchronizedList` wrapper to eliminate multithreaded write race conditions when logging active ports.
*   **Deterministic Execution:** Utilizes `executor.invokeAll()` to completely block the main tracking thread, preventing early reporting anomalies and ensuring absolute accuracy down to milliseconds.

## Performance Benchmark
*   **Sequential Scan (Standard):** ~3.4 minutes (for 1,024 ports with a 200ms timeout constraint).
*   **Concurrent Scan (100 Workers):** **~2.1 seconds** (A 98.9% reduction in execution latency).

## Prerequisites & Installation
*   Java 17 LTS or higher
*   Maven 3.9+

```bash
# Clone the repository
git clone [https://github.com/zidanredha2/Multi-Threaded-Port-Scanner-and-Network-Mapper.git](https://github.com/zidanredha2/Multi-Threaded-Port-Scanner-and-Network-Mapper.git)
cd Multi-Threaded-Port-Scanner-and-Network-Mapper

# Build the executable fat JAR
mvn clean package

# Run the engine
java -jar target/network-mapper-backend-1.0-SNAPSHOT.jar
```
---

## 🐳 Docker Containerization

The application utilizes a **multi-stage Docker build** to separate the compilation environment from the final execution runtime. This cloud-native best practice strips away heavy build tools (like Maven and the full JDK), reducing the final production image size from over 800MB down to an ultra-lightweight **~100MB**.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.

---

### 1. Create the Dockerfile

Ensure you have a file named exactly `Dockerfile` (no extension) in your root directory (`Multi-Threaded-Port-Scanner-and-Network-Mapper/`) with the following multi-stage build setup:

```dockerfile
# ==========================================
# STAGE 1: The Compilation & Build Environment
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy the build configuration file first to leverage Docker layer caching
COPY pom.xml .

# Copy the entire source directory
COPY src ./src

# Compile and package the executable shadow fat-JAR, skipping unit tests
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: The Lightweight Runtime Environment
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy ONLY the final compiled binary from the builder stage
COPY --from=builder /build/target/network-mapper-backend-1.0-SNAPSHOT.jar app.jar

# Run the application as a non-root system user for production security hardening
RUN addgroup -S runnergroup && adduser -S runneruser -G runnergroup
USER runneruser

# Define the execution command to launch our multi-threaded engine
ENTRYPOINT ["java", "-jar", "app.jar"]

```

---

### 2. Build the Production Image

Navigate to the root directory containing the `Dockerfile` and execute the following command to compile your source code and build the hardened runtime container:

```bash
docker build -t network-scanner-backend .

```

---

### 3. Execution Environments

Because Docker containers run inside an isolated virtual network by default, you can run this tool in two distinct networking modes depending on your objective:

#### Mode A: Isolated Virtual Bridge (Default Sandbox)

This mode limits the scanner to its own internal container sandbox. It is ideal for staging environments or checking internal container dependencies.

```bash
docker run -e SCAN_TARGET="127.0.0.1" -e SCAN_THREADS="150" network-scanner-backend

```

> 📌 *Note: In this mode, scanning `127.0.0.1` refers to the container itself, so it will likely report zero open ports unless services are running explicitly inside it.*

#### Mode B: Host Networking Mode (Real-World Adapter Access)

To grant the application direct access to scan the host machine's physical network adapters and discover actual open system ports, attach the `--network=host` flag:

```bash
docker run --network=host -e SCAN_TARGET="127.0.0.1" -e SCAN_THREADS="100" network-scanner-backend

```

> 🔥 *Observation: Running this mode bypasses container network virtualization, allowing your Java engine to instantly flag system daemons (like CUPS on port `631`, local databases, or active web servers) running on your physical machine.*

---

### ⚙️ Dynamic Runtime Configuration

The containerized engine dynamically shifts behavior based on injected environment variables, completely removing the need to recompile your source code:

| Environment Variable | Default Value | Description                                                              |
|----------------------|---------------|--------------------------------------------------------------------------|
| `SCAN_TARGET`        | `127.0.0.1`   | The target IP address or domain name to scan.                            |
| `SCAN_THREADS`       | `100`         | The number of concurrent worker threads allocated to the execution pool. |

---

