# Resume And Interview Notes

Use this file as a copy-ready source for resumes, LinkedIn, GitHub project descriptions, and interview prep.

## One-Line Project Description

Built a Java 21 crash-safe job scheduler from scratch using append-only log persistence, deterministic recovery, immutable task IDs, and at-least-once execution semantics.

## Resume Bullets

- Built a single-node Java job scheduler with durable task submission, replay-based recovery, and append-only JSON-lines persistence.
- Implemented a file-backed task store that flushes state transitions to disk and truncates corrupted trailing records during recovery.
- Designed at-least-once execution semantics using `PENDING -> COMPLETED` state transitions and idempotent task assumptions.
- Separated scheduler responsibilities across API, domain, persistence, recovery, payload, and execution packages for maintainable architecture.
- Documented system invariants, failure modes, recovery behavior, limitations, and future scalability paths.

## Short GitHub Description

A from-scratch Java 21 crash-safe job scheduler that demonstrates durable background processing with append-only log persistence, deterministic recovery, and at-least-once task execution.

## Interview Talking Points

- Why append-only logs simplify recovery compared with mutating records in place.
- Why the scheduler provides at-least-once execution instead of pretending to provide exactly-once execution.
- How corrupted trailing log records are handled by replaying the valid prefix and truncating invalid data.
- Why immutable task IDs matter for deterministic recovery.
- What would be needed for a production V2: payload serialization, retry policies, dead-letter queues, compaction, structured logging, and concurrent workers.

## Honest Limitations To Mention

- The current implementation is intentionally single-threaded.
- Payload objects are registered in memory and are not fully reconstructed from disk yet.
- There is no retry policy, backoff, dead-letter queue, or distributed execution in V1.
- Exactly-once execution is not guaranteed; correctness depends on idempotent payload effects.

## Stronger Version For A Systems-Focused Resume

Built a Java 21 crash-safe job scheduler that models production background-processing concerns including durable task submission, write-ahead-log-style persistence, deterministic log replay, corrupted-record recovery, immutable task identity, and at-least-once execution semantics.
