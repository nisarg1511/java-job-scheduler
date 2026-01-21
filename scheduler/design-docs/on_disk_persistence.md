# On-Disk Persistence Model

This document defines how the Scheduler persists task state to disk in a **crash-safe, minimal, file-based** manner.

The persistence model is designed to:
- Survive process crashes
- Avoid partial or corrupt writes
- Support restart recovery
- Align exactly with `task-state-machine.md` and `failure-modes.md`

---

## Persistence Principles

- File-based storage (no embedded DB)
- Atomic writes using **write-then-rename**
- No in-place mutation of state files
- Disk state is the **source of truth** on restart

---

## Directory Layout

```
data/
└── tasks/
    ├── pending/
    │   └── <task-id>.json
    ├── running/
    │   └── <task-id>.json
    └── completed/
        └── <task-id>.json
```

Each task exists in **exactly one directory** at any time.

---

## Task File Format

Each task is stored as a single JSON file.

Example:

```json
{
  "taskId": "task-123",
  "state": "PENDING",
  "createdAt": "2026-01-06T10:15:30Z",
  "payload": {
    "type": "EMAIL_SEND",
    "data": {
      "to": "user@example.com"
    }
  }
}
```

Notes:
- `state` is redundant but useful for validation
- `payload` is opaque to the scheduler

---

## Atomic State Transitions

State transitions are implemented as **file moves**, not edits.

### Transition: `PENDING → RUNNING`

Steps:
1. Read `pending/<task-id>.json`
2. Write updated file to temporary path `running/.tmp-<task-id>.json`
3. `fsync` temp file
4. Atomically rename to `running/<task-id>.json`
5. Delete original pending file

---

### Transition: `RUNNING → COMPLETED`

Steps:
1. Ensure task effects are applied
2. Write updated file to `completed/.tmp-<task-id>.json`
3. `fsync` temp file
4. Atomically rename to `completed/<task-id>.json`
5. Delete original running file

---

## Crash Recovery Logic

On scheduler startup:

1. Scan `completed/`
   - Tasks are terminal and ignored

2. Scan `running/`
   - All tasks are treated as `PENDING`
   - Files are moved back to `pending/`

3. Scan `pending/`
   - Tasks are eligible for execution

This guarantees:
- No task is lost
- Tasks may be retried
- At-least-once execution

---

## Handling Partial Writes

- Temporary files (`.tmp-*`) are ignored on startup
- Only fully renamed files are considered valid
- Partial writes are treated as failed transitions

---

## Invariants

- A task file must exist in exactly one state directory
- `COMPLETED` tasks are never retried
- State transitions are atomic

---

## Non-Goals

- Efficient querying
- Concurrent writers
- Large-scale task volumes
- Distributed storage

These concerns are deferred to later systems.

