# Crash-Safe Task Scheduler (V1)

A minimal, crash-safe, append-only task scheduler implemented in Java.

This project is intentionally small and opinionated. It focuses on **correctness,
durability, and recovery**, not throughput or features.

The scheduler persists all task state transitions to disk using an append-only log
and is able to recover safely after crashes without corrupting state.

---

## Key Features

- Append-only, crash-safe task log (WAL-style)
- Deterministic crash recovery
- Immutable task IDs (never reused)
- In-memory task registry with persistent backing
- Explicit state transitions (`PENDING → COMPLETED`)
- Simple executor loop (no thread pools, no executors)

---

## Non-Goals

- No concurrency (single-threaded by design)
- No retries or failure handling
- No scheduling policies (FIFO only)
- No compaction or log cleanup
- No distributed execution

This is **V1** by design.

---

## Project Structure

.
├── src
│ └── main
│ └── java
│ └── com
│ └── example
│ ├── api # Submission API
│ ├── core # Core domain types
│ ├── payload # Task payload implementations
│ ├── recovery # Crash recovery logic
│ ├── scheduler # Scheduler & registry
│ ├── store # Append-only task store
│ └── App.java # Entry point
├── pom.xml # Maven configuration
└── README.md

---

## How It Works (High Level)

1. Tasks are submitted via `TaskSubmissionService`
2. Task metadata is appended to a durable log
3. Scheduler reconstructs state from the log
4. Pending tasks are executed exactly once
5. Completion is persisted as a new log entry

---

## Running the Project

```bash
mvn clean compile exec:java
```
