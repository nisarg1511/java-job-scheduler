# Java Crash-Safe Job Scheduler

A small, deliberately engineered job scheduler written in Java 21. The project focuses on the core mechanics behind reliable background processing: durable task submission, append-only persistence, deterministic recovery, and conservative retry semantics after crashes.

This is not a framework wrapper. It is a from-scratch implementation designed to make the failure model explicit and easy to reason about.

## Why This Project Matters

Job schedulers sit behind many production systems: email delivery, report generation, data pipelines, webhooks, billing jobs, and internal automation. The hard part is not just running a function later. The hard part is deciding what happens when the process dies halfway through a write, a task fails after producing side effects, or the scheduler restarts with only disk state available.

This project demonstrates those systems-design concerns in a compact Java codebase.

## Highlights

- Implements a single-node scheduler with a simple submission API and execution loop.
- Persists task lifecycle events to an append-only JSON-lines log.
- Rebuilds in-memory scheduler state by replaying the durable log on startup.
- Detects corrupted or partially written records during recovery and truncates to the last valid log position.
- Uses immutable task identifiers to avoid ambiguous recovery behavior.
- Separates API, domain, scheduler, recovery, payload, and storage responsibilities into clear packages.
- Documents invariants, crash behavior, design decisions, and known limitations.

## Current Semantics

The implemented scheduler uses two durable states:

```text
PENDING -> COMPLETED
```

A submitted task is appended as `PENDING`. After successful execution, the scheduler appends a `COMPLETED` record. On restart, recovery replays the log and treats the latest valid state for each task as authoritative.

This gives the scheduler at-least-once execution semantics. If a task executes but the process crashes before `COMPLETED` is durably recorded, the task can be retried. For that reason, task payloads should be idempotent.

## Architecture

```text
scheduler/
├── src/main/java/com/scheduler
│   ├── api/          # Task submission API and submission exceptions
│   ├── core/         # Task model, task state, task type, and payload contract
│   ├── payload/      # Example payload implementations
│   ├── recovery/     # Log replay and recovery result modeling
│   ├── scheduler/    # Execution loop and in-memory task registry
│   ├── store/        # Append-only file-backed task store
│   └── App.java      # Demo entry point
├── data/             # Local append-only task log
├── design-docs/      # Detailed design notes
└── pom.xml           # Maven build
```

## How It Works

1. A client submits a task through `TaskSubmissionService`.
2. The service checks for duplicate task IDs in the existing log.
3. The payload is registered in memory for the current process.
4. A `PENDING` task record is appended to `data/task-log.txt` and flushed to disk.
5. `Scheduler.runOnce()` invokes recovery to rebuild pending/completed task sets from the log.
6. Pending tasks are executed one by one.
7. Successfully executed tasks are appended as `COMPLETED`.
8. Future recovery runs skip completed tasks and retry tasks that were not durably completed.

## Persistence Model

The log is newline-delimited JSON. Each row is one durable state transition.

```json
{"taskId":"task-1","state":"PENDING","timestamp":"2026-05-05T10:15:30Z"}
{"taskId":"task-1","state":"COMPLETED","timestamp":"2026-05-05T10:15:31Z"}
```

The log is append-only. Existing records are never mutated during normal operation. During recovery, if a malformed trailing record is detected, the store truncates the file to the last known-good byte offset.

## Running Locally

Requirements:

- Java 21
- Maven 3.x

```bash
cd scheduler
mvn clean test
mvn exec:java
```

The demo submits two print tasks and executes pending work using the file-backed log.

## Design Trade-Offs

This version is intentionally small. It optimizes for correctness and explainability over throughput.

| Decision | Reason |
| --- | --- |
| Single-threaded execution | Keeps recovery and ordering easy to reason about. |
| Append-only log | Avoids in-place mutation and simplifies crash recovery. |
| At-least-once semantics | Practical baseline for crash-safe execution without distributed coordination. |
| In-memory payload registry | Keeps persistence focused on task state transitions in V1. |
| No log compaction yet | Makes replay behavior transparent while the design is still small. |

## Known Limitations

- No concurrent workers or thread pool.
- No delayed scheduling or cron expressions.
- No retry limits, exponential backoff, or dead-letter queue.
- Payload implementations are registered in memory and are not fully serialized for restart across independently created processes.
- No log compaction or snapshotting.
- No distributed locking, leader election, or multi-node execution.

These are documented as deliberate V1 constraints, not accidental omissions.

## Future Improvements

- Persist payload metadata with a payload factory/registry for full restart reconstruction.
- Add retry policies, failure records, and dead-letter handling.
- Introduce a `RUNNING` state if/when concurrent workers are added.
- Add log compaction or snapshotting for large histories.
- Replace demo output with structured logging.
- Expand unit tests around duplicate submission, corrupted log recovery, and idempotent retries.

## Documentation Map

| Document | Purpose |
| --- | --- |
| [`resume.md`](resume.md) | Copy-ready resume bullets, GitHub description, and interview talking points. |
| [`architecture.md`](architecture.md) | Component layout, data flow, and recovery architecture. |
| [`design.md`](design.md) | Major design decisions and trade-offs. |
| [`invariants.md`](invariants.md) | Correctness rules the scheduler is expected to preserve. |
| [`crash_recovery.md`](crash_recovery.md) | Crash scenarios and replay-based recovery behavior. |
| [`limitations.md`](limitations.md) | Current V1 boundaries and future work. |
| [`scheduler/design-docs`](scheduler/design-docs) | More detailed implementation design notes. |

## Resume Summary

Built a Java 21 crash-safe job scheduler from scratch using an append-only JSON-lines write-ahead log, deterministic replay-based recovery, immutable task IDs, and at-least-once execution semantics. Designed and documented failure handling, storage invariants, and trade-offs for a minimal single-node background processing system.
