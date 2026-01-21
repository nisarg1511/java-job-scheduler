# Task State Machine

This document defines the **authoritative state machine** for tasks managed by the single-node Scheduler.

It specifies:
- Valid task states
- Legal state transitions
- Forbidden transitions
- Crash-recovery implications

This state machine is derived directly from `failure-modes.md` and must not contradict it.

---

## Task States

- **PENDING**  
  Task is known to the scheduler but has not started execution.

- **RUNNING**  
  Task execution has started, but completion has not yet been durably recorded.

- **COMPLETED**  
  Task effects have been applied and completion has been durably persisted.

---

## State Transition Diagram (Logical)

```
PENDING  ──▶  RUNNING  ──▶  COMPLETED
   ▲          │
   └──────────┘
     (retry after crash)
```

---

## Legal Transitions

### 1. `PENDING → RUNNING`

**When:**
- Scheduler selects a task for execution

**Actions:**
- Persist `RUNNING` state atomically
- Begin task execution

**Invariant:**
- A task must not start execution unless the scheduler intends to run it

---

### 2. `RUNNING → COMPLETED`

**When:**
- Task execution finishes successfully
- Task effects have been fully applied

**Actions:**
- Persist `COMPLETED` state atomically

**Invariant:**
- `COMPLETED` must only be recorded after effects are applied

---

### 3. `RUNNING → PENDING` (Implicit, via crash)

**When:**
- Scheduler crashes during execution
- `COMPLETED` was not durably recorded

**Actions on Restart:**
- Task is treated as `PENDING`
- Task is eligible for retry

**Invariant:**
- Tasks must be idempotent

---

## Forbidden Transitions

The following transitions must **never** occur:

- `PENDING → COMPLETED`  
  (Completion without execution)

- `COMPLETED → RUNNING`  
  (No re-execution of completed tasks)

- `COMPLETED → PENDING`  
  (Completed tasks are terminal)

---

## Crash-Recovery Semantics

On scheduler startup:

- Tasks with state `COMPLETED` are ignored
- Tasks with state `RUNNING` are treated as `PENDING`
- Tasks with state `PENDING` are eligible for execution

This ensures:
- At-least-once execution
- Correctness via idempotency

---

## Design Guarantees

- The scheduler provides **at-least-once execution semantics**
- Exactly-once execution is explicitly not attempted
- Correctness relies on:
  - Idempotent task effects
  - Atomic persistence
  - Conservative retry rules

---

## Non-Goals

- Distributed task execution
- Remote workers
- Task preemption
- Exactly-once guarantees

These concerns are deferred to later systems (e.g., Workflow Engine).

