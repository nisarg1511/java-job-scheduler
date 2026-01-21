# Java Project Structure & Core Interfaces

This document defines the **package structure, core abstractions, and interface contracts** for the Scheduler project.

The goal is to:
- Keep the system small and understandable
- Enforce invariants via structure
- Make failure handling explicit
- Avoid premature abstraction

---

## High-Level Package Structure

```
scheduler/
├── core/            # Domain concepts (Task, TaskState)
├── store/           # Persistence logic (file-based)
├── scheduler/       # Scheduling & execution loop
├── recovery/        # Startup recovery logic
├── util/            # Small utilities (time, IO helpers)
└── Main.java        # Entry point
```

Rules:
- `core` has **no dependency** on other packages
- `store` depends on `core`
- `scheduler` depends on `core` and `store`
- `recovery` depends on `store`

This enforces directionality.

---

## Core Domain Model

### TaskState

```java
public enum TaskState {
    PENDING,
    RUNNING,
    COMPLETED
}
```

---

### Task

```java
public final class Task {
    private final String taskId;
    private final TaskState state;
    private final TaskPayload payload;
    private final Instant createdAt;

    // constructor + getters only
}
```

Design decisions:
- `Task` is immutable
- State changes create a **new Task instance**
- Prevents accidental in-memory mutation

---

### TaskPayload

```java
public interface TaskPayload {
    void execute();
}
```

Notes:
- Scheduler treats payload as opaque
- Payload must be idempotent
- No return value (effects only)

---

## Persistence Layer

### TaskStore

```java
public interface TaskStore {
    void save(Task task);
    Task load(String taskId);
    List<Task> loadAll(TaskState state);
    void delete(String taskId, TaskState state);
}
```

Responsibilities:
- Persist tasks to disk
- Perform atomic writes
- Enforce "exactly one directory" invariant

---

### FileTaskStore (implementation)

```java
public class FileTaskStore implements TaskStore {
    // root data directory
}
```

Notes:
- Uses write-then-rename
- No in-place file edits
- Ignores `.tmp-*` files

---

## Scheduler Layer

### TaskExecutor

```java
public interface TaskExecutor {
    void execute(Task task) throws Exception;
}
```

Notes:
- Wraps payload execution
- Allows future instrumentation

---

### Scheduler

```java
public class Scheduler {
    private final TaskStore taskStore;
    private final TaskExecutor executor;

    public void runOnce();
}
```

Responsibilities:
- Pick next `PENDING` task
- Transition state
- Execute task
- Persist state transitions

No concurrency yet.

---

## Recovery Layer

### RecoveryManager

```java
public class RecoveryManager {
    private final TaskStore taskStore;

    public void recover();
}
```

Responsibilities:
- On startup:
  - Move all `RUNNING` tasks back to `PENDING`
  - Ignore `COMPLETED`

---

## Entry Point

### Main

```java
public class Main {
    public static void main(String[] args) {
        // 1. Initialize TaskStore
        // 2. Run recovery
        // 3. Start scheduler loop
    }
}
```

---

## Explicit Non-Goals (for now)

- Concurrency
- Thread pools
- Task prioritization
- Retry limits
- Backoff
- Observability

These will be added only if needed later.

---

## What You Should Implement First

**Implementation order:**
1. `TaskState`, `Task`, `TaskPayload`
2. `FileTaskStore`
3. `RecoveryManager`
4. `Scheduler.runOnce()`

One layer at a time. No shortcuts.

