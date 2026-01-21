# Failure Modes & Recovery Semantics

This document defines the failure assumptions, invariants, and recovery behavior of the **single-node Scheduler**.

The scheduler and task execution are **co-located in the same process**. There are no remote workers. If the scheduler crashes, all in-flight tasks terminate.

---

## Core Assumptions

- Scheduler and task execution run in the same process
- File-based persistence (no embedded DB)
- Tasks may be retried
- Tasks must be **idempotent**
- A task is considered **COMPLETED only when its effects are durably applied**

---

## Task States

- `PENDING` – Task is known but not started
- `RUNNING` – Task execution has started
- `COMPLETED` – Task effects have been applied and durably recorded

---

## Failure Scenarios

### 1. Scheduler Process Crash

**What fails:** Scheduler process terminates unexpectedly

**Possible state:**

- In-memory task state is lost
- Disk state remains

**Recovery:**

- Scheduler restarts
- Tasks not durably marked as `COMPLETED` are retried

---

### 2. Crash Before Recording `RUNNING`

**What fails:** Scheduler crashes after starting execution but before persisting `RUNNING`

**Possible state:**

- Task execution never completed
- No durable `RUNNING` record

**Recovery:**

- Task is treated as `PENDING`
- Task is retried on restart

**Correctness condition:**

- Safe because task execution did not survive the crash

---

### 3. Crash During Task Execution

**What fails:** Scheduler crashes while task is executing

**Possible state:**

- Partial effects may have been applied

**Recovery:**

- Task is retried
- Task effects must be idempotent

---

### 4. Crash After Execution but Before Recording `COMPLETED`

**What fails:** Task finishes execution, but scheduler crashes before persisting `COMPLETED`

**Possible state:**

- Effects may have been applied
- No durable completion record

**Recovery:**

- Task is retried
- Idempotency ensures correctness

---

### 5. Crash During Disk Write (Partial Write)

**What fails:** System crashes while writing task state to disk

**Possible state:**

- Corrupt or partial data

**Recovery:**

- Partial writes are treated as failures
- Writes must be atomic (write-then-rename)
- Task is retried

---

## Design Guarantees

- The scheduler does **not** attempt exactly-once execution
- Correctness is achieved through:
  - Idempotent task effects
  - Conservative retry semantics
  - Atomic persistence

---

## Non-Goals

- Distributed execution
- Task isolation across processes
- Exactly-once execution guarantees
- High availability

These are intentionally deferred to later projects (e.g., Workflow Engine).
