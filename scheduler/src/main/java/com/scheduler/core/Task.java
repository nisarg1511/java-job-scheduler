package com.scheduler.core;

import java.time.Instant;

public final class Task {

    private final String taskId;
    private final TaskType type;
    private final TaskPayload payload;
    private final Instant createdAt;

    public Task(
            String taskId,
            TaskType type,
            TaskPayload payload,
            Instant createdAt
    ) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }

        this.taskId = taskId;
        this.type = type;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskType getType() {
        return type;
    }

    public TaskPayload getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
