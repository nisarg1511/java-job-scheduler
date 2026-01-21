# Crash Recovery Model

Crash recovery is a first-class concern in this system.

---

## Failure Assumptions

- Process can crash at any time
- Disk writes may be partially written
- JVM may terminate mid-execution

---

## Recovery Strategy

1. Read log sequentially
2. Stop at first corrupted record
3. Truncate log to last known-good position
4. Rebuild task state map:
   - Last record per task wins

---

## Why This Is Safe

- Append-only writes
- Durable fsync on append
- No in-place updates
- Deterministic replay

---

## Result of Recovery

- A set of task IDs in `PENDING`
- Registry repopulated in memory
- Scheduler can resume execution safely
