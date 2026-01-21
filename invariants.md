# System Invariants

These invariants **must always hold**.

---

## Storage Invariants

1. Log is append-only
2. One record = one logical state transition
3. No record is ever modified or deleted
4. Log truncation only occurs during recovery

---

## Execution Invariants

1. A task is executed at most once
2. A task must be `PENDING` to execute
3. `COMPLETED` tasks are never re-executed

---

## Identity Invariants

1. Task IDs are immutable
2. Task IDs are never reused
3. Task identity is global across time

Violating any invariant breaks correctness.
