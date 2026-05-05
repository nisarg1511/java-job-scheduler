# Java Project Structure And Core Interfaces

This document describes the current package structure and the role of each major abstraction.

## Package Layout

```text
com.scheduler
├── api/          # Public submission API
├── core/         # Domain types and payload contract
├── payload/      # Example task payloads
├── recovery/     # Replay-based recovery
├── scheduler/    # Execution loop and task registry
├── store/        # Append-only file persistence
└── App.java      # Demo entry point
```

## Dependency Direction

The intended dependency flow is simple:

```text
api       -> core, scheduler, store
scheduler -> core, recovery, store
recovery  -> core, store
store     -> core
payload   -> core
```

`core` should remain small and independent.

## Core Interfaces And Classes

### `TaskPayload`

```java
public interface TaskPayload {
    void execute() throws Exception;
}
```

Payloads contain the work a task performs. Because the scheduler provides at-least-once execution, payload effects should be idempotent.

### `TaskState`

```java
public enum TaskState {
    PENDING,
    COMPLETED
}
```

These are the durable task states in the current implementation.

### `TaskStore`

```java
public interface TaskStore {
    void append(TaskRecord record) throws IOException;
    List<TaskRecord> loadAll() throws IOException;
}
```

The store is intentionally minimal: append new state transitions and replay all valid records.

### `TaskRegistry`

```java
public interface TaskRegistry {
    void register(String taskId, TaskPayload payload);
    TaskPayload getTask(String taskId);
    boolean contains(String taskId);
}
```

The registry maps task IDs to executable payloads in memory.

### `RecoveryManager`

Recovery reads all valid records and returns:

- Pending task IDs.
- Completed task IDs.

### `Scheduler`

The scheduler coordinates recovery, payload lookup, execution, and completion persistence.

## Entry Point

`App.java` wires the components together for a local demo:

1. Create `FileTaskStore`.
2. Create `InMemoryTaskRegistry`.
3. Create `RecoveryManager`.
4. Create `Scheduler`.
5. Submit example print tasks.
6. Run the scheduler once.

## Future Structure Changes

A production-oriented version would likely add:

- A payload serialization package.
- A retry policy package.
- A structured logging layer.
- Tests for store recovery and scheduler behavior.
- Worker abstractions for parallel execution.
