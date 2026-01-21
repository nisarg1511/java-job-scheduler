# Design Decisions

This document explains _why_ the system is designed the way it is.

---

## Append-Only Log

We never modify or delete records.

Reasons:

- Crash safety
- Simple correctness model
- No partial writes
- Easy recovery

---

## Immutable Task IDs

Task IDs are globally unique **forever**.

We do NOT reuse IDs even after completion.

Why:

- Prevents state ambiguity
- Avoids log cleanup complexity
- Guarantees deterministic recovery

---

## Explicit State Transitions

Valid transition:
PENDING → COMPLETED

All other transitions are ignored.

This ensures:

- Idempotent recovery
- Safe replays
- No duplicate execution

---

## No Executors / Thread Pools

Execution is:

- Single-threaded
- Explicit
- Deterministic

Concurrency is deferred to future versions.
