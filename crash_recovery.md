# Crash Recovery Model

Crash recovery is the main systems-design focus of this project. The scheduler assumes the JVM can stop at inconvenient moments and that only durable log records can be trusted after restart.

## Failure Assumptions

- The process can crash at any time.
- In-memory task state is lost after a crash.
- The final log write may be incomplete or corrupted.
- A task may finish execution but crash before completion is recorded.
- Task side effects must be idempotent if they can be retried.

## Recovery Strategy

1. Open the append-only task log.
2. Read one newline-delimited JSON record at a time.
3. Keep track of the last known-good byte offset.
4. Stop at the first malformed or partial record.
5. Truncate the log to the last valid position if corruption is detected.
6. Replay valid records to derive each task's latest state.
7. Return tasks whose latest state is `PENDING`.

## Why This Is Safe

The scheduler treats the log as the source of truth. If a `COMPLETED` record exists and is valid, the task is terminal. If no valid `COMPLETED` record exists, the task is eligible to run again.

This conservative approach prevents losing submitted work. It may cause duplicate execution after certain crashes, which is why payloads should be idempotent.

## Crash Scenarios

| Scenario | Recovery behavior |
| --- | --- |
| Crash before `PENDING` append is durable | The task may not appear in the log and is not recovered. |
| Crash after `PENDING` append | The task is recovered as pending. |
| Crash during task execution | The task remains pending unless completion was already appended. |
| Crash after execution but before `COMPLETED` append | The task is retried. |
| Crash during final log write | Recovery truncates the malformed trailing record and replays valid records. |

## Guarantee

The current design provides at-least-once execution for durably submitted tasks, not exactly-once execution.
