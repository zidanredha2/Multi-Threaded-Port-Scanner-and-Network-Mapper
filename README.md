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
git clone [https://github.com/yourusername/network-mapper-backend.git](https://github.com/yourusername/network-mapper-backend.git)
cd network-mapper-backend

# Build the executable fat JAR
mvn clean package

# Run the engine
java -jar target/network-mapper-backend-1.0-SNAPSHOT.jar
```
---

### 3. Keep Configurations Dynamic (Environment Variables)
Right now, your code has values like `threadPoolSize = 100` and `targetHost = "127.0.0.1"` hardcoded inside your `main` method. Hardcoding environment settings makes code difficult to deploy inside Docker containers or production servers.

Before pushing to Git, swap those hardcoded lines in `App.java` with dynamic system pickups:

```java
// Reads from system environment variables; falls back to defaults if not provided
String targetHost = System.getenv("SCAN_TARGET") != null ? System.getenv("SCAN_TARGET") : "127.0.0.1";
int threadPoolSize = System.getenv("SCAN_THREADS") != null ? Integer.parseInt(System.getenv("SCAN_THREADS")) : 100;
```