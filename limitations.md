# Known Limitations

This project is intentionally scoped as a small, single-node scheduler. The limitations below are useful future-work boundaries and help keep the current implementation honest.

## Current Limitations

- No concurrent worker pool.
- No delayed tasks, cron scheduling, or priority queues.
- No retry limits, backoff, or dead-letter queue.
- No durable failure records for failed executions.
- No persisted payload serialization across independent process restarts.
- No log compaction, checkpointing, or snapshotting.
- No metrics, tracing, or structured logging.
- No distributed execution or high-availability model.

## Important Clarification

The scheduler persists task state, but executable payload objects currently live in an in-memory registry. This is acceptable for the V1 demo and for studying crash-safe state transitions, but a production scheduler would also persist payload type and arguments so tasks can be reconstructed after a completely fresh process start.

## Future Directions

A natural V2 could add:

- Payload serialization through a typed payload factory.
- Retry count tracking and exponential backoff.
- A `FAILED` or dead-letter terminal state.
- Log compaction after completed tasks are safely summarized.
- Concurrent workers with a durable `RUNNING` or lease-based state.
- Unit and integration tests for crash scenarios.
