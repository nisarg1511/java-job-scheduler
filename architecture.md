# Architecture Overview

The scheduler is built around a **single source of truth**: an append-only log.

All in-memory state is derived from this log.

---

## Core Components

### TaskStore

- Durable, append-only storage
- One record per line (newline-delimited JSON)
- No mutation, no deletion

### TaskRegistry

- In-memory mapping of `taskId → TaskPayload`
- Non-persistent
- Rebuilt via recovery

### RecoveryManager

- Reads the log
- Reconstructs the latest state per task
- Determines which tasks are pending

### Scheduler

- Executes pending tasks
- Persists state transitions
- Stateless across restarts

---

## Data Flow

Submit Task
↓
Append PENDING record
↓
In-memory registry updated
↓
Crash? → Recovery reads log
↓
Scheduler executes pending tasks
↓
Append COMPLETED record

---

## Persistence Model

- **Log-only**
- No snapshots
- No checkpoints
- Recovery always replays from the beginning

This trades performance for correctness and simplicity.
