# Design Decisions

This document captures the major choices behind the Java crash-safe job scheduler.

## Append-Only Log

Task state transitions are appended to `data/task-log.txt` as newline-delimited JSON records. Existing records are not updated in place.

Why this choice works well for V1:

- It makes writes simple and auditable.
- It enables replay-based recovery.
- It avoids complex synchronization around mutable state files.
- It provides a clear history of every durable state transition.

## Durable States

The implemented durable states are:

```text
PENDING -> COMPLETED
```

`PENDING` means the scheduler knows about the task and may execute it. `COMPLETED` means the task finished successfully and the completion was persisted.

A separate durable `RUNNING` state is intentionally not implemented in V1. In a single-threaded process, a crash terminates the executing task anyway, so a task without a durable `COMPLETED` record is retried as pending.

## Immutable Task IDs

Task IDs are treated as permanent identities. A submitted task ID must not be reused.

This prevents ambiguity during log replay. If the same ID could represent multiple different tasks over time, recovery would not be able to safely determine which task a completion record belongs to.

## At-Least-Once Execution

The scheduler does not claim exactly-once execution. It provides at-least-once behavior:

- A submitted task should not be lost after its `PENDING` record is durable.
- A task without a durable `COMPLETED` record may be executed again.
- Payloads should be idempotent so retries are safe.

This is the same practical foundation used by many real background job systems.

## Single-Threaded Scheduler

The scheduler executes one task at a time.

Benefits:

- Easier failure reasoning.
- Deterministic recovery and execution order.
- Smaller surface area for race conditions.
- Clearer code for a learning and portfolio project.

Concurrency is a reasonable future extension, but adding it too early would obscure the core recovery mechanics.

## In-Memory Payload Registry

The current log persists task state transitions, not full payload definitions. Payload objects are registered in memory for the current process.

This keeps V1 focused on scheduler state management. A later version could persist payload type plus serialized arguments and reconstruct payloads through a factory registry.

## Corrupt Record Handling

The file store reads log records sequentially. If it encounters a malformed trailing record, it truncates the file to the last valid byte offset.

This models a common crash scenario: the process dies while writing the final record. Earlier valid records remain recoverable.
