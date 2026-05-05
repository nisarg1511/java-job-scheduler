# Architecture Overview

The scheduler is organized around one central idea: durable task state lives in an append-only log, while runtime state is reconstructed from that log whenever the scheduler starts.

## System Boundary

This is a single-node scheduler. Task submission, task execution, recovery, and persistence all run in one JVM process. There are no remote workers, distributed locks, or external databases in the current version.

## Core Components

| Component | Package | Responsibility |
| --- | --- | --- |
| Submission API | `com.scheduler.api` | Accepts task IDs and payloads, rejects duplicate IDs, and persists new pending tasks. |
| Domain model | `com.scheduler.core` | Defines task identifiers, task states, task types, and executable payloads. |
| Task registry | `com.scheduler.scheduler` | Holds in-memory mappings from task ID to executable payload for the current process. |
| Scheduler | `com.scheduler.scheduler` | Recovers state, selects pending tasks, executes payloads, and records completion. |
| Recovery manager | `com.scheduler.recovery` | Replays the durable log and derives pending/completed task sets. |
| File task store | `com.scheduler.store` | Appends JSON-lines task records, flushes writes to disk, and truncates corrupted trailing records during recovery. |

## Data Flow

```text
submit(taskId, payload)
        |
        v
register payload in memory
        |
        v
append PENDING record to task-log.txt
        |
        v
run scheduler
        |
        v
replay log to recover latest task states
        |
        v
execute pending payloads
        |
        v
append COMPLETED records
```

## Source Of Truth

The append-only log is the durable source of truth for task state. In-memory structures are caches derived from the log and can be discarded at any time.

## Recovery Model

Recovery is deterministic:

1. Read records from the log in order.
2. Stop if a corrupted or partial record is encountered.
3. Truncate the log to the last known-good byte position.
4. Build the latest known state for each task ID.
5. Return pending tasks for execution and completed task IDs for exclusion.

## Execution Model

Execution is single-threaded. The scheduler loops through recovered pending task IDs, looks up the payload in the registry, executes it, and appends a completion record after successful execution.

## Architectural Trade-Off

The implementation favors explicitness over scale. A production-grade scheduler would eventually need payload serialization, richer failure records, retry policies, concurrency, observability, and compaction. This project intentionally keeps the first version small so that correctness and failure behavior remain inspectable.
