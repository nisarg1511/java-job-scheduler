# Failure Modes And Recovery Semantics

This document describes how the current single-node scheduler behaves when failures occur.

## Core Assumptions

- The scheduler and task execution run in one JVM process.
- Persistence uses an append-only JSON-lines file.
- In-memory task payloads are not durable in V1.
- Durably submitted tasks should not be lost.
- Tasks may be retried, so payload effects should be idempotent.

## Failure Scenarios

| Scenario | Behavior |
| --- | --- |
| Process crashes before `PENDING` is appended | The task was not durably accepted and is not recovered. |
| Process crashes after `PENDING` is appended | Recovery finds the task and marks it pending. |
| Process crashes during execution | No completion is recorded, so the task can be retried. |
| Process crashes after execution but before `COMPLETED` append | The task can be retried, so payloads must be idempotent. |
| Process crashes during a log write | Recovery truncates the malformed trailing record and replays the valid prefix. |
| Payload throws an exception | The scheduler does not append `COMPLETED`; the task remains eligible for a later retry. |

## What The Scheduler Guarantees

- Replay-based recovery from valid log records.
- Terminal treatment for tasks with valid `COMPLETED` records.
- Conservative retry behavior for tasks without durable completion.
- No in-place mutation of valid historical records.

## What The Scheduler Does Not Guarantee

- Exactly-once execution.
- Durable reconstruction of arbitrary payload objects after a fresh restart.
- Retry limits or dead-letter handling.
- Isolation from non-idempotent side effects.
- High availability or distributed execution.

## Correctness Condition

The system is correct when task side effects are safe to repeat or externally de-duplicated. This is a standard requirement for at-least-once job execution systems.
