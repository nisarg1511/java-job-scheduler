# Task State Machine

This document defines the authoritative task state machine for the current implementation.

## Durable States

- `PENDING`: the task was accepted and written to the append-only log.
- `COMPLETED`: the task executed successfully and completion was written to the append-only log.

## State Diagram

```text
PENDING -> COMPLETED
```

## Legal Transition

### `PENDING -> COMPLETED`

This transition occurs only after the scheduler successfully executes the task payload.

Required order:

1. Select a pending task.
2. Execute its payload.
3. Append a `COMPLETED` record.
4. Treat the task as terminal during future recovery.

## Forbidden Transitions

- `COMPLETED -> PENDING`: completed tasks are terminal.
- `COMPLETED -> COMPLETED`: completion should not be appended repeatedly for the same task.
- Reusing a completed task ID for a new task: this would make recovery ambiguous.

## Retry Semantics

The scheduler does not persist a durable `RUNNING` state. If a task does not have a valid `COMPLETED` record, recovery treats it as pending.

This gives at-least-once execution semantics. Tasks may run more than once if a crash happens after side effects but before completion is persisted.

## Future State Machine

A future concurrent version may introduce additional states such as:

- `RUNNING`
- `FAILED`
- `DEAD_LETTERED`

Those states are intentionally outside the current implementation.
