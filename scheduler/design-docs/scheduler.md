# Scheduler Design

## Overview

The scheduler is a single-threaded, file-backed job runner. It is designed to demonstrate the core mechanics of durable background processing without hiding them behind a framework.

The implementation prioritizes correctness, deterministic recovery, and readable failure semantics over throughput.

## Current Task Lifecycle

```text
PENDING -> COMPLETED
```

- `PENDING`: the task has been accepted and durably recorded.
- `COMPLETED`: the task executed successfully and completion was durably recorded.

There is no durable `RUNNING` state in the current implementation. If the process crashes while a task is executing, the task has no completion record and is retried on the next run.

## Scheduler Phases

### 1. Submission

`TaskSubmissionService` receives a client-provided task ID and `TaskPayload`. It checks the log for duplicate IDs, registers the payload in memory, and appends a `PENDING` record.

### 2. Recovery

Before execution, the scheduler invokes `RecoveryManager`. Recovery replays the append-only log and derives the latest known state for each task ID.

### 3. Selection

Pending task IDs are returned in replay order. The scheduler processes them one by one.

### 4. Execution

For each pending task ID, the scheduler looks up the payload from `TaskRegistry` and calls `payload.execute()`.

### 5. Commit

If execution succeeds, the scheduler appends a `COMPLETED` record through `TaskStore`.

## Failure Semantics

| Failure point | Result |
| --- | --- |
| Before durable submission | Task is not recovered because it was never durably accepted. |
| After durable submission | Task is recovered as pending. |
| During payload execution | Task can be retried. |
| After payload execution but before completion append | Task can be retried. |
| During completion append | Recovery keeps valid records and truncates malformed trailing data. |

## Guarantees

- The scheduler can rebuild task state from the log.
- Completed tasks are not selected again after a valid `COMPLETED` record exists.
- Pending tasks remain eligible until completion is durably recorded.
- Recovery is deterministic for a given valid log prefix.

## Non-Goals

- Exactly-once execution.
- Parallel execution.
- Delayed scheduling.
- Durable payload reconstruction.
- Distributed coordination.

## Future Extensions

The current design leaves room for retries with backoff, failure states, payload serialization, worker pools, log compaction, and eventually a durable `RUNNING` or lease state for concurrent execution.
