# On-Disk Persistence Model

The current scheduler uses a single append-only JSON-lines file as its durable store.

## Location

```text
scheduler/data/task-log.txt
```

The path is created by `FileTaskStore` if it does not already exist.

## Record Format

Each line is one serialized `TaskRecord`.

```json
{"taskId":"task-1","state":"PENDING","timestamp":"2026-05-05T10:15:30Z"}
{"taskId":"task-1","state":"COMPLETED","timestamp":"2026-05-05T10:15:31Z"}
```

Fields:

| Field | Meaning |
| --- | --- |
| `taskId` | Immutable task identity. |
| `state` | Durable state transition, currently `PENDING` or `COMPLETED`. |
| `timestamp` | Time when the record was created. |

## Write Path

`FileTaskStore.append()` serializes the record, appends a newline, writes it to the log, and forces the file channel to disk.

The implementation avoids modifying earlier records during normal operation.

## Read And Recovery Path

`FileTaskStore.loadAll()` reads records sequentially. It tracks the byte offset after each successfully parsed record.

If parsing fails because the final record is partial or corrupted, the store truncates the file to the last good offset and returns the valid records.

## Why JSON Lines?

JSON-lines is simple, inspectable, and easy to replay. It is not the most compact format, but it is a good fit for a small crash-recovery project where readability matters.

## Trade-Offs

| Choice | Trade-off |
| --- | --- |
| Single log file | Simple replay, but linear recovery cost. |
| No compaction | Clear history, but unbounded log growth. |
| JSON records | Easy debugging, but more bytes than binary formats. |
| State-only persistence | Focused implementation, but payloads are not fully reconstructible yet. |

## Future Improvements

- Add snapshots or compaction.
- Persist payload type and arguments.
- Store failure records and retry metadata.
- Add checksums for stronger corruption detection.
