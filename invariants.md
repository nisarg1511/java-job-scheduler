# System Invariants

These invariants define the correctness rules for the current scheduler implementation.

## Storage Invariants

1. The task log is append-only during normal operation.
2. Each valid log line represents one durable task state transition.
3. Recovery may truncate only a corrupted trailing record.
4. Earlier valid records must remain unchanged.
5. The latest valid record for a task ID determines its durable state.

## Identity Invariants

1. Task IDs are immutable.
2. Task IDs are never intentionally reused.
3. Duplicate task submission should be rejected before appending a new `PENDING` record.

## State Invariants

1. Valid durable states are `PENDING` and `COMPLETED`.
2. The only intended durable transition is `PENDING -> COMPLETED`.
3. `COMPLETED` is terminal.
4. A completed task must not be selected for execution again.

## Execution Invariants

1. A task must be pending before the scheduler attempts to execute it.
2. Completion is recorded only after payload execution succeeds.
3. If execution fails before completion is appended, the task remains eligible for retry.
4. Payloads should be idempotent because retries can occur after crashes.

## Recovery Invariants

1. Recovery must be deterministic for the same log contents.
2. In-memory task state must be reconstructible from durable records.
3. Corrupted trailing records are ignored after truncation.
4. No task with a durable `PENDING` record should disappear during recovery.
