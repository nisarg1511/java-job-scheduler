# Failure Reasoning Notes

These notes explain the reasoning behind the scheduler's conservative recovery behavior.

## What if the process crashes while writing to disk?

The final log record may be incomplete. During recovery, `FileTaskStore` reads records sequentially and stops when it sees malformed data. It then truncates the log back to the last known-good byte offset.

Result: valid earlier records are preserved, and the partial transition is treated as if it did not happen.

## What if a task is executed twice?

The scheduler provides at-least-once execution, so duplicate execution is possible after some crash windows. Payloads should therefore be idempotent.

Examples of idempotent approaches include using a stable operation ID, checking whether an output already exists, or making the external side effect naturally repeat-safe.

## What if the scheduler crashes after accepting a task?

If the `PENDING` record was flushed successfully, recovery will find the task and make it eligible for execution.

If the process died before the `PENDING` record was durable, the task was not safely accepted.

## What if the task completes but the scheduler crashes before recording `COMPLETED`?

The task will be retried because recovery has no durable proof of completion. This avoids losing work, but it means task side effects must tolerate repetition.

## Summary Table

| Question | Conservative answer |
| --- | --- |
| Is a task lost after durable submission? | It should be recovered from the log. |
| Can a task run twice? | Yes, after certain crashes. |
| Are completed tasks retried? | Not if a valid `COMPLETED` record exists. |
| Is exactly-once execution provided? | No. |
| What makes retries safe? | Idempotent payload effects. |
