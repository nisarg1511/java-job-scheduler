# Scheduler Design

## Overview

The scheduler is a **single-threaded, crash-safe task executor** built on top of an **append-only persistent log**.
Its primary responsibility is to **execute tasks reliably** while guaranteeing **correct recovery after crashes**.

## The scheduler prioritizes **correctness, determinism, and recoverability** over throughput or parallelism.

## Core Principles

1. **Persistence-first design**
   The append-only task log is the **only source of truth**.

2. **At-least-once execution**
   Tasks may execute multiple times, but their effects must be idempotent.

3. **Crash safety through replay**
   All in-memory state is reconstructible by replaying the log.

4. **Single-threaded execution**
   Tasks are executed one at a time to simplify reasoning and ensure correctness.

---

## Task Lifecycle

A task transitions through the following states:

```
PENDING  ──────▶  COMPLETED
   ▲                │
   └──── crash ─────┘
```

- `PENDING`
  The task is eligible for execution.
- `COMPLETED`
  The task’s effects have been fully realized and persisted.

### Notes

- `RUNNING` is **not persisted**.
- Any task that was running during a crash is treated as `PENDING` on restart.
- `COMPLETED` is a **terminal state**.

---

## Failure Model

The scheduler assumes the following failure modes:

- Process crash
- Partial disk writes
- Power loss
- Task execution exceptions

### Guarantees

- A task is never marked `COMPLETED` unless its effects are fully applied.
- Partial or corrupted log entries are discarded during recovery.
- No task is lost due to a crash.

---

## Scheduler Phases

### Phase 0 — Bootstrap

Initializes core components:

- TaskStore
- RecoveryManager
- TaskExecutor

No task execution occurs in this phase.

---

### Phase 1 — Recovery

On startup, the scheduler reconstructs state by replaying the task log.

**Responsibilities**

- Load all task records from the log
- Rebuild the final state for each task
- Produce:
  - Ordered list of pending tasks
  - Set of completed tasks

**Outcome**

- In-memory state accurately reflects the last durable system state.

---

### Phase 2 — Selection

The scheduler selects the next task to execute:

- Tasks are selected **in creation order**
- Only tasks in `PENDING` state are considered

---

### Phase 3 — Execution

The selected task is executed:

- Task logic runs
- Side effects are applied

**Important**

- No state is persisted during execution
- Any failure leaves the task in `PENDING`

---

### Phase 4 — Commit

Once task effects are successfully realized:

- A `COMPLETED` record is appended to the log
- The write is forced to disk

This marks the task as terminal.

---

### Phase 5 — Loop Continuation

- The completed task is removed from the pending list
- The scheduler proceeds to the next task

---

## Crash Semantics

The scheduler may crash at any point.

On restart:

- All in-memory state is discarded
- Recovery replays the log
- Tasks not marked `COMPLETED` are retried

This ensures **deterministic recovery** and **no data loss**.

---

## Determinism Guarantee

Given the same log:

- Recovery produces the same in-memory state
- Tasks are executed in the same order
- Scheduler behavior is reproducible

---

## Design Trade-offs

### Why single-threaded?

- Simplifies reasoning
- Avoids concurrency-related failure modes
- Keeps invariants clear and enforceable

### Why no `RUNNING` persistence?

- `RUNNING` is transient
- Tasks running during a crash are retried anyway
- Persistence is reserved for durable state only

---

## Summary

The scheduler is best understood as:

> **A deterministic interpreter of an append-only task log that guarantees correct execution and recovery.**

This design forms a strong foundation for future extensions such as:

- Concurrency
- Retries with backoff
- Dead-letter queues
- Distributed execution

---
